package com.example.eduapp.screen

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.eduapp.data.PuzzleRepository
import com.example.eduapp.database.DatabaseProvider
import com.example.eduapp.helper.AppPreferences
import com.example.eduapp.helper.SoundPlayer
import com.example.eduapp.helper.rememberAssetImage
import com.example.eduapp.ui.theme.AnswerCorrect
import com.example.eduapp.ui.theme.AnswerWrong
import com.example.eduapp.viewmodel.GameUiState
import com.example.eduapp.viewmodel.GameViewModel
import com.example.eduapp.viewmodel.GameViewModelFactory

/**
 * Activity screen: the puzzle itself.
 *
 * Answer feedback is shown through the option border rather than colour alone
 * where possible, and the correct answer is always revealed so a wrong guess
 * still teaches something.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    currentContext: Context,
    navController: NavHostController,
    playerName: String,
    level: Int,
    modifier: Modifier = Modifier
) {
    val dao = remember { DatabaseProvider.getDatabase(currentContext).appDao() }
    val puzzleRepository = remember { PuzzleRepository(currentContext) }
    val preferences = remember { AppPreferences(currentContext) }

    val viewModel: GameViewModel = viewModel(
        factory = GameViewModelFactory(puzzleRepository, dao)
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(playerName, level) {
        viewModel.startGame(playerName, level, preferences.shuffleQuestions)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Level $level - $playerName") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            when {
                state.isLoading -> Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(12.dp))
                    Text("Loading puzzles...")
                }

                state.errorMessage != null -> Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = state.errorMessage ?: "",
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))
                    OutlinedButton(onClick = { navController.popBackStack() }) {
                        Text("Back")
                    }
                }

                state.isFinished -> FinishedView(
                    state = state,
                    onPlayAgain = { navController.popBackStack() },
                    onViewScores = {
                        navController.navigate("scores") { popUpTo("landing") }
                    },
                    onHome = {
                        navController.navigate("landing") { popUpTo("landing") { inclusive = true } }
                    }
                )

                else -> PlayingView(
                    state = state,
                    onSelect = { option ->
                        val correct = viewModel.selectOption(option)
                        if (state.selectedOption == null) {
                            if (correct) {
                                SoundPlayer.playCorrect(preferences.soundEnabled)
                            } else {
                                SoundPlayer.playWrong(preferences.soundEnabled)
                            }
                        }
                    },
                    onNext = viewModel::nextQuestion
                )
            }
        }
    }
}

@Composable
private fun PlayingView(
    state: GameUiState,
    onSelect: (Int) -> Unit,
    onNext: () -> Unit
) {
    val question = state.current ?: return
    val imageBitmap = rememberAssetImage(question.imagePath)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Question ${state.questionNumber} of ${state.total}   -   Score ${state.score}",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
                .semantics {
                    contentDescription =
                        "Progress: question ${state.questionNumber} of ${state.total}"
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(state.progress)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
            )
        }

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (imageBitmap != null) {
                Image(
                    bitmap = imageBitmap,
                    contentDescription = "Puzzle picture. Work out the value of the missing symbol.",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 200.dp, max = 320.dp)
                        .padding(8.dp)
                )
            } else {
                Text(
                    text = "Could not load this puzzle image.",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(24.dp)
                )
            }
        }

        Text(
            text = "What is the missing value?",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        question.options.forEach { option ->
            val isCorrectOption = option == question.correctAnswer
            val isChosen = option == state.selectedOption

            val borderColour = when {
                !state.isAnswered -> MaterialTheme.colorScheme.primary
                isCorrectOption -> AnswerCorrect
                isChosen -> AnswerWrong
                else -> MaterialTheme.colorScheme.outlineVariant
            }
            val borderWidth = if (state.isAnswered && (isCorrectOption || isChosen)) 3.dp else 1.dp

            OutlinedButton(
                onClick = { onSelect(option) },
                enabled = !state.isAnswered,
                border = BorderStroke(borderWidth, borderColour),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .semantics {
                        contentDescription = when {
                            !state.isAnswered -> "Answer option $option"
                            isCorrectOption -> "$option, correct answer"
                            isChosen -> "$option, your answer, incorrect"
                            else -> "$option"
                        }
                    }
            ) {
                Text(
                    text = option.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        if (state.isAnswered) {
            val gotItRight = state.selectedOption == question.correctAnswer
            Text(
                text = if (gotItRight) {
                    "Correct."
                } else {
                    "Not quite. The answer was ${question.correctAnswer}."
                },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (gotItRight) AnswerCorrect else AnswerWrong
            )

            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
            ) {
                Text(if (state.index == state.total - 1) "Finish" else "Next question")
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun FinishedView(
    state: GameUiState,
    onPlayAgain: () -> Unit,
    onViewScores: () -> Unit,
    onHome: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Level ${state.level} complete", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))
        Text(
            text = "${state.score} / ${state.total}",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Saved for ${state.playerName}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = onPlayAgain,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
        ) { Text("Play another level") }

        Spacer(Modifier.height(10.dp))

        OutlinedButton(
            onClick = onViewScores,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
        ) { Text("High scores") }

        Spacer(Modifier.height(10.dp))

        OutlinedButton(
            onClick = onHome,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
        ) { Text("Home") }
    }
}