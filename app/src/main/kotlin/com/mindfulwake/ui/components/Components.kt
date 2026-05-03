package com.mindfulwake.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.mindfulwake.ui.theme.*

// ===== LIQUID GLASS CARD =====
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    tint: Color = LiquidGlassDark,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    val baseModifier = modifier
        .clip(shape)
        .background(
            Brush.linearGradient(
                colors = listOf(
                    Color(0x30FFFFFF),
                    Color(0x10FFFFFF)
                ),
                start = Offset(0f, 0f),
                end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
            )
        )
        .border(
            width = 1.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0x50FFFFFF),
                    Color(0x10FFFFFF)
                )
            ),
            shape = shape
        )

    if (onClick != null) {
        Column(
            modifier = baseModifier.clickable(onClick = onClick).padding(16.dp),
            content = content
        )
    } else {
        Column(
            modifier = baseModifier.padding(16.dp),
            content = content
        )
    }
}

// ===== GLOW EFFECT =====
fun Modifier.glowEffect(
    color: Color = Color(0x409B8FFF),
    radius: Dp = 24.dp
): Modifier = this.drawBehind {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color, Color.Transparent),
            radius = radius.toPx()
        ),
        radius = radius.toPx()
    )
}

// ===== BACKGROUND GRADIENT =====
@Composable
fun MindfulWakeBackground(content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0E0E1A),
                        Color(0xFF0F1525),
                        Color(0xFF071520)
                    )
                )
            )
    ) {
        // Ambient glow blobs
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x151E0080), Color.Transparent),
                    radius = size.width * 0.6f,
                    center = Offset(size.width * 0.2f, size.height * 0.2f)
                ),
                radius = size.width * 0.6f,
                center = Offset(size.width * 0.2f, size.height * 0.2f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x12005F6B), Color.Transparent),
                    radius = size.width * 0.5f,
                    center = Offset(size.width * 0.8f, size.height * 0.6f)
                ),
                radius = size.width * 0.5f,
                center = Offset(size.width * 0.8f, size.height * 0.6f)
            )
        }
        content()
    }
}

// ===== ALARM CARD =====
@Composable
fun AlarmCard(
    hour: Int, minute: Int,
    label: String,
    isEnabled: Boolean,
    questionCount: Int,
    difficulty: com.mindfulwake.data.models.Difficulty,
    repeatDays: Set<Int>,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    val difficultyLabel = difficulty.name.lowercase().replaceFirstChar { it.uppercase() }
    val difficultyColor = when (difficulty) {
        com.mindfulwake.data.models.Difficulty.GENTLE -> Color(0xFF4CAF50)
        com.mindfulwake.data.models.Difficulty.FOCUSED -> MaterialTheme.colorScheme.primary
        com.mindfulwake.data.models.Difficulty.INTENSE -> Color(0xFFFF5722)
    }

    val cardAlpha = if (isEnabled) 1f else 0.5f

    LiquidGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(cardAlpha),
        onClick = onClick
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = String.format("%02d:%02d %s",
                        if (hour == 0 || hour == 12) 12 else hour % 12,
                        minute,
                        if (hour < 12) "AM" else "PM"),
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontFamily = com.mindfulwake.ui.theme.RobotoMono
                    ),
                    color = if (isEnabled) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (label.isNotEmpty()) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    AssistChip(
                        onClick = {},
                        label = { Text("${questionCount}Q", style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.height(24.dp)
                    )
                    AssistChip(
                        onClick = {},
                        label = { Text(difficultyLabel.uppercase(), style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.height(24.dp),
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = difficultyColor.copy(alpha = 0.2f),
                            labelColor = difficultyColor
                        )
                    )
                }
                if (repeatDays.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    val days = listOf("M", "T", "W", "T", "F", "S", "S")
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        days.forEachIndexed { i, d ->
                            val active = repeatDays.contains(i + 1)
                            Text(
                                text = d,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (active) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.outlineVariant,
                                modifier = Modifier.width(14.dp)
                            )
                        }
                    }
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle
            )
        }
    }
}

// ===== TIME PICKER WHEEL =====
@Composable
fun TimeWheelPicker(
    hour: Int,
    minute: Int,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit,
    isAm: Boolean,
    onAmPmChange: (Boolean) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        NumberScroller(
            value = if (hour == 0 || hour == 12) 12 else hour % 12,
            range = 1..12,
            onValueChange = { v ->
                onHourChange(if (isAm) { if (v == 12) 0 else v } else { if (v == 12) 12 else v + 12 })
            }
        )
        Text(":", style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 8.dp))
        NumberScroller(
            value = minute,
            range = 0..59,
            onValueChange = onMinuteChange,
            formatTwo = true
        )
        Spacer(Modifier.width(16.dp))
        Column {
            FilterChip(
                selected = isAm,
                onClick = { onAmPmChange(true) },
                label = { Text("AM") }
            )
            FilterChip(
                selected = !isAm,
                onClick = { onAmPmChange(false) },
                label = { Text("PM") }
            )
        }
    }
}

@Composable
fun NumberScroller(
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit,
    formatTwo: Boolean = false
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = {
            val next = if (value >= range.last) range.first else value + 1
            onValueChange(next)
        }) {
            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Up",
                tint = MaterialTheme.colorScheme.primary)
        }
        Text(
            text = if (formatTwo) String.format("%02d", value) else String.format("%02d", value),
            style = MaterialTheme.typography.displayMedium.copy(
                fontFamily = com.mindfulwake.ui.theme.RobotoMono
            ),
            color = MaterialTheme.colorScheme.primary
        )
        IconButton(onClick = {
            val prev = if (value <= range.first) range.last else value - 1
            onValueChange(prev)
        }) {
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Down",
                tint = MaterialTheme.colorScheme.primary)
        }
    }
}

// ===== PILL NAVIGATION BAR =====
@Composable
fun MindfulWakeNavBar(
    selected: Int,
    onSelect: (Int) -> Unit
) {
    val items = listOf(
        Triple(Icons.Default.Alarm, "ALARMS", 0),
        Triple(Icons.Default.Add, "NEW", 1),
        Triple(Icons.Default.Quiz, "MY Q'S", 2),
        Triple(Icons.Default.BarChart, "STATS", 3),
        Triple(Icons.Default.Cloud, "WEATHER", 4),
        Triple(Icons.Default.Timer, "TOOLS", 5)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(50.dp))
                .background(Color(0xFF1C1C2E))
                .border(1.dp, Color(0x30FFFFFF), RoundedCornerShape(50.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            items.forEach { (icon, label, index) ->
                val isSelected = selected == index
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .clip(CircleShape)
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { onSelect(index) }
                        .padding(vertical = 10.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            icon, contentDescription = label,
                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                   else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        if (isSelected) {
                            Text(
                                label,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}