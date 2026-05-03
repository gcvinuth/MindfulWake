package com.mindfulwake.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mindfulwake.ui.components.AlarmCard
import com.mindfulwake.ui.components.LiquidGlassCard
import com.mindfulwake.ui.theme.RobotoMono
import com.mindfulwake.viewmodel.AlarmViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AlarmsScreen(viewModel: AlarmViewModel) {
    val alarms by viewModel.alarms.collectAsStateWithLifecycle(initialValue = emptyList())
    val currentTime by produceState(initialValue = "") {
        while (true) {
            value = SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(Date())
            kotlinx.coroutines.delay(1000)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Your Alarms",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    "Tap an alarm to toggle it on or off",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            LiquidGlassCard(modifier = Modifier.padding(0.dp)) {
                Text(
                    currentTime,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontFamily = RobotoMono
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        if (alarms.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🌙", style = MaterialTheme.typography.displayLarge)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "No alarms yet",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Tap NEW to create your first mindful alarm",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(alarms, key = { it.id }) { alarm ->
                    AlarmCard(
                        hour = alarm.hour,
                        minute = alarm.minute,
                        label = alarm.label,
                        isEnabled = alarm.isEnabled,
                        questionCount = alarm.questionCount,
                        difficulty = alarm.difficulty,
                        repeatDays = alarm.repeatDays,
                        onToggle = { enabled -> viewModel.toggleAlarm(alarm, enabled) },
                        onDelete = { viewModel.deleteAlarm(alarm) },
                        onClick = { viewModel.toggleAlarm(alarm, !alarm.isEnabled) }
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}