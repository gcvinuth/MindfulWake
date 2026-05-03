package com.mindfulwake.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mindfulwake.data.models.Lap
import com.mindfulwake.ui.components.LiquidGlassCard
import com.mindfulwake.ui.components.MindfulWakeBackground
import com.mindfulwake.ui.theme.RobotoMono
import kotlinx.coroutines.delay

@Composable
fun TimerStopwatchScreen() {
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Tab row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(50.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(4.dp)
        ) {
            listOf("Timer", "Stopwatch").forEachIndexed { i, label ->
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(50.dp))
                        .background(
                            if (selectedTab == i) MaterialTheme.colorScheme.primary
                            else Color.Transparent
                        )
                        .clickable { selectedTab = i }
                        .padding(vertical = 10.dp)
                ) {
                    Text(label,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (selectedTab == i) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        AnimatedContent(targetState = selectedTab, label = "tab") { tab ->
            if (tab == 0) TimerContent() else StopwatchContent()
        }
    }
}

@Composable
fun TimerContent() {
    var totalMs by remember { mutableLongStateOf(0L) }
    var remainingMs by remember { mutableLongStateOf(0L) }
    var isRunning by remember { mutableStateOf(false) }
    var hours by remember { mutableIntStateOf(0) }
    var minutes by remember { mutableIntStateOf(5) }
    var seconds by remember { mutableIntStateOf(0) }
    var isSetMode by remember { mutableStateOf(true) }

    LaunchedEffect(isRunning) {
        while (isRunning && remainingMs > 0) {
            delay(100)
            remainingMs -= 100
        }
        if (remainingMs <= 0 && !isSetMode) isRunning = false
    }

    val progress = if (totalMs > 0) remainingMs.toFloat() / totalMs else 0f
    val h = (remainingMs / 3600000).toInt()
    val m = ((remainingMs % 3600000) / 60000).toInt()
    val s = ((remainingMs % 60000) / 1000).toInt()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(16.dp))

        // Circular progress timer
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(280.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stroke = 12.dp.toPx()
                val radius = size.minDimension / 2 - stroke
                // Background arc
                drawCircle(
                    color = Color(0xFF1C1C2E),
                    radius = radius, style = Stroke(stroke)
                )
                // Progress arc
                if (totalMs > 0) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(Color(0xFF9B8FFF), Color(0xFF5ECBBD))
                        ),
                        startAngle = -90f,
                        sweepAngle = 360f * progress,
                        useCenter = false,
                        style = Stroke(stroke, cap = StrokeCap.Round)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (isSetMode) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        TimeDigitScroller(hours, 0..99) { hours = it }
                        Text(":", style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.primary)
                        TimeDigitScroller(minutes, 0..59) { minutes = it }
                        Text(":", style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.primary)
                        TimeDigitScroller(seconds, 0..59) { seconds = it }
                    }
                    Text("h  :  m  :  s", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Text(
                        String.format("%02d:%02d:%02d", h, m, s),
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontFamily = RobotoMono, fontWeight = FontWeight.Bold
                        ),
                        color = if (remainingMs < 10000) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        Spacer(Modifier.height(40.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // Reset
            if (!isSetMode) {
                FilledTonalIconButton(
                    onClick = {
                        isRunning = false
                        remainingMs = totalMs
                        isSetMode = true
                    },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset")
                }
            }

            // Play/Pause
            FloatingActionButton(
                onClick = {
                    if (isSetMode) {
                        totalMs = (hours * 3600 + minutes * 60 + seconds) * 1000L
                        remainingMs = totalMs
                        isSetMode = false
                        isRunning = true
                    } else {
                        isRunning = !isRunning
                    }
                },
                modifier = Modifier.size(72.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play/Pause",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun TimeDigitScroller(value: Int, range: IntRange, onChange: (Int) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = { onChange(if (value >= range.last) range.first else value + 1) },
            modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.KeyboardArrowUp, null, modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary)
        }
        Text(String.format("%02d", value),
            style = MaterialTheme.typography.displaySmall.copy(fontFamily = RobotoMono),
            color = MaterialTheme.colorScheme.onBackground)
        IconButton(onClick = { onChange(if (value <= range.first) range.last else value - 1) },
            modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.KeyboardArrowDown, null, modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun StopwatchContent() {
    var ms by remember { mutableLongStateOf(0L) }
    var isRunning by remember { mutableStateOf(false) }
    var laps by remember { mutableStateOf(listOf<Lap>()) }
    var lapStart by remember { mutableLongStateOf(0L) }

    LaunchedEffect(isRunning) {
        while (isRunning) {
            delay(10)
            ms += 10
        }
    }

    val h = ms / 3600000
    val m = (ms % 3600000) / 60000
    val s = (ms % 60000) / 1000
    val cs = (ms % 1000) / 10

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(32.dp))

        // Big time display
        LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = if (h > 0) String.format("%02d:%02d:%02d.%02d", h, m, s, cs)
                       else String.format("%02d:%02d.%02d", m, s, cs),
                style = MaterialTheme.typography.displayMedium.copy(
                    fontFamily = RobotoMono,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }

        Spacer(Modifier.height(32.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // Lap / Reset button
            FilledTonalButton(
                onClick = {
                    if (isRunning) {
                        val lapMs = ms - lapStart
                        laps = laps + Lap(laps.size + 1, lapMs, ms)
                        lapStart = ms
                    } else {
                        ms = 0; laps = emptyList(); lapStart = 0
                    }
                },
                modifier = Modifier.height(52.dp)
            ) {
                Icon(if (isRunning) Icons.Default.Flag else Icons.Default.Refresh,
                    contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (isRunning) "Lap" else "Reset")
            }

            Button(
                onClick = { isRunning = !isRunning; if (isRunning) lapStart = ms },
                modifier = Modifier.height(52.dp)
            ) {
                Icon(if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (isRunning) "Pause" else "Start")
            }
        }

        Spacer(Modifier.height(24.dp))

        // Laps
        if (laps.isNotEmpty()) {
            Text("Laps", style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Start))
            Spacer(Modifier.height(8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)) {
                items(laps.reversed()) { lap ->
                    val lapMs = lap.lapTimeMs
                    val lm = lapMs / 60000; val ls = (lapMs % 60000) / 1000; val lcs = (lapMs % 1000) / 10
                    LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Lap ${lap.number}", style = MaterialTheme.typography.labelLarge)
                            Text(
                                String.format("%02d:%02d.%02d", lm, ls, lcs),
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontFamily = RobotoMono
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}