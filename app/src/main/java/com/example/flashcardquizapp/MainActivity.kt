package com.example.flashcardquizapp

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FlashcardQuizApp()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun FlashcardQuizApp() {

    val flashcards = listOf(
        Flashcard("What is the capital of France?", "Paris"),
        Flashcard("What is 2 + 2?", "4"),
        Flashcard("Who wrote 'Romeo and Juliet'?", "Shakespeare"),
        Flashcard("What is the largest planet in our solar system?", "Jupiter"),
        Flashcard("Who painted the Mona Lisa?", "Leonardo da Vinci"),
        Flashcard("What is the square root of 64?", "8"),
        Flashcard("What is the chemical symbol for water?", "H2O"),
        Flashcard("Who was the first President of the United States?", "George Washington"),
        Flashcard("What is the hardest natural substance on Earth?", "Diamond"),
        Flashcard("What year did World War I begin?", "1914"),
        Flashcard("What is the main ingredient in guacamole?", "Avocado"),
        Flashcard("What is the capital city of Japan?", "Tokyo"),
        Flashcard("How many continents are there?", "7"),
        Flashcard("Which planet is known as the Red Planet?", "Mars"),
        Flashcard("In which year did the Titanic sink?", "1912")
    )


    var currentQuestionIndex by remember { mutableStateOf(0) }
    var userAnswer by remember { mutableStateOf("") }
    var attemptsLeft by remember{mutableStateOf(3)}
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var showEndOfQuizSnackbar by remember { mutableStateOf(false) }
    val currentFlashcard = flashcards.getOrNull(currentQuestionIndex)

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Flashcards Quiz") })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (currentFlashcard != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = currentFlashcard.question, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = userAnswer,
                    onValueChange = { userAnswer = it },
                    label = { Text("Your Answer") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            handleAnswer(
                                userAnswer = userAnswer,
                                correctAnswer = currentFlashcard.answer,
                                attemptsLeft = attemptsLeft,
                                onAttemptFail = {attemptsLeft--},
                                onNextQuestion = {
                                    userAnswer = ""
                                    if (currentQuestionIndex < flashcards.size - 1) {
                                        currentQuestionIndex++
                                    } else {
                                        showEndOfQuizSnackbar = true
                                    }
                                },
                                snackbarHostState = snackbarHostState,
                                coroutineScope = coroutineScope
                            )
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        handleAnswer(
                            userAnswer = userAnswer,
                            correctAnswer = currentFlashcard.answer,
                            attemptsLeft = attemptsLeft,
                            onAttemptFail = { attemptsLeft-- },
                            onNextQuestion = {
                                userAnswer = ""
                                attemptsLeft = 3
                                if (currentQuestionIndex < flashcards.size - 1) {
                                    currentQuestionIndex++
                                } else {
                                    showEndOfQuizSnackbar = true
                                }
                            },
                            snackbarHostState = snackbarHostState,
                            coroutineScope = coroutineScope
                        )
                    }
                ) {
                    Text("Submit Answer")
                }
                Text("Attempts left: $attemptsLeft")

            } else {
                Text("Quiz Complete!")
            }

            if (showEndOfQuizSnackbar) {
                LaunchedEffect(snackbarHostState) {
                    val result = snackbarHostState.showSnackbar(
                        message = "Quiz Complete! Do you want to restart?",
                        actionLabel = "Restart"
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        currentQuestionIndex = 0
                        showEndOfQuizSnackbar = false
                    }
                }
            }
        }
    }
}

fun handleAnswer(
    userAnswer: String,
    correctAnswer: String,
    attemptsLeft: Int,
    onAttemptFail: () -> Unit,
    onNextQuestion: () -> Unit,
    snackbarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope
) {
    if (userAnswer.isNotBlank()) {
        val isCorrect = userAnswer.equals(correctAnswer, ignoreCase = true)
        coroutineScope.launch {
            if (isCorrect) {
                snackbarHostState.showSnackbar("Correct!")
                onNextQuestion()
            } else {
                if (attemptsLeft > 1) {
                    snackbarHostState.showSnackbar("Wrong! Try again.")
                    onAttemptFail()
                } else {
                    snackbarHostState.showSnackbar("Wrong! The answer is $correctAnswer")
                    onNextQuestion()
                }
            }
        }
    }
}

data class Flashcard(val question: String, val answer: String)

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    FlashcardQuizApp()
}
