package com.example.data

import com.example.model.BufferOverflowType
import com.example.model.ComparisonRow
import com.example.model.FlashcardItem
import com.example.model.FlowKind
import com.example.model.PresetScenario
import com.example.model.QuizItem
import com.example.model.SoundAlikeItem

object FlowContentRepository {

  val presets = listOf(
    PresetScenario(
      id = "preset_tryemit_trap",
      title = "The tryEmit() Trap",
      tag = "HOT INTERVIEW QUESTION",
      subtitle = "Why does tryEmit() return FALSE in ViewModel?",
      flowKind = FlowKind.SHARED_FLOW,
      replay = 0,
      extraBuffer = 0,
      overflow = BufferOverflowType.SUSPEND,
      explanation = "With replay=0, extraBuffer=0 and SUSPEND, tryEmit() will ALWAYS fail (return false) unless a subscriber is actively suspended waiting to receive! To allow tryEmit() to succeed without active subscribers, you MUST provide extraBufferCapacity >= 1 or use DROP_OLDEST/DROP_LATEST."
    ),
    PresetScenario(
      id = "preset_stateflow_dedup",
      title = "StateFlow Value Deduping",
      tag = "CORE BEHAVIOR",
      subtitle = "Why StateFlow skips emitting identical values",
      flowKind = FlowKind.STATE_FLOW,
      replay = 1,
      extraBuffer = 0,
      overflow = BufferOverflowType.DROP_OLDEST,
      explanation = "StateFlow applies an internal distinctUntilChanged() check (`Any.equals()`). If you assign or emit the exact same value that is already in `value`, StateFlow drops the emission and does not notify collectors!"
    ),
    PresetScenario(
      id = "preset_replay_catchup",
      title = "Replay Catch-Up Buffer",
      tag = "ARCHITECTURE",
      subtitle = "How late subscribers receive recent emissions",
      flowKind = FlowKind.SHARED_FLOW,
      replay = 2,
      extraBuffer = 2,
      overflow = BufferOverflowType.SUSPEND,
      explanation = "When replay = 2, any subscriber that joins LATER receives the 2 most recent values immediately upon subscription, while new values stream in real time."
    ),
    PresetScenario(
      id = "preset_slow_collector_drop",
      title = "Slow Collector Backpressure",
      tag = "PERFORMANCE",
      subtitle = "Handling collector delays with DROP_OLDEST",
      flowKind = FlowKind.SHARED_FLOW,
      replay = 1,
      extraBuffer = 1,
      overflow = BufferOverflowType.DROP_OLDEST,
      explanation = "When Collector B is slow (1.5s delay), rapid emissions quickly fill the total buffer (replay + extra = 2). With DROP_OLDEST, the oldest buffered item is discarded to make room for new incoming items!"
    ),
    PresetScenario(
      id = "preset_event_bus",
      title = "One-Off Navigation Event Bus",
      tag = "BEST PRACTICE",
      subtitle = "Replacement for deprecated SingleLiveEvent",
      flowKind = FlowKind.SHARED_FLOW,
      replay = 0,
      extraBuffer = 1,
      overflow = BufferOverflowType.DROP_OLDEST,
      explanation = "For one-off UI events (e.g., Navigate, ShowSnackbar) that should NOT be replayed on screen rotation, use replay = 0, extraBufferCapacity = 1, onBufferOverflow = DROP_OLDEST. This guarantees tryEmit() works safely from ViewModel!"
    )
  )

  val soundAlikes = listOf(
    SoundAlikeItem(
      id = "stateflow_vs_sharedflow",
      termA = "StateFlow",
      termB = "SharedFlow",
      mnemonic = "StateFlow = 'State Board' (always shows current score). SharedFlow = 'Broadcaster / Walkie-Talkie' (fires events to whoever is listening).",
      quickRule = "Use StateFlow for UI state that survives rotation (like screen data). Use SharedFlow for transient events (like snackbars, toasts, navigation).",
      summaryA = "Hot flow that holds a single current state value and replays it to new collectors. StateFlow is a specialized SharedFlow.",
      summaryB = "Highly configurable hot flow that broadcasts events to multiple collectors. Can configure replay buffer and overflow behavior.",
      comparisonRows = listOf(
        ComparisonRow("Initial Value", "Mandatory (must provide initial)", "Optional (defaults to none)"),
        ComparisonRow("Replay Cache", "Always exactly 1 (holds latest)", "Configurable (0, 1, 2, ... N)"),
        ComparisonRow("Value Property", "Direct read access via `.value`", "No direct `.value` property"),
        ComparisonRow("Equality Check", "Automatic distinctUntilChanged()", "Emits duplicate values freely"),
        ComparisonRow("Primary Use", "ViewModel UI State (ScreenState)", "Events, Navigation, User Actions")
      ),
      snippetA = """
        // StateFlow: Holds UI State
        private val _uiState = MutableStateFlow(UiState.Loading)
        val uiState: StateFlow<UiState> = _uiState.asStateFlow()
        
        // Reading synchronously:
        val current = _uiState.value
      """.trimIndent(),
      snippetB = """
        // SharedFlow: One-off Events
        private val _event = MutableSharedFlow<UiEvent>(
          replay = 0,
          extraBufferCapacity = 1,
          onBufferOverflow = BufferOverflow.DROP_OLDEST
        )
        val event: SharedFlow<UiEvent> = _event.asSharedFlow()
      """.trimIndent(),
      interviewGotcha = "Interviewers love asking: 'Is StateFlow an implementation of SharedFlow?' YES! StateFlow is a sub-interface of SharedFlow with replay=1, onBufferOverflow=DROP_OLDEST, and distinctUntilChanged."
    ),

    SoundAlikeItem(
      id = "statein_vs_sharein",
      termA = "stateIn(...)",
      termB = "shareIn(...)",
      mnemonic = "stateIn gives you a StateFlow (needs initialValue). shareIn gives you a SharedFlow (needs replay count).",
      quickRule = "Convert cold flow to StateFlow using .stateIn; convert cold flow to SharedFlow using .shareIn.",
      summaryA = "Converts any cold Flow into a StateFlow in a given CoroutineScope, with a SharingStarted strategy and mandatory initialValue.",
      summaryB = "Converts any cold Flow into a SharedFlow in a given CoroutineScope, with a SharingStarted strategy and replay count.",
      comparisonRows = listOf(
        ComparisonRow("Return Type", "StateFlow<T>", "SharedFlow<T>"),
        ComparisonRow("Requires initialValue?", "YES (mandatory parameter)", "NO"),
        ComparisonRow("Replay parameter?", "Fixed at 1", "Configurable (replay = 0, 1..)"),
        ComparisonRow("Typical Use", "repository.getUser().stateIn(...)", "repository.locationStream.shareIn(...)")
      ),
      snippetA = """
        val userFlow: StateFlow<User?> = userRepository
          .observeUser()
          .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
          )
      """.trimIndent(),
      snippetB = """
        val locationUpdates: SharedFlow<Location> = locationSource
          .updatesFlow()
          .shareIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            replay = 1
          )
      """.trimIndent(),
      interviewGotcha = "Remember that both take `started: SharingStarted`. Always use `WhileSubscribed(5000)` in Android ViewModels to survive 5-second configuration changes without wasting network calls!"
    ),

    SoundAlikeItem(
      id = "emit_vs_tryemit",
      termA = "emit(value)",
      termB = "tryEmit(value)",
      mnemonic = "emit = 'I am willing to WAIT (suspend) until there is room'. tryEmit = 'I must drop it off NOW (non-suspending), tell me if it fit!'",
      quickRule = "Use `emit()` inside coroutines (`viewModelScope.launch`). Use `tryEmit()` outside coroutines (e.g. standard callbacks), BUT only when buffer capacity > 0!",
      summaryA = "Suspending function. Pauses execution if the flow buffer is full until space becomes available.",
      summaryB = "Normal non-suspending function. Returns Boolean: true if value was buffered/delivered, false if buffer is full.",
      comparisonRows = listOf(
        ComparisonRow("Suspends?", "YES (must be in coroutine)", "NO (regular Kotlin function)"),
        ComparisonRow("Return Value", "Unit", "Boolean (true/false)"),
        ComparisonRow("Default SharedFlow", "Suspends until received", "FAILS (returns false!) if no buffer"),
        ComparisonRow("StateFlow Behavior", "Always succeeds (replaces value)", "Always returns true for StateFlow")
      ),
      snippetA = """
        viewModelScope.launch {
          // Will suspend safely if buffer is saturated
          _eventFlow.emit(UiEvent.ShowToast("Saved!"))
        }
      """.trimIndent(),
      snippetB = """
        // Inside standard callback (non-suspending):
        val success = _eventFlow.tryEmit(UiEvent.ShowToast("Saved!"))
        if (!success) {
          Log.w("Flow", "tryEmit failed because buffer is full!")
        }
      """.trimIndent(),
      interviewGotcha = "The #1 Flow bug in Android: `val flow = MutableSharedFlow<T>()` has replay=0, extraBuffer=0, SUSPEND. Calling `flow.tryEmit(x)` returns FALSE unless a collector is suspended awaiting!"
    ),

    SoundAlikeItem(
      id = "collect_vs_collectlatest",
      termA = "collect { }",
      termB = "collectLatest { }",
      mnemonic = "collect = 'Finish what you started'. collectLatest = 'Drop whatever you are doing if something newer arrives'.",
      quickRule = "Use collect for critical operations (e.g. DB writes, analytics). Use collectLatest for UI updates or searches where only the latest query matters.",
      summaryA = "Processes every single emitted value sequentially. Does not start processing the next value until the current block finishes.",
      summaryB = "Cancels the previous collector block execution whenever a new value is emitted before the current block finishes.",
      comparisonRows = listOf(
        ComparisonRow("Cancellation", "Never cancels previous action", "Cancels ongoing coroutine on new emit"),
        ComparisonRow("Backpressure", "Slow block delays the emitter", "Fast emitter overrides slow block"),
        ComparisonRow("Use Case", "Payment transactions, Logging", "Search-as-you-type, UI redraws")
      ),
      snippetA = """
        // Processes 1, 2, 3 completely
        flow.collect { item ->
          saveToDatabase(item) // Completes each item
        }
      """.trimIndent(),
      snippetB = """
        // If user types 'a' then 'ab' rapidly,
        // search('a') is cancelled mid-execution!
        searchQueryFlow.collectLatest { query ->
          fetchSearchResults(query)
        }
      """.trimIndent(),
      interviewGotcha = "Gotcha: If `collectLatest` has a network call followed by a DB write, canceling mid-way can leave partial state if cancellation is not handled cleanly."
    ),

    SoundAlikeItem(
      id = "debounce_vs_sample",
      termA = "debounce(time)",
      termB = "sample(period)",
      mnemonic = "debounce = 'Wait until typing STOPS for X ms'. sample = 'Take a photo every X ms regardless of activity'.",
      quickRule = "Use debounce for search input fields. Use sample for high-frequency telemetry (accelerometer, GPS, audio meters).",
      summaryA = "Emits an item only after a specified quiet window has elapsed without another emission.",
      summaryB = "Emits only the most recent item within periodic fixed time intervals.",
      comparisonRows = listOf(
        ComparisonRow("Trigger", "Silence/pause after burst", "Fixed clock ticks"),
        ComparisonRow("Search Bars", "PERFECT (waits for typing pause)", "Can trigger mid-word"),
        ComparisonRow("Sensors", "May never emit if sensor is noisy", "PERFECT (caps updates to 60fps)")
      ),
      snippetA = """
        searchQueryFlow
          .debounce(300) // Emits 300ms AFTER user stops typing
          .distinctUntilChanged()
          .collect { search(it) }
      """.trimIndent(),
      snippetB = """
        gyroscopeSensorFlow
          .sample(100) // Emits latest reading every 100ms
          .collect { updateUi(it) }
      """.trimIndent(),
      interviewGotcha = "Debounce resets its timer on EVERY emission. If a stream emits every 100ms and debounce is 300ms, it will NEVER emit until the stream stops!"
    ),

    SoundAlikeItem(
      id = "flatmaplatest_vs_flatmapconcat_vs_flatmapmerge",
      termA = "flatMapLatest",
      termB = "flatMapConcat / Merge",
      mnemonic = "Latest = 'Switch to newest flow'. Concat = 'Queue one after another in order'. Merge = 'Run all flows in parallel'.",
      quickRule = "flatMapLatest is what 90% of Android apps want for dynamic queries (search, user ID switch).",
      summaryA = "Transforms each value into a Flow, canceling the previous inner Flow as soon as a new value arrives.",
      summaryB = "Concat waits for the previous inner Flow to complete before starting the next. Merge collects multiple inner flows simultaneously.",
      comparisonRows = listOf(
        ComparisonRow("Concurrent Flows", "1 (cancels previous)", "Concat: 1 (queued), Merge: Multiple"),
        ComparisonRow("Ordering", "Only latest results emit", "Concat: strictly sequential; Merge: interleaved"),
        ComparisonRow("Search queries", "IDEAL (cancels old queries)", "Concat wastes requests; Merge causes race conditions")
      ),
      snippetA = """
        userIdFlow.flatMapLatest { id ->
          // When userId changes, old request is cancelled!
          userRepository.getUserDetailsFlow(id)
        }
      """.trimIndent(),
      snippetB = """
        // Concat: finishes user 1 before user 2
        userIdFlow.flatMapConcat { id ->
          userRepository.syncUserData(id)
        }
      """.trimIndent(),
      interviewGotcha = "Interview trap: If you use `flatMapMerge` for a search bar, results for 'cat' might arrive after 'caterpillar', showing stale data on screen!"
    ),

    SoundAlikeItem(
      id = "callbackflow_vs_channelflow",
      termA = "callbackFlow { }",
      termB = "channelFlow { }",
      mnemonic = "callbackFlow = for external listeners (GPS, Firebase, BroadcastReceiver). Must call awaitClose! channelFlow = for concurrent flow emitting.",
      quickRule = "Always use callbackFlow for wrapping legacy callback-based APIs. You MUST call awaitClose { unregisterListener() }.",
      summaryA = "A specialized channelFlow with strict safety rules designed to convert legacy callback listeners into a coroutine Flow.",
      summaryB = "A builder for concurrent, channel-backed flows where multiple coroutines can emit concurrently via `send()`.",
      comparisonRows = listOf(
        ComparisonRow("Primary Purpose", "Bridge callback APIs (GPS, View)", "Concurrent emissions from multiple coroutines"),
        ComparisonRow("awaitClose required?", "YES (throws exception if forgotten)", "Optional"),
        ComparisonRow("Thread Safety", "Thread-safe `trySend()`", "Thread-safe `send()` and `trySend()`")
      ),
      snippetA = """
        fun locationUpdates(): Flow<Location> = callbackFlow {
          val listener = LocationListener { location ->
            trySend(location) // Non-blocking send
          }
          locationManager.requestUpdates(listener)
          awaitClose { locationManager.removeUpdates(listener) }
        }
      """.trimIndent(),
      snippetB = """
        fun parallelDownload(): Flow<Progress> = channelFlow {
          launch { downloadPart1(::send) }
          launch { downloadPart2(::send) }
        }
      """.trimIndent(),
      interviewGotcha = "Top Android interview gotcha: What happens if you forget `awaitClose` in `callbackFlow`? The block finishes immediately, closes the flow, and cancels collection before any callback fires!"
    ),

    SoundAlikeItem(
      id = "collectasstate_vs_collectasstatewithlifecycle",
      termA = "collectAsStateWithLifecycle()",
      termB = "collectAsState()",
      mnemonic = "collectAsStateWithLifecycle = 'Android App Aware' (stops collecting when screen is in background). collectAsState = 'Blind' (keeps running even when phone is locked).",
      quickRule = "In Jetpack Compose on Android, ALWAYS use collectAsStateWithLifecycle() instead of collectAsState() to save battery and prevent leaks.",
      summaryA = "Lifecycle-aware Compose collector. Suspends flow collection when the Android Activity/Fragment drops below the specified Lifecycle state (e.g. STARTED).",
      summaryB = "Standard Compose collector from runtime library. Collects actively as long as the composable is in composition, completely ignoring Android Lifecycle.",
      comparisonRows = listOf(
        ComparisonRow("Library", "androidx.lifecycle.runtime.compose", "androidx.compose.runtime"),
        ComparisonRow("Lifecycle Awareness", "YES (stops on STOPPED)", "NO (ignores Android Activity/Fragment)"),
        ComparisonRow("Battery Impact", "LOW (frees resources in background)", "HIGH (wastes battery/network in background)")
      ),
      snippetA = """
        @Composable
        fun UserScreen(viewModel: UserViewModel) {
          // SAFE: stops collecting when app minimized
          val state by viewModel.uiState.collectAsStateWithLifecycle()
          UserContent(state)
        }
      """.trimIndent(),
      snippetB = """
        // UNSAFE on Android: keeps collecting in background!
        val state by viewModel.uiState.collectAsState()
      """.trimIndent(),
      interviewGotcha = "Interviewers ask: 'Why was collectAsStateWithLifecycle introduced?' Because `collectAsState` continues collecting even if the app goes to the background, burning CPU and battery!"
    )
  )

  val flashcards = listOf(
    FlashcardItem(
      id = "fc_cold_vs_hot",
      category = "Core Fundamentals",
      question = "What is the difference between a Cold Flow and a Hot Flow?",
      punchline = "Cold = On-demand (like Netflix). Hot = Live broadcast (like FM Radio).",
      keyPoints = listOf(
        "Cold Flow (standard `flow { }`): Producer code only runs when a collector calls `.collect()`. Each collector gets its own independent execution. Unicast.",
        "Hot Flow (`StateFlow`, `SharedFlow`, Channels): Active and emitting regardless of whether anyone is collecting. Values are shared across all active collectors. Multicast.",
        "Cold flows don't store data in memory. Hot flows can retain replay buffers in memory."
      ),
      interviewerGotcha = "Impress them by explaining: Cold flows can be transformed into hot flows using `stateIn()` or `shareIn()`."
    ),

    FlashcardItem(
      id = "fc_stateflow_vs_livedata",
      category = "StateFlow & SharedFlow",
      question = "How does StateFlow compare to Android LiveData?",
      punchline = "StateFlow is Kotlin-native, thread-safe, coroutine-powered, and doesn't require Android OS dependencies.",
      keyPoints = listOf(
        "LiveData requires Android OS Main Thread dispatch. StateFlow works on any CoroutineDispatcher (pure Kotlin multiplatform).",
        "StateFlow requires an initial value; LiveData does not.",
        "StateFlow has built-in operators like map, filter, combine, debounce. LiveData operators are limited (Transformations).",
        "LiveData automatically handles Android Lifecycle. StateFlow requires `repeatOnLifecycle` or `collectAsStateWithLifecycle` to achieve identical lifecycle safety."
      ),
      interviewerGotcha = "Always mention that StateFlow uses `distinctUntilChanged` by default, whereas LiveData emits identical values unless MediatorLiveData is used."
    ),

    FlashcardItem(
      id = "fc_whilesubscribed_5000",
      category = "SharingStarted",
      question = "Why do Android engineers use `SharingStarted.WhileSubscribed(5000)`?",
      punchline = "The '5000ms magic timeout' protects against costly reconnections during screen rotation (Configuration Changes)!",
      keyPoints = listOf(
        "When an Activity rotates, it is destroyed and recreated in ~100-300ms.",
        "During this tiny window, subscriber count drops from 1 to 0, then returns to 1.",
        "Without the 5000ms delay, the upstream cold flow would cancel and restart immediately, triggering duplicate network requests and UI flickering.",
        "If the user presses Home, the 5000ms expires and the flow safely stops upstream collection to preserve battery!"
      ),
      interviewerGotcha = "State: 'It bridges the gap between Activity recreation and true backgrounding.'"
    ),

    FlashcardItem(
      id = "fc_buffer_overflow",
      category = "Buffers & Overflow",
      question = "What are the 3 `BufferOverflow` strategies in `MutableSharedFlow`?",
      punchline = "SUSPEND (Wait), DROP_OLDEST (Evict back), DROP_LATEST (Reject front).",
      keyPoints = listOf(
        "BufferOverflow.SUSPEND (default): The emitter coroutine suspends when the buffer is full until all slow collectors free up space.",
        "BufferOverflow.DROP_OLDEST: Drops the oldest value currently sitting in the buffer to make room for the new emission. Emitter never suspends.",
        "BufferOverflow.DROP_LATEST: Drops the newly emitted value immediately. Buffer contents remain unchanged. Emitter never suspends.",
        "Total buffer capacity is `replay + extraBufferCapacity`."
      ),
      interviewerGotcha = "Crucial trap: `tryEmit()` cannot suspend. Therefore, `tryEmit()` will return FALSE if the overflow strategy is SUSPEND and the buffer is full!"
    ),

    FlashcardItem(
      id = "fc_tryemit_failure",
      category = "Buffers & Overflow",
      question = "When does `MutableSharedFlow.tryEmit()` fail and return false?",
      punchline = "When buffer capacity is 0 and subscribers are not suspended awaiting a value!",
      keyPoints = listOf(
        "By default, `MutableSharedFlow()` has `replay = 0, extraBufferCapacity = 0, onBufferOverflow = BufferOverflow.SUSPEND`.",
        "Because extraBufferCapacity = 0 and overflow is SUSPEND, there is NO place to hold the item.",
        "If `tryEmit()` was called, it cannot suspend. So it fails and returns false.",
        "Solution: Set `extraBufferCapacity = 1` or use `onBufferOverflow = BufferOverflow.DROP_OLDEST`."
      ),
      interviewerGotcha = "This is the single most common bug found in Android ViewModel event dispatching code."
    ),

    FlashcardItem(
      id = "fc_conflate",
      category = "Flow Operators",
      question = "What does the `conflate()` operator do in Flow?",
      punchline = "Skips intermediate values if the collector is too slow to process them.",
      keyPoints = listOf(
        "`flow.conflate()` is shorthand for `flow.buffer(capacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)`.",
        "The slow collector will always receive the freshest emitted item, dropping anything that arrived while it was busy.",
        "Emitter never suspends due to collector speed.",
        "Great for stock prices, sensor readings, or progress tickers where only the newest value matters."
      ),
      interviewerGotcha = "Contrast it with `collectLatest`: conflate drops upstream values in the buffer; collectLatest cancels the collector block mid-flight."
    ),

    FlashcardItem(
      id = "fc_repeatonlifecycle",
      category = "Android Lifecycle",
      question = "Why should we collect flows inside `repeatOnLifecycle` instead of just `lifecycleScope.launch`?",
      punchline = "Because `lifecycleScope.launch` does NOT stop collection when the app is in the background!",
      keyPoints = listOf(
        "`lifecycleScope.launch` keeps executing coroutines until the lifecycle reaches `DESTROYED`.",
        "If the user presses Home, the Activity is in `STOPPED` state, but `lifecycleScope.launch` continues collecting in the background, consuming CPU, network, and battery.",
        "`viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED)` cancels the coroutine when the app goes below STARTED and restarts collection when it returns."
      ),
      interviewerGotcha = "In Jetpack Compose, `collectAsStateWithLifecycle()` internally uses `repeatOnLifecycle`!"
    ),

    FlashcardItem(
      id = "fc_channels_vs_flows",
      category = "Core Fundamentals",
      question = "What is the difference between a Channel and a SharedFlow?",
      punchline = "Channel = Unicast Work Queue (each item goes to 1 worker). SharedFlow = Multicast Event Bus (each item goes to ALL subscribers).",
      keyPoints = listOf(
        "In a Channel, if there are 3 collectors, each emitted item is consumed by only ONE of them (like a task queue).",
        "In a SharedFlow, every active collector receives a copy of every emitted item (like radio broadcast).",
        "Channels can be closed (`channel.close()`), whereas SharedFlow is an ongoing infinite stream.",
        "Channels are hot streams designed for communication between coroutines."
      ),
      interviewerGotcha = "Interviewers ask: 'Can we replace Channel with SharedFlow for UI events?' Yes, SharedFlow with `extraBufferCapacity = 1, DROP_OLDEST` is the recommended pattern."
    )
  )

  val quizQuestions = listOf(
    QuizItem(
      id = "q1",
      question = "What does `MutableSharedFlow<Int>()` (default parameters) do when you call `tryEmit(42)` if no collector is currently suspended?",
      options = listOf(
        "Buffers the value 42 and delivers it when a collector appears",
        "Returns false and the value 42 is discarded",
        "Throws an IllegalStateException",
        "Suspends the calling thread until a subscriber arrives"
      ),
      correctIndex = 1,
      explanation = "By default, MutableSharedFlow has replay=0, extraBufferCapacity=0, and onBufferOverflow=SUSPEND. Because tryEmit() cannot suspend and there is zero buffer capacity, it returns false and drops the emission!",
      interviewTip = "Always configure `extraBufferCapacity = 1` or `BufferOverflow.DROP_OLDEST` if you plan to use `tryEmit()`."
    ),
    QuizItem(
      id = "q2",
      question = "You have a `StateFlow<Int>` with current value 10. A coroutine calls `_state.value = 10`. What happens to active collectors?",
      options = listOf(
        "Active collectors receive the value 10 again",
        "Collectors are NOT notified because StateFlow performs distinctUntilChanged()",
        "StateFlow crashes with DuplicateValueException",
        "The flow cancels and restarts"
      ),
      correctIndex = 1,
      explanation = "StateFlow checks equality (`equals()`) between the new value and old value. If they are equal, StateFlow does not trigger any emission to collectors.",
      interviewTip = "If you ever need to emit identical events repeatedly (like clicks), use SharedFlow instead of StateFlow!"
    ),
    QuizItem(
      id = "q3",
      question = "Why is `SharingStarted.WhileSubscribed(5_000)` preferred over `SharingStarted.Eagerly` in Android ViewModels?",
      options = listOf(
        "It speeds up network requests by 5 seconds",
        "It cancels upstream collection when the app is backgrounded, but tolerates brief orientation changes without restarting",
        "It forces the collector to wait 5 seconds before receiving the first item",
        "It caches up to 5000 items in memory"
      ),
      correctIndex = 1,
      explanation = "During a screen rotation, the Activity unbinds and rebinds in ~200ms. The 5000ms delay prevents the upstream flow from prematurely shutting down and restarting, while still stopping it when the app is genuinely sent to the background.",
      interviewTip = "Remember: 5000ms is the standard Android community convention."
    ),
    QuizItem(
      id = "q4",
      question = "What is the total buffer size of `MutableSharedFlow(replay = 2, extraBufferCapacity = 3)`?",
      options = listOf(
        "2 items",
        "3 items",
        "5 items (replay + extraBufferCapacity)",
        "Unlimited"
      ),
      correctIndex = 2,
      explanation = "Total buffer capacity of a SharedFlow is the sum of `replay` (cached for new subscribers) + `extraBufferCapacity` (headroom for active subscribers before overflow rules trigger). 2 + 3 = 5.",
      interviewTip = "New subscribers only receive up to `replay` items (2), not the full 5 buffer slots."
    ),
    QuizItem(
      id = "q5",
      question = "What happens if you use `collectLatest { search(it) }` and the user rapidly types: 'c', 'ca', 'cat'?",
      options = listOf(
        "All three searches run sequentially to completion in order",
        "The search for 'c' and 'ca' are cancelled mid-execution, and only 'cat' finishes",
        "Only 'c' runs, and 'ca' and 'cat' are ignored",
        "An exception is thrown due to concurrent modification"
      ),
      correctIndex = 1,
      explanation = "collectLatest cancels the coroutine running the current collector block as soon as a new value arrives from upstream. Only the latest search query ('cat') completes.",
      interviewTip = "collectLatest is ideal for search queries, but dangerous for database writes or analytics tracking!"
    ),
    QuizItem(
      id = "q6",
      question = "What happens if you forget to call `awaitClose { ... }` inside a `callbackFlow { ... }` builder?",
      options = listOf(
        "It works fine; awaitClose is optional",
        "The flow block exits immediately and closes, canceling collection before callbacks ever fire",
        "The app leaks memory and crashes with OOM",
        "It throws a compilation error"
      ),
      correctIndex = 1,
      explanation = "Without `awaitClose`, the callbackFlow coroutine block reaches the end and completes immediately. As a result, the flow closes and unregisters/cancels collection before external callbacks can even be triggered.",
      interviewTip = "Always put your listener cleanup logic inside `awaitClose { ... }`."
    ),
    QuizItem(
      id = "q7",
      question = "Which collector is recommended when collecting a Flow inside a Jetpack Compose screen on Android?",
      options = listOf(
        "`flow.collectAsState()`",
        "`flow.collectAsStateWithLifecycle()`",
        "`flow.toState()`",
        "`remember { flow.collect() }`"
      ),
      correctIndex = 1,
      explanation = "`collectAsStateWithLifecycle()` is lifecycle-aware. It pauses flow collection when the Activity/Fragment goes into the background, saving CPU and battery.",
      interviewTip = "`collectAsState()` is part of standard Compose runtime, but on Android you should always use `collectAsStateWithLifecycle()`."
    ),
    QuizItem(
      id = "q8",
      question = "Which buffer overflow strategy in MutableSharedFlow evicts the oldest queued item when buffer capacity is exceeded?",
      options = listOf(
        "BufferOverflow.SUSPEND",
        "BufferOverflow.DROP_OLDEST",
        "BufferOverflow.DROP_LATEST",
        "BufferOverflow.CONFLATE"
      ),
      correctIndex = 1,
      explanation = "`BufferOverflow.DROP_OLDEST` removes the oldest item currently in the buffer to make room for the newly incoming emission without suspending the emitter.",
      interviewTip = "Use `DROP_OLDEST` when fresher data is always more important than historical data."
    )
  )
}
