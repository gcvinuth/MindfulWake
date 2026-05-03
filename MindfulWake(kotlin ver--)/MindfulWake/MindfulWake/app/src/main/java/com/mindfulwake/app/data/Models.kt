package com.mindfulwake.app.data

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class Alarm(
    val id: String,
    var hour: Int,        // 1-12
    var minute: Int,
    var ampm: String,     // "AM" or "PM"
    var label: String,
    var questionCount: Int,
    var difficulty: String, // easy / medium / hard
    var days: List<Int>,    // 0=Sun..6=Sat
    var enabled: Boolean,
    var lastTriggered: String? = null
) {
    fun hour24(): Int {
        var h = hour
        if (ampm == "PM" && hour != 12) h += 12
        if (ampm == "AM" && hour == 12) h = 0
        return h
    }

    fun displayTime(): String =
        "%02d:%02d %s".format(hour, minute, ampm)
}

data class AppSettings(
    var sound: String = "classic",       // classic / gentle / nature / urgent
    var allowSnooze: Boolean = false,
    var penaltyEnabled: Boolean = true,
    var bedtimeTarget: String = "22:30",
    var bedtimeOffsetHours: Int = 1,
    var timerEnabled: Boolean = false,
    var autoAdjust: Boolean = true,
    var gradualVolume: Boolean = false,
    var agenda: String = ""
)

data class Stats(
    var streak: Int = 0,
    var lastCompletedDate: String? = null,
    var totalCorrect: Int = 0,
    var totalAnswered: Int = 0,
    var fastestTimeMs: Long? = null,
    var history: MutableList<HistoryEntry> = mutableListOf(),
    var weeklyPerformance: IntArray = IntArray(7)
)

data class HistoryEntry(
    val date: String,
    val accuracy: Int,
    val timeMs: Long
)

// ── Question types ──
data class QuizOption(val text: String, val isCorrect: Boolean)
data class Question(
    val question: String,
    val options: List<QuizOption>,
    val category: String,
    val explanation: String
)
