package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.MarketRepository
import com.example.model.AutoRefreshInterval
import com.example.model.ChartRange
import com.example.model.IndexMarketData
import com.example.model.MarketSessionState
import com.example.model.QuoteDisplayMode
import com.example.model.WatchScreenDestination
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class MarketWatchUiState(
  val sp500Data: IndexMarketData? = null,
  val nasdaq100Data: IndexMarketData? = null,
  val isRefreshing: Boolean = false,
  val displayMode: QuoteDisplayMode = QuoteDisplayMode.INDEX_POINTS,
  val selectedRange: ChartRange = ChartRange.ONE_DAY,
  val currentDestination: WatchScreenDestination = WatchScreenDestination.DUAL_HOME,
  val autoRefreshInterval: AutoRefreshInterval = AutoRefreshInterval.THIRTY_SEC,
  val isAmbientMode: Boolean = false,
  val showWatchBezelInPreview: Boolean = true,
  val sessionState: MarketSessionState = MarketRepository.currentMarketSessionState(),
  val watchTimeFormatted: String = formatCurrentTime(),
  val lastUpdatedFormatted: String = "Syncing...",
  val secondsUntilNextRefresh: Int = 30
) {
  companion object {
    fun formatCurrentTime(millis: Long = System.currentTimeMillis()): String {
      return SimpleDateFormat("HH:mm", Locale.US).format(Date(millis))
    }

    fun formatTimestamp(millis: Long): String {
      return SimpleDateFormat("HH:mm:ss", Locale.US).format(Date(millis))
    }
  }
}

class MarketWatchViewModel(
  application: Application
) : AndroidViewModel(application) {

  private val repository = MarketRepository(application.applicationContext)

  private val _uiState = MutableStateFlow(MarketWatchUiState())
  val uiState: StateFlow<MarketWatchUiState> = _uiState.asStateFlow()

  private var autoRefreshJob: Job? = null
  private var clockJob: Job? = null

  init {
    refreshMarketData()
    startAutoRefreshLoop()
    startWatchClockLoop()
  }

  fun refreshMarketData() {
    if (_uiState.value.isRefreshing) return
    viewModelScope.launch {
      _uiState.update { it.copy(isRefreshing = true) }
      val range = _uiState.value.selectedRange
      val (sp500, nasdaq100) = repository.fetchBothIndices(range)
      val now = System.currentTimeMillis()
      _uiState.update { state ->
        state.copy(
          sp500Data = sp500,
          nasdaq100Data = nasdaq100,
          isRefreshing = false,
          sessionState = MarketRepository.currentMarketSessionState(now),
          watchTimeFormatted = MarketWatchUiState.formatCurrentTime(now),
          lastUpdatedFormatted = MarketWatchUiState.formatTimestamp(sp500.lastUpdatedMillis),
          secondsUntilNextRefresh = state.autoRefreshInterval.seconds
        )
      }
    }
  }

  fun navigateTo(destination: WatchScreenDestination) {
    _uiState.update { it.copy(currentDestination = destination) }
  }

  fun cycleDestinationForward() {
    val order = listOf(
      WatchScreenDestination.DUAL_HOME,
      WatchScreenDestination.SP500_DETAIL,
      WatchScreenDestination.NASDAQ100_DETAIL,
      WatchScreenDestination.WATCH_SETTINGS
    )
    val currentIdx = order.indexOf(_uiState.value.currentDestination).coerceAtLeast(0)
    val next = order[(currentIdx + 1) % order.size]
    navigateTo(next)
  }

  fun cycleDestinationBackward() {
    val order = listOf(
      WatchScreenDestination.DUAL_HOME,
      WatchScreenDestination.SP500_DETAIL,
      WatchScreenDestination.NASDAQ100_DETAIL,
      WatchScreenDestination.WATCH_SETTINGS
    )
    val currentIdx = order.indexOf(_uiState.value.currentDestination).coerceAtLeast(0)
    val prev = order[(currentIdx - 1 + order.size) % order.size]
    navigateTo(prev)
  }

  fun toggleDisplayMode() {
    _uiState.update { state ->
      val next = if (state.displayMode == QuoteDisplayMode.INDEX_POINTS) {
        QuoteDisplayMode.ETF_PRICE
      } else {
        QuoteDisplayMode.INDEX_POINTS
      }
      state.copy(displayMode = next)
    }
  }

  fun setDisplayMode(mode: QuoteDisplayMode) {
    _uiState.update { it.copy(displayMode = mode) }
  }

  fun selectChartRange(range: ChartRange) {
    if (_uiState.value.selectedRange == range) return
    _uiState.update { it.copy(selectedRange = range) }
    refreshMarketData()
  }

  fun setAutoRefreshInterval(interval: AutoRefreshInterval) {
    _uiState.update {
      it.copy(
        autoRefreshInterval = interval,
        secondsUntilNextRefresh = interval.seconds
      )
    }
    startAutoRefreshLoop()
  }

  fun toggleAmbientMode() {
    _uiState.update { it.copy(isAmbientMode = !it.isAmbientMode) }
  }

  fun toggleWatchBezelInPreview() {
    _uiState.update { it.copy(showWatchBezelInPreview = !it.showWatchBezelInPreview) }
  }

  private fun startAutoRefreshLoop() {
    autoRefreshJob?.cancel()
    val intervalSec = _uiState.value.autoRefreshInterval.seconds
    if (intervalSec <= 0) return

    autoRefreshJob = viewModelScope.launch {
      var remaining = intervalSec
      while (isActive) {
        delay(1000L)
        remaining--
        if (remaining <= 0) {
          refreshMarketData()
          remaining = _uiState.value.autoRefreshInterval.seconds
        } else {
          _uiState.update { it.copy(secondsUntilNextRefresh = remaining) }
        }
      }
    }
  }

  private fun startWatchClockLoop() {
    clockJob?.cancel()
    clockJob = viewModelScope.launch {
      while (isActive) {
        val now = System.currentTimeMillis()
        _uiState.update {
          it.copy(
            watchTimeFormatted = MarketWatchUiState.formatCurrentTime(now),
            sessionState = MarketRepository.currentMarketSessionState(now)
          )
        }
        delay(10_000L)
      }
    }
  }
}
