package com.mindfulwake.ui.screens

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mindfulwake.data.models.*
import com.mindfulwake.services.AlarmService
import com.mindfulwake.services.AlarmScheduler
import com.mindfulwake.ui.components.LiquidGlassCard
import com.mindfulwake.ui.components.MindfulWakeBackground
import com.mindfulwake.ui.theme.MindfulWakeTheme
import com.mindfulwake.ui.theme.RobotoMono
import com.mindfulwake.viewmodel.AlarmViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

class AlarmRingActivity : ComponentActivity() {
    private val viewModel: AlarmViewModel by viewModels()
    private var alarmService: AlarmService? = null

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            alarmService = (service as? AlarmService.TimerBinder)?.getService() as? AlarmService
        }
        override fun onServiceDisconnected(name: ComponentName?) { alarmService = null }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val alarmId = intent.getIntExtra(AlarmScheduler.EXTRA_ALARM_ID, -1)

        setContent {
            MindfulWakeTheme {
                AlarmRingScreen(
                    alarmId = alarmId,
                    viewModel = viewModel,
                    onDismiss = { stopAndFinish() },
                    onSnooze = { stopAndFinish() }
                )
            }
        }
    }

    private fun stopAndFinish() {
        alarmService?.stopAlarm()
        stopService(Intent(this, AlarmService::class.java))
        finish()
    }

    override fun onDestroy() {
        try { unbindService(connection) } catch (_: Exception) {}
        super.onDestroy()
    }
}

@Composable
fun AlarmRingScreen(
    alarmId: Int,
    viewModel: AlarmViewModel,
    onDismiss: () -> Unit,
    onSnooze: () -> Unit
) {
    var alarm by remember { mutableStateOf<Alarm?>(null) }
    var questions by remember { mutableStateOf<List<Question>>(emptyList()) }
    var currentQIndex by remember { mutableIntStateOf(0) }
    var selectedAnswer by remember { mutableStateOf<String?>(null) }
    var userText by remember { mutableStateOf("") }
    var isCorrect by remember { mutableStateOf<Boolean?>(null) }
    var correctCount by remember { mutableIntStateOf(0) }
    var phase by remember { mutableStateOf(RingPhase.RINGING) } // RINGING, QUESTIONS, DISMISSED

    val currentTime by produceState(initialValue = "") {
        while (true) {
            value = SimpleDateFormat("hh:mm", Locale.getDefault()).format(Date())
            delay(1000)
        }
    }
    val currentAmPm by produceState(initialValue = "") {
        value = SimpleDateFormat("a", Locale.getDefault()).format(Date())
    }

    LaunchedEffect(alarmId) {
        alarm = viewModel.getAlarmById(alarmId)
        alarm?.let { a ->
            questions = viewModel.getQuestionsForAlarm(a)
        }
    }

    // Pulse animation
    val pulseAnim = rememberInfiniteTransition(label = "pulse")
    val scale by pulseAnim.animateFloat(
        initialValue = 1f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
        label = "scale"
    )

    MindfulWakeBackground {
        when (phase) {
            RingPhase.RINGING -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Spacer(Modifier.height(48.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("⏰", style = MaterialTheme.typography.displayLarge,
                            modifier = Modifier.scale(scale))
                        Spacer(Modifier.height(16.dp))
                        Text(currentTime,
                            style = MaterialTheme.typography.displayLarge.copy(fontFamily = RobotoMono),
                            color = MaterialTheme.colorScheme.onBackground)
                        Text(currentAmPm,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary)
                        alarm?.label?.let {
                            if (it.isNotEmpty()) {
                                Spacer(Modifier.height(8.dp))
                                Text(it, style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(bottom = 48.dp)
                    ) {
                        Button(
                            onClick = { phase = RingPhase.QUESTIONS },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(28.dp)
                        ) {
                            Icon(Icons.Default.Quiz, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Answer Questions to Dismiss")
                        }
                        OutlinedButton(
                            onClick = onSnooze,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(28.dp)
                        ) {
                            Icon(Icons.Default.Snooze, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Snooze 5 min")
                        }
                    }
                }
            }

            RingPhase.QUESTIONS -> {
                val q = questions.getOrNull(currentQIndex)
                if (q == null) {
                    phase = RingPhase.DISMISSED
                    return@MindfulWakeBackground
                }

                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(48.dp))
                    // Progress
                    LinearProgressIndicator(
                        progress = { currentQIndex.toFloat() / questions.size },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("Question ${currentQIndex + 1} of ${questions.size}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(Modifier.height(32.dp))

                    LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text(q.questionText,
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth())
                    }

                    Spacer(Modifier.height(24.dp))

                    if (q.options.isNotEmpty()) {
                        // Multiple choice
                        q.options.forEach { option ->
                            val bgColor = when {
                                isCorrect == null -> Color.Transparent
                                option == q.answer && isCorrect == true -> Color(0x4000C853)
                                option == selectedAnswer && isCorrect == false -> Color(0x40FF1744)
                                option == q.answer -> Color(0x4000C853)
                                else -> Color.Transparent
                            }
                            OutlinedButton(
                                onClick = {
                                    if (isCorrect == null) {
                                        selectedAnswer = option
                                        isCorrect = option == q.answer
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = bgColor
                                )
                            ) { Text(option) }
                            Spacer(Modifier.height(8.dp))
                        }
                    } else {
                        // Text input
                        OutlinedTextField(
                            value = userText,
                            onValueChange = { userText = it },
                            label = { Text("Your answer") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    isCorrect?.let { correct ->
                        Spacer(Modifier.height(16.dp))
                        Text(
                            if (correct) "✅ Correct!" else "❌ Incorrect. Answer: ${q.answer}",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (correct) Color(0xFF4CAF50) else Color(0xFFFF5722)
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = {
                                if (correct) correctCount++
                                isCorrect = null
                                selectedAnswer = null
                                userText = ""
                                if (currentQIndex + 1 >= questions.size) {
                                    if (correct || correctCount >= questions.size - 1) {
                                        phase = RingPhase.DISMISSED
                                        onDismiss()
                                    } else {
                                        // Not all correct, restart
                                        currentQIndex = 0
                                        correctCount = 0
                                    }
                                } else {
                                    currentQIndex++
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(26.dp)
                        ) { Text(if (currentQIndex + 1 >= questions.size) "Finish" else "Next") }
                    } ?: run {
                        if (q.options.isEmpty()) {
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    isCorrect = userText.trim().equals(q.answer, ignoreCase = true)
                                },
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                shape = RoundedCornerShape(26.dp)
                            ) { Text("Submit") }
                        }
                    }
                }
            }

            RingPhase.DISMISSED -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🎉", style = MaterialTheme.typography.displayLarge)
                        Text("Good Morning!", style = MaterialTheme.typography.headlineLarge)
                        Text("You dismissed the alarm!", style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

enum class RingPhase { RINGING, QUESTIONS, DISMISSED }