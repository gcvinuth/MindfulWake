package com.mindfulwake

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.mindfulwake.ui.components.MindfulWakeNavBar
import com.mindfulwake.ui.components.MindfulWakeBackground
import com.mindfulwake.ui.screens.*
import com.mindfulwake.ui.theme.MindfulWakeTheme
import com.mindfulwake.viewmodel.*

class MainActivity : ComponentActivity() {
    private val alarmViewModel: AlarmViewModel by viewModels()
    private val questionViewModel: QuestionViewModel by viewModels()
    private val statsViewModel: StatsViewModel by viewModels()
    private val weatherViewModel: WeatherViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* Handle permission results */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        requestPermissions()

        setContent {
            MindfulWakeTheme {
                MindfulWakeApp(
                    alarmViewModel = alarmViewModel,
                    questionViewModel = questionViewModel,
                    statsViewModel = statsViewModel,
                    weatherViewModel = weatherViewModel
                )
            }
        }
    }

    private fun requestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.VIBRATE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionLauncher.launch(permissions.toTypedArray())
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MindfulWakeApp(
    alarmViewModel: AlarmViewModel,
    questionViewModel: QuestionViewModel,
    statsViewModel: StatsViewModel,
    weatherViewModel: WeatherViewModel
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    MindfulWakeBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            // App header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // MindfulWake logo text
                Text(
                    "🕐",
                    style = MaterialTheme.typography.titleLarge
                )
                androidx.compose.ui.unit.Dp
                androidx.compose.foundation.layout.Spacer(Modifier.width(8.dp))
                Text(
                    "MindfulWake",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Screen content
            Box(modifier = Modifier.weight(1f)) {
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        if (targetState > initialState) {
                            slideInHorizontally { it } + fadeIn() togetherWith
                            slideOutHorizontally { -it } + fadeOut()
                        } else {
                            slideInHorizontally { -it } + fadeIn() togetherWith
                            slideOutHorizontally { it } + fadeOut()
                        }
                    },
                    label = "screen"
                ) { tab ->
                    when (tab) {
                        0 -> AlarmsScreen(alarmViewModel)
                        1 -> CreateAlarmScreen(alarmViewModel) { selectedTab = 0 }
                        2 -> MyQuestionsScreen(questionViewModel)
                        3 -> StatsScreen(statsViewModel)
                        4 -> WeatherScreen(weatherViewModel)
                        5 -> TimerStopwatchScreen()
                        else -> AlarmsScreen(alarmViewModel)
                    }
                }
            }

            // Navigation
            MindfulWakeNavBar(
                selected = selectedTab,
                onSelect = { selectedTab = it }
            )

            Spacer(Modifier.navigationBarsPadding())
        }
    }
}