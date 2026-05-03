package com.mindfulwake.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mindfulwake.data.models.WeatherData
import com.mindfulwake.ui.components.LiquidGlassCard
import com.mindfulwake.ui.theme.RobotoMono
import com.mindfulwake.viewmodel.WeatherViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WeatherScreen(viewModel: WeatherViewModel) {
    val weatherState by viewModel.weatherState.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.fetchWeather() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Weather", style = MaterialTheme.typography.headlineLarge)
                Text("Current conditions for your area",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = { viewModel.fetchWeather() }) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh",
                    tint = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(Modifier.height(20.dp))

        when {
            isLoading -> {
                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(8.dp))
                        Text("Fetching weather...", style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            error != null -> {
                LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null,
                            tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("Could not load weather", style = MaterialTheme.typography.titleSmall)
                            Text(error ?: "", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { viewModel.fetchWeather() }) { Text("Retry") }
                }
            }
            weatherState != null -> {
                WeatherContent(weatherState!!)
            }
            else -> {
                LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Enable location permission to see local weather.",
                        style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { viewModel.fetchWeather() }) {
                        Icon(Icons.Default.LocationOn, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Enable Location")
                    }
                }
            }
        }
        Spacer(Modifier.height(100.dp))
    }
}

@Composable
fun WeatherContent(weather: WeatherData) {
    // Hero card
    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        tint = androidx.compose.ui.graphics.Color(0x30FFFFFF)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Text(weather.cityName, style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "${weather.temperature.toInt()}°",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontFamily = RobotoMono, fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(weather.description.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Feels like ${weather.feelsLike.toInt()}°",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                weatherIcon(weather.icon),
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 72.dp.value.toInt().sp)
            )
        }
    }

    Spacer(Modifier.height(12.dp))

    // Stats row
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        WeatherStatCard("💧", "Humidity", "${weather.humidity}%", Modifier.weight(1f))
        WeatherStatCard("💨", "Wind", "${weather.windSpeed.toInt()} km/h", Modifier.weight(1f))
        WeatherStatCard("🌡️", "UV Index", "${weather.uvIndex.toInt()}", Modifier.weight(1f))
    }

    Spacer(Modifier.height(12.dp))

    // Sunrise / Sunset
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val fmt = SimpleDateFormat("h:mm a", Locale.getDefault())
        WeatherStatCard("🌅", "Sunrise", fmt.format(Date(weather.sunrise * 1000L)), Modifier.weight(1f))
        WeatherStatCard("🌇", "Sunset", fmt.format(Date(weather.sunset * 1000L)), Modifier.weight(1f))
    }

    if (weather.forecast.isNotEmpty()) {
        Spacer(Modifier.height(16.dp))
        Text("5-Day Forecast", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(weather.forecast) { day ->
                val dayFmt = SimpleDateFormat("EEE", Locale.getDefault())
                LiquidGlassCard(modifier = Modifier.width(80.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()) {
                        Text(dayFmt.format(Date(day.date * 1000L)),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(4.dp))
                        Text(weatherIcon(day.icon), style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(4.dp))
                        Text("${day.tempMax.toInt()}°",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold)
                        Text("${day.tempMin.toInt()}°",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun WeatherStatCard(emoji: String, label: String, value: String, modifier: Modifier) {
    LiquidGlassCard(modifier = modifier) {
        Text(emoji, style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(Modifier.height(4.dp))
        Text(value,
            style = MaterialTheme.typography.titleMedium.copy(fontFamily = RobotoMono),
            modifier = Modifier.align(Alignment.CenterHorizontally),
            color = MaterialTheme.colorScheme.onBackground)
        Text(label,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

fun weatherIcon(code: String): String = when {
    code.startsWith("01") -> "☀️"
    code.startsWith("02") -> "🌤️"
    code.startsWith("03") -> "⛅"
    code.startsWith("04") -> "☁️"
    code.startsWith("09") -> "🌦️"
    code.startsWith("10") -> "🌧️"
    code.startsWith("11") -> "⛈️"
    code.startsWith("13") -> "❄️"
    code.startsWith("50") -> "🌫️"
    else -> "🌡️"
}

private val androidx.compose.ui.unit.Dp.sp get() = this.value.sp