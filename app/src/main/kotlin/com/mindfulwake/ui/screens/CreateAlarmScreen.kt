package com.mindfulwake.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mindfulwake.data.models.*
import com.mindfulwake.ui.components.*
import com.mindfulwake.viewmodel.AlarmViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAlarmScreen(
    viewModel: AlarmViewModel,
    onSaved: () -> Unit
) {
    var hour by remember { mutableIntStateOf(7) }
    var minute by remember { mutableIntStateOf(0) }
    var isAm by remember { mutableStateOf(true) }
    var label by remember { mutableStateOf("") }
    var questionCount by remember { mutableIntStateOf(3) }
    var questionSource by remember { mutableStateOf(QuestionSource.BUILT_IN) }
    var difficulty by remember { mutableStateOf(Difficulty.FOCUSED) }
    var repeatDays by remember { mutableStateOf(setOf<Int>()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        Text("Create Alarm", style = MaterialTheme.typography.headlineLarge)
        Text("Set your wake-up time and challenge level",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(Modifier.height(20.dp))

        // Wake Up Time
        SectionCard(title = "WAKE UP TIME") {
            TimeWheelPicker(
                hour = hour, minute = minute,
                onHourChange = { hour = it },
                onMinuteChange = { minute = it },
                isAm = isAm, onAmPmChange = { isAm = it }
            )
        }

        Spacer(Modifier.height(12.dp))

        // Alarm Label
        SectionCard(title = "ALARM LABEL") {
            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                placeholder = { Text("e.g. Morning Workout") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

        Spacer(Modifier.height(12.dp))

        // Question Source
        SectionCard(title = "QUESTION SOURCE") {
            Text("Choose where to pull questions from when this alarm rings.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            QuestionSource.values().forEach { source ->
                val (emoji, label) = when (source) {
                    QuestionSource.BUILT_IN -> "🧠" to "BUILT-IN"
                    QuestionSource.MY_QUESTIONS -> "📝" to "MY QUESTIONS"
                    QuestionSource.MIXED -> "🔀" to "MIXED"
                }
                SelectableRow(
                    emoji = emoji, label = label,
                    selected = questionSource == source,
                    onClick = { questionSource = source }
                )
                if (source != QuestionSource.values().last()) Spacer(Modifier.height(8.dp))
            }
        }

        Spacer(Modifier.height(12.dp))

        // Questions to Answer
        SectionCard(title = "QUESTIONS TO ANSWER") {
            Text("More questions = deeper wake-up. You must answer all correctly to dismiss the alarm.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            Slider(
                value = questionCount.toFloat(),
                onValueChange = { questionCount = it.toInt() },
                valueRange = 1f..10f,
                steps = 8
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("1", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("$questionCount questions",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary)
                Text("10", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(Modifier.height(12.dp))

        // Difficulty
        SectionCard(title = "DIFFICULTY") {
            Difficulty.values().forEach { diff ->
                val (emoji, label) = when (diff) {
                    Difficulty.GENTLE -> "🌄" to "Gentle"
                    Difficulty.FOCUSED -> "🧠" to "Focused"
                    Difficulty.INTENSE -> "⚡" to "Intense"
                }
                SelectableRow(
                    emoji = emoji, label = label,
                    selected = difficulty == diff,
                    onClick = { difficulty = diff }
                )
                if (diff != Difficulty.values().last()) Spacer(Modifier.height(8.dp))
            }
        }

        Spacer(Modifier.height(12.dp))

        // Repeat
        SectionCard(title = "REPEAT") {
            val dayLabels = listOf("S", "M", "T", "W", "T", "F", "S")
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                dayLabels.forEachIndexed { i, d ->
                    val day = i + 1
                    val selected = repeatDays.contains(day)
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable {
                                repeatDays = if (selected) repeatDays - day else repeatDays + day
                            }
                    ) {
                        Text(d, style = MaterialTheme.typography.labelLarge,
                            color = if (selected) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Save Button
        Button(
            onClick = {
                val finalHour = if (isAm) {
                    if (hour == 12) 0 else hour
                } else {
                    if (hour == 12) 12 else hour + 12
                }
                viewModel.createAlarm(
                    hour = finalHour, minute = minute,
                    label = label, questionCount = questionCount,
                    questionSource = questionSource, difficulty = difficulty,
                    repeatDays = repeatDays
                )
                onSaved()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp)
        ) {
            Icon(Icons.Default.Alarm, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Save Alarm", style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.height(100.dp))
    }
}

@Composable
fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
        Text(title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(12.dp))
        content()
    }
}

@Composable
fun SelectableRow(emoji: String, label: String, selected: Boolean, onClick: () -> Unit) {
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                else Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Text(emoji, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.titleMedium)
    }
}