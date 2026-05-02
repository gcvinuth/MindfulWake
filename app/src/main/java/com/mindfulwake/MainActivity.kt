package com.mindfulwake

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MindfulWakeTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MindfulWakeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = androidx.compose.ui.graphics.Color(0xFF8B5CF6),
            background = androidx.compose.ui.graphics.Color(0xFF0A0A14),
            surface = androidx.compose.ui.graphics.Color(0xFF1E1E2C)
        ),
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            NavigationBar {
                val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
                NavigationBarItem(
                    icon = { Text("⏰") },
                    label = { Text("Alarms") },
                    selected = currentRoute == "alarms",
                    onClick = { navController.navigate("alarms") }
                )
                NavigationBarItem(
                    icon = { Text("➕") },
                    label = { Text("New") },
                    selected = currentRoute == "create",
                    onClick = { navController.navigate("create") }
                )
                NavigationBarItem(
                    icon = { Text("⚙️") },
                    label = { Text("Settings") },
                    selected = currentRoute == "settings",
                    onClick = { navController.navigate("settings") }
                )
            }
        }
    ) { padding ->
        NavHost(navController, startDestination = "alarms", Modifier.padding(padding)) {
            composable("alarms") { AlarmsListScreen() }
            composable("create") { CreateAlarmScreen() }
            composable("settings") { SettingsScreen() }
        }
    }
}

@Composable
fun AlarmsListScreen() {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Your Alarms", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(16.dp))
        // Map over DB alarms here
        Card(modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("07:00 AM", style = MaterialTheme.typography.headlineMedium)
                    Text("Morning Routine", style = MaterialTheme.typography.bodyMedium)
                }
                Switch(checked = true, onCheckedChange = {})
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAlarmScreen() {
    var hour by remember { mutableStateOf("07") }
    var minute by remember { mutableStateOf("00") }
    var label by remember { mutableStateOf("") }
    
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Create Alarm", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(16.dp))
        
        OutlinedTextField(
            value = hour,
            onValueChange = { hour = it },
            label = { Text("Hour") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = minute,
            onValueChange = { minute = it },
            label = { Text("Minute") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = label,
            onValueChange = { label = it },
            label = { Text("Label") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { /* Save to DB and Schedule */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Alarm")
        }
    }
}

@Composable
fun SettingsScreen() {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Allow Snooze")
            Switch(checked = false, onCheckedChange = {})
        }
    }
}
