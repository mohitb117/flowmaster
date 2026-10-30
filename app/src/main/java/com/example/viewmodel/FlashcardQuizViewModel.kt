package com.example.viewmodel

import androidx.lifecycle.ViewModel
import com.example.data.FlowContentRepository
import com.example.model.FlashcardItem
import com.example.model.QuizItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class FlashcardQuizUiState(
  val flashcards: List<FlashcardItem> = FlowContentRepository.flashcards,
  val selectedCategory: String = "All",
  val masteredIds: Set<String> = emptySet(),
  val flippedCardIds: Set<String> = emptySet(),
  val currentCardIndex: Int = 0,

  // Quiz state
  val quizQuestions: List<QuizItem> = FlowContentRepository.quizQuestions,
  val currentQuizIndex: Int = 0,
  val selectedAnswers: Map<Int, Int> = emptyMap(), // questionIndex -> selectedOptionIndex
  val isQuizCompleted: Boolean = false,
  val quizScore: Int = 0
)

class FlashcardQuizViewModel : ViewModel() {

  private val _uiState = MutableStateFlow(FlashcardQuizUiState())
  val uiState: StateFlow<FlashcardQuizUiState> = _uiState.asStateFlow()

  val categories: List<String> = listOf("All") + FlowContentRepository.flashcards.map { it.category }.distinct()

  fun selectCategory(category: String) {
    _uiState.update { it.copy(selectedCategory = category, currentCardIndex = 0) }
  }

  fun toggleCardFlip(cardId: String) {
    _uiState.update { state ->
      val newFlipped = if (state.flippedCardIds.contains(cardId)) {
        state.flippedCardIds - cardId
      } else {
        state.flippedCardIds + cardId
      }
      state.copy(flippedCardIds = newFlipped)
    }
  }

  fun toggleMastery(cardId: String) {
    _uiState.update { state ->
      val newMastered = if (state.masteredIds.contains(cardId)) {
        state.masteredIds - cardId
      } else {
        state.masteredIds + cardId
      }
      state.copy(masteredIds = newMastered)
    }
  }

  fun nextCard(maxSize: Int) {
    _uiState.update { state ->
      val next = if (state.currentCardIndex + 1 < maxSize) state.currentCardIndex + 1 else 0
      state.copy(currentCardIndex = next)
    }
  }

  fun previousCard(maxSize: Int) {
    _uiState.update { state ->
      val prev = if (state.currentCardIndex - 1 >= 0) state.currentCardIndex - 1 else maxSize - 1
      state.copy(currentCardIndex = prev)
    }
  }

  fun answerQuiz(questionIndex: Int, optionIndex: Int) {
    if (_uiState.value.isQuizCompleted) return
    _uiState.update { state ->
      val updatedAnswers = state.selectedAnswers + (questionIndex to optionIndex)
      state.copy(selectedAnswers = updatedAnswers)
    }
  }

  fun nextQuizQuestion() {
    val state = _uiState.value
    if (state.currentQuizIndex + 1 < state.quizQuestions.size) {
      _uiState.update { it.copy(currentQuizIndex = it.currentQuizIndex + 1) }
    } else {
      // Calculate final score
      val correctCount = state.quizQuestions.mapIndexed { idx, q ->
        if (state.selectedAnswers[idx] == q.correctIndex) 1 else 0
      }.sum()
      _uiState.update { it.copy(isQuizCompleted = true, quizScore = correctCount) }
    }
  }

  fun previousQuizQuestion() {
    _uiState.update { state ->
      if (state.currentQuizIndex > 0) {
        state.copy(currentQuizIndex = state.currentQuizIndex - 1)
      } else state
    }
  }

  fun resetQuiz() {
    _uiState.update {
      it.copy(
        currentQuizIndex = 0,
        selectedAnswers = emptyMap(),
        isQuizCompleted = false,
        quizScore = 0
      )
    }
  }
}
