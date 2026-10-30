package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FlowContentRepository
import com.example.model.BufferOverflowType
import com.example.model.CollectorState
import com.example.model.FlowKind
import com.example.model.LogCategory
import com.example.model.PresetScenario
import com.example.model.SimulationLogItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SimulatorUiState(
  val flowKind: FlowKind = FlowKind.SHARED_FLOW,
  val replay: Int = 1,
  val extraBufferCapacity: Int = 1,
  val overflowType: BufferOverflowType = BufferOverflowType.SUSPEND,
  val collectors: List<CollectorState> = listOf(
    CollectorState(id = "collector_a", label = "Collector A (Fast)", isSubscribed = true, delayMs = 0L),
    CollectorState(id = "collector_b", label = "Collector B (Slow 1.5s)", isSubscribed = true, delayMs = 1500L),
    CollectorState(id = "collector_c", label = "Collector C (Late Joiner)", isSubscribed = false, delayMs = 0L)
  ),
  val bufferedItems: List<String> = emptyList(),
  val stateFlowValue: String = "Init: 0",
  val nextPayloadId: Int = 1,
  val logs: List<SimulationLogItem> = emptyList(),
  val activePreset: PresetScenario? = null,
  val isEmitting: Boolean = false,
  val lastTryEmitResult: Boolean? = null
)

class SimulatorViewModel : ViewModel() {

  private val _uiState = MutableStateFlow(SimulatorUiState())
  val uiState: StateFlow<SimulatorUiState> = _uiState.asStateFlow()

  // Real underlying Coroutines Flows
  private var realSharedFlow = createSharedFlow(1, 1, BufferOverflowType.SUSPEND)
  private var realStateFlow = MutableStateFlow("Init: 0")

  private val collectorJobs = mutableMapOf<String, Job>()
  private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())

  init {
    restartEngine()
    log(LogCategory.BUFFER, "Simulator initialized with SharedFlow (replay=1, extraBuffer=1, SUSPEND)")
  }

  private fun createSharedFlow(replay: Int, extra: Int, overflow: BufferOverflowType): MutableSharedFlow<String> {
    val coroutineOverflow = when (overflow) {
      BufferOverflowType.SUSPEND -> BufferOverflow.SUSPEND
      BufferOverflowType.DROP_OLDEST -> BufferOverflow.DROP_OLDEST
      BufferOverflowType.DROP_LATEST -> BufferOverflow.DROP_LATEST
    }
    return MutableSharedFlow(
      replay = replay,
      extraBufferCapacity = extra,
      onBufferOverflow = coroutineOverflow
    )
  }

  fun setFlowKind(kind: FlowKind) {
    if (_uiState.value.flowKind == kind) return
    _uiState.update { it.copy(flowKind = kind, activePreset = null) }
    restartEngine()
    log(LogCategory.BUFFER, "Switched flow kind to $kind")
  }

  fun setReplay(replay: Int) {
    _uiState.update { it.copy(replay = replay, activePreset = null) }
    restartEngine()
    log(LogCategory.BUFFER, "Updated replay to $replay (Restarted flow)")
  }

  fun setExtraBuffer(extra: Int) {
    _uiState.update { it.copy(extraBufferCapacity = extra, activePreset = null) }
    restartEngine()
    log(LogCategory.BUFFER, "Updated extraBufferCapacity to $extra (Restarted flow)")
  }

  fun setOverflow(overflow: BufferOverflowType) {
    _uiState.update { it.copy(overflowType = overflow, activePreset = null) }
    restartEngine()
    log(LogCategory.BUFFER, "Updated onBufferOverflow to $overflow (Restarted flow)")
  }

  fun applyPreset(preset: PresetScenario) {
    _uiState.update {
      it.copy(
        activePreset = preset,
        flowKind = preset.flowKind,
        replay = preset.replay,
        extraBufferCapacity = preset.extraBuffer,
        overflowType = preset.overflow
      )
    }
    restartEngine()
    log(LogCategory.BUFFER, "Applied preset: '${preset.title}' — ${preset.subtitle}")
  }

  fun toggleCollector(collectorId: String) {
    val currentCollectors = _uiState.value.collectors
    val target = currentCollectors.find { it.id == collectorId } ?: return
    val newSubscribed = !target.isSubscribed

    _uiState.update { state ->
      state.copy(
        collectors = state.collectors.map {
          if (it.id == collectorId) it.copy(isSubscribed = newSubscribed) else it
        }
      )
    }

    if (newSubscribed) {
      log(LogCategory.COLLECTION, "${target.label} SUBSCRIBED (Will receive replay cache if available)")
      startCollectorJob(target.copy(isSubscribed = true))
    } else {
      collectorJobs[collectorId]?.cancel()
      collectorJobs.remove(collectorId)
      log(LogCategory.COLLECTION, "${target.label} UNSUBSCRIBED")
    }
  }

  fun emitSuspending() {
    val state = _uiState.value
    val payload = "Item #${state.nextPayloadId}"
    _uiState.update { it.copy(nextPayloadId = it.nextPayloadId + 1, isEmitting = true, lastTryEmitResult = null) }

    viewModelScope.launch {
      log(LogCategory.EMISSION, "Calling suspend emit(\"$payload\")...")
      updateBufferSnapshot(payload)

      if (state.flowKind == FlowKind.STATE_FLOW) {
        realStateFlow.value = payload
        _uiState.update { it.copy(stateFlowValue = payload, isEmitting = false) }
        log(LogCategory.EMISSION, "StateFlow updated to: $payload")
      } else {
        realSharedFlow.emit(payload)
        _uiState.update { it.copy(isEmitting = false) }
        log(LogCategory.EMISSION, "emit(\"$payload\") completed successfully")
      }
    }
  }

  fun emitDuplicateStateFlow() {
    val current = _uiState.value.stateFlowValue
    log(LogCategory.EMISSION, "Attempting to assign duplicate '$current' to StateFlow...")
    viewModelScope.launch {
      // In StateFlow, assigning identical value skips emission to collectors
      realStateFlow.value = current
      log(LogCategory.WARNING, "StateFlow distinctUntilChanged: Skipped notification because '$current' == old value!")
    }
  }

  fun tryEmitValue() {
    val state = _uiState.value
    val payload = "Item #${state.nextPayloadId}"
    _uiState.update { it.copy(nextPayloadId = it.nextPayloadId + 1) }

    if (state.flowKind == FlowKind.STATE_FLOW) {
      val success = realStateFlow.tryEmit(payload)
      _uiState.update { it.copy(stateFlowValue = payload, lastTryEmitResult = success) }
      log(LogCategory.EMISSION, "StateFlow.tryEmit(\"$payload\") returned $success (StateFlow always succeeds)")
      updateBufferSnapshot(payload)
    } else {
      val success = realSharedFlow.tryEmit(payload)
      _uiState.update { it.copy(lastTryEmitResult = success) }
      if (success) {
        log(LogCategory.EMISSION, "tryEmit(\"$payload\") -> SUCCESS (true): Buffered into queue")
        updateBufferSnapshot(payload)
      } else {
        log(
          LogCategory.WARNING,
          "tryEmit(\"$payload\") -> FAILED (false)! Buffer is full & overflow=SUSPEND. Cannot suspend!"
        )
      }
    }
  }

  fun clearLogs() {
    _uiState.update { it.copy(logs = emptyList()) }
  }

  fun resetAll() {
    _uiState.update {
      it.copy(
        nextPayloadId = 1,
        bufferedItems = emptyList(),
        logs = emptyList(),
        stateFlowValue = "Init: 0",
        lastTryEmitResult = null,
        collectors = it.collectors.map { c -> c.copy(receivedItems = emptyList()) }
      )
    }
    restartEngine()
    log(LogCategory.BUFFER, "Reset all flow data and subscriber buffers.")
  }

  private fun restartEngine() {
    collectorJobs.values.forEach { it.cancel() }
    collectorJobs.clear()

    val state = _uiState.value
    realSharedFlow = createSharedFlow(state.replay, state.extraBufferCapacity, state.overflowType)
    realStateFlow = MutableStateFlow(state.stateFlowValue)

    _uiState.value.collectors.filter { it.isSubscribed }.forEach { collector ->
      startCollectorJob(collector)
    }
  }

  private fun startCollectorJob(collector: CollectorState) {
    collectorJobs[collector.id]?.cancel()
    val job = viewModelScope.launch {
      if (_uiState.value.flowKind == FlowKind.STATE_FLOW) {
        realStateFlow.collect { item ->
          handleCollection(collector, item)
        }
      } else {
        realSharedFlow.collect { item ->
          handleCollection(collector, item)
        }
      }
    }
    collectorJobs[collector.id] = job
  }

  private suspend fun handleCollection(collector: CollectorState, item: String) {
    if (collector.delayMs > 0) {
      log(LogCategory.BUFFER, "${collector.label} received '$item' (Processing with ${collector.delayMs}ms delay...)")
      delay(collector.delayMs)
    }
    _uiState.update { state ->
      state.copy(
        collectors = state.collectors.map {
          if (it.id == collector.id) {
            it.copy(receivedItems = listOf(item) + it.receivedItems.take(7))
          } else it
        }
      )
    }
    log(LogCategory.COLLECTION, "${collector.label} finished consuming '$item'")
  }

  private fun updateBufferSnapshot(newItem: String) {
    val state = _uiState.value
    val maxCap = if (state.flowKind == FlowKind.STATE_FLOW) 1 else (state.replay + state.extraBufferCapacity)

    _uiState.update { current ->
      val updatedList = if (maxCap <= 0) {
        emptyList()
      } else {
        (current.bufferedItems + newItem).takeLast(maxCap)
      }
      current.copy(bufferedItems = updatedList)
    }
  }

  private fun log(category: LogCategory, message: String) {
    val item = SimulationLogItem(
      time = timeFormat.format(Date()),
      category = category,
      text = message
    )
    _uiState.update { it.copy(logs = listOf(item) + it.logs.take(30)) }
  }
}
