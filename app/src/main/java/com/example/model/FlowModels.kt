package com.example.model

enum class FlowKind {
  STATE_FLOW,
  SHARED_FLOW
}

enum class BufferOverflowType {
  SUSPEND,
  DROP_OLDEST,
  DROP_LATEST
}

data class SimulationLogItem(
  val id: Long = System.currentTimeMillis(),
  val time: String,
  val category: LogCategory,
  val text: String,
  val payload: String? = null
)

enum class LogCategory {
  EMISSION,
  COLLECTION,
  BUFFER,
  SUSPENDED,
  DROPPED,
  WARNING
}

data class CollectorState(
  val id: String,
  val label: String,
  val isSubscribed: Boolean,
  val delayMs: Long = 0L,
  val receivedItems: List<String> = emptyList()
)

data class PresetScenario(
  val id: String,
  val title: String,
  val tag: String,
  val subtitle: String,
  val flowKind: FlowKind,
  val replay: Int,
  val extraBuffer: Int,
  val overflow: BufferOverflowType,
  val explanation: String
)

data class SoundAlikeItem(
  val id: String,
  val termA: String,
  val termB: String,
  val mnemonic: String,
  val quickRule: String,
  val summaryA: String,
  val summaryB: String,
  val comparisonRows: List<ComparisonRow>,
  val snippetA: String,
  val snippetB: String,
  val interviewGotcha: String
)

data class ComparisonRow(
  val aspect: String,
  val valA: String,
  val valB: String
)

data class FlashcardItem(
  val id: String,
  val category: String,
  val question: String,
  val punchline: String,
  val keyPoints: List<String>,
  val interviewerGotcha: String,
  val codeExample: String? = null
)

data class QuizItem(
  val id: String,
  val question: String,
  val codeSnippet: String? = null,
  val options: List<String>,
  val correctIndex: Int,
  val explanation: String,
  val interviewTip: String
)
