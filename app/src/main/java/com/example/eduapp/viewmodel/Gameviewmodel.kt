package com.example.eduapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eduapp.data.PuzzleRepository
import com.example.eduapp.database.AppDao
import com.example.eduapp.database.User
import com.example.eduapp.logic.GameRules
import com.example.eduapp.logic.OptionGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** One question as presented to the player. */
data class GameQuestion(
    val imagePath: String,
    val correctAnswer: Int,
    val options: List<Int>
)

data class GameUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val playerName: String = "",
    val level: Int = 1,
    val questions: List<GameQuestion> = emptyList(),
    val index: Int = 0,
    val score: Int = 0,
    val selectedOption: Int? = null,
    val isAnswered: Boolean = false,
    val isFinished: Boolean = false
) {
    val current: GameQuestion? get() = questions.getOrNull(index)
    val questionNumber: Int get() = (index + 1).coerceAtMost(questions.size)
    val total: Int get() = questions.size
    val progress: Float get() = if (questions.isEmpty()) 0f else index.toFloat() / questions.size
}

class GameViewModel(
    private val puzzleRepository: PuzzleRepository,
    private val dao: AppDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var scoreSaved = false
    private var started = false

    /** Starts a round. Repeat calls are ignored so rotation does not restart the game. */
    fun startGame(playerName: String, level: Int, shuffle: Boolean) {
        if (started) return
        started = true
        scoreSaved = false

        viewModelScope.launch {
            try {
                val levels = puzzleRepository.loadLevels()
                val puzzles = levels.firstOrNull { it.level == level }?.puzzles.orEmpty()

                if (puzzles.isEmpty()) {
                    _uiState.value = GameUiState(
                        isLoading = false,
                        errorMessage = "No puzzles found for level $level."
                    )
                    return@launch
                }

                val ordered = if (shuffle) puzzles.shuffled() else puzzles

                val questions = ordered.mapIndexed { position, puzzle ->
                    GameQuestion(
                        imagePath = puzzle.imagePath,
                        correctAnswer = puzzle.answer,
                        // Seeded per question so the options stay put on rotation.
                        options = OptionGenerator.generate(
                            correctAnswer = puzzle.answer,
                            seed = (System.currentTimeMillis() / 1000) + position
                        )
                    )
                }

                _uiState.value = GameUiState(
                    isLoading = false,
                    playerName = playerName,
                    level = level,
                    questions = questions
                )
            } catch (e: Exception) {
                _uiState.value = GameUiState(
                    isLoading = false,
                    errorMessage = e.message ?: "Could not load the puzzles."
                )
            }
        }
    }

    /** @return true when the chosen option was correct, used to trigger sound. */
    fun selectOption(option: Int): Boolean {
        val state = _uiState.value
        val question = state.current ?: return false
        if (state.isAnswered) return false

        val correct = option == question.correctAnswer
        _uiState.value = state.copy(
            selectedOption = option,
            isAnswered = true,
            score = if (correct) state.score + 1 else state.score
        )
        return correct
    }

    fun nextQuestion() {
        val state = _uiState.value
        if (!state.isAnswered) return

        val nextIndex = state.index + 1
        if (nextIndex >= state.questions.size) {
            _uiState.value = state.copy(isFinished = true)
            saveScore()
        } else {
            _uiState.value = state.copy(
                index = nextIndex,
                selectedOption = null,
                isAnswered = false
            )
        }
    }

    private fun saveScore() {
        if (scoreSaved) return
        scoreSaved = true
        val state = _uiState.value

        viewModelScope.launch {
            dao.insert(
                User(
                    username = state.playerName,
                    level = state.level.toString(),
                    score = state.score
                )
            )
            dao.trimScores(GameRules.MAX_SCORES)
        }
    }
}