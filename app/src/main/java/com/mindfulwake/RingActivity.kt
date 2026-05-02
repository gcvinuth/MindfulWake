package com.mindfulwake

import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

class RingActivity : ComponentActivity() {
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val label = intent.getStringExtra("ALARM_LABEL") ?: "Wake Up!"

        // Play default alarm sound
        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        mediaPlayer = MediaPlayer.create(this, uri).apply {
            isLooping = true
            start()
        }

        setContent {
            MindfulWakeTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    QuizScreen(label) {
                        finishAlarm()
                    }
                }
            }
        }
    }

    private fun finishAlarm() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.stop()
        mediaPlayer?.release()
    }
}

@Composable
fun QuizScreen(label: String, onComplete: () -> Unit) {
    var phase by remember { mutableStateOf("ringing") } // ringing, quiz, complete
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var score by remember { mutableStateOf(0) }

    val questions = listOf(
        "What is 15 + 27?" to "42",
        "What is the capital of France?" to "Paris",
        "If x = 5, what is 2x + 3?" to "13"
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (phase) {
            "ringing" -> {
                Text(label, style = MaterialTheme.typography.headlineLarge)
                Spacer(Modifier.height(32.dp))
                Button(
                    onClick = { phase = "quiz" },
                    modifier = Modifier.fillMaxWidth().height(60.dp)
                ) {
                    Text("Wake Up & Answer")
                }
            }
            "quiz" -> {
                val q = questions[currentQuestionIndex]
                Text("Question ${currentQuestionIndex + 1}/${questions.size}")
                Spacer(Modifier.height(16.dp))
                Text(q.first, style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(32.dp))
                
                var answer by remember { mutableStateOf("") }
                OutlinedTextField(
                    value = answer,
                    onValueChange = { answer = it },
                    label = { Text("Your Answer") }
                )
                Spacer(Modifier.height(16.dp))
                Button(onClick = {
                    if (answer.trim().equals(q.second, ignoreCase = true)) {
                        score++
                    }
                    if (currentQuestionIndex < questions.size - 1) {
                        currentQuestionIndex++
                        answer = ""
                    } else {
                        phase = "complete"
                    }
                }) {
                    Text("Submit")
                }
            }
            "complete" -> {
                Text("Good Morning! ☀️", style = MaterialTheme.typography.headlineLarge)
                Spacer(Modifier.height(16.dp))
                Text("You answered $score/${questions.size} correctly.")
                Spacer(Modifier.height(32.dp))
                Button(
                    onClick = onComplete,
                    modifier = Modifier.fillMaxWidth().height(60.dp)
                ) {
                    Text("Start My Day")
                }
            }
        }
    }
}
