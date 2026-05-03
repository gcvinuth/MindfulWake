package com.mindfulwake.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mindfulwake.ui.components.LiquidGlassCard
import com.mindfulwake.ui.theme.RobotoMono
import com.mindfulwake.viewmodel.StatsViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun StatsScreen(viewModel: StatsViewModel) {
    val totalAlarms by viewModel.totalAlarms.collectAsStateWithLifecycle(0)
    val accuracy by viewModel.averageAccuracy.collectAsStateWithLifecycle(null)
    val totalSnoozes by viewModel.totalSnoozes.collectAsStateWithLifecycle(0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        Text("Stats", style = MaterialTheme.typography.headlineLarge)
        Text("Your wake-up performance",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatBigCard(
                "🔔", "Total Alarms",
                "$totalAlarms",
                MaterialTheme.colorScheme.primary,
                Modifier.weight(1f)
            )
            StatBigCard(
                "🧠", "Accuracy",
                "${((accuracy ?: 0f) * 100).toInt()}%",
                Color(0xFF4CAF50),
                Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatBigCard(
                "😴", "Total Snoozes",
                "$totalSnoozes",
                Color(0xFFFF9800),
                Modifier.weight(1f)
            )
            StatBigCard(
                "🏆", "Streak",
                "—",
                MaterialTheme.colorScheme.tertiary,
                Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(24.dp))

        LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
            Text("Performance Tips", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
            val tips = listOf(
                "💡 Try FOCUSED difficulty to improve mental agility",
                "📈 More questions = deeper wake-up state",
                "🎯 Answer 3+ questions daily for best results",
                "⚡ Reduce snooze usage for better sleep cycles"
            )
            tips.forEach { tip ->
                Text(tip, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(6.dp))
            }
        }

        Spacer(Modifier.height(100.dp))
    }
}

@Composable
fun StatBigCard(emoji: String, label: String, value: String, color: Color, modifier: Modifier) {
    LiquidGlassCard(modifier = modifier) {
        Text(emoji, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(4.dp))
        Text(value,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontFamily = RobotoMono,
                fontWeight = FontWeight.Bold
            ),
            color = color)
        Text(label, style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}