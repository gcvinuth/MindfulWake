package com.mindfulwake.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters

// ===== ALARM MODEL =====
@Entity(tableName = "alarms")
@TypeConverters(Converters::class)
data class Alarm(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val hour: Int,
    val minute: Int,
    val label: String = "",
    val isEnabled: Boolean = true,
    val repeatDays: Set<Int> = emptySet(), // 1=Mon..7=Sun
    val questionCount: Int = 3,
    val questionSource: QuestionSource = QuestionSource.BUILT_IN,
    val difficulty: Difficulty = Difficulty.FOCUSED,
    val soundUri: String = "default",
    val vibrate: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

enum class QuestionSource { BUILT_IN, MY_QUESTIONS, MIXED }
enum class Difficulty { GENTLE, FOCUSED, INTENSE }

// ===== QUESTION MODEL =====
@Entity(tableName = "questions")
data class Question(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val questionText: String,
    val answer: String,
    val options: List<String> = emptyList(), // empty = open-ended
    val category: String = "General",
    val difficulty: Difficulty = Difficulty.FOCUSED,
    val isCustom: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

// ===== STATS MODEL =====
@Entity(tableName = "alarm_stats")
data class AlarmStat(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val alarmId: Int,
    val triggeredAt: Long,
    val dismissedAt: Long? = null,
    val questionsAnswered: Int = 0,
    val questionsCorrect: Int = 0,
    val snoozedCount: Int = 0
)

// ===== TIMER SESSION =====
data class TimerSession(
    val id: String = System.currentTimeMillis().toString(),
    val durationMs: Long,
    val remainingMs: Long,
    val isRunning: Boolean = false,
    val label: String = ""
)

// ===== STOPWATCH LAP =====
data class Lap(
    val number: Int,
    val lapTimeMs: Long,
    val totalTimeMs: Long
)

// ===== WEATHER =====
data class WeatherData(
    val temperature: Double,
    val feelsLike: Double,
    val humidity: Int,
    val description: String,
    val icon: String,
    val windSpeed: Double,
    val cityName: String,
    val uvIndex: Double = 0.0,
    val sunrise: Long = 0,
    val sunset: Long = 0,
    val forecast: List<ForecastDay> = emptyList()
)

data class ForecastDay(
    val date: Long,
    val tempMin: Double,
    val tempMax: Double,
    val description: String,
    val icon: String
)

// ===== TYPE CONVERTERS =====
class Converters {
    @TypeConverter
    fun fromIntSet(set: Set<Int>): String = set.joinToString(",")

    @TypeConverter
    fun toIntSet(str: String): Set<Int> =
        if (str.isEmpty()) emptySet() else str.split(",").mapNotNull { it.toIntOrNull() }.toSet()

    @TypeConverter
    fun fromStringList(list: List<String>): String = list.joinToString("||")

    @TypeConverter
    fun toStringList(str: String): List<String> =
        if (str.isEmpty()) emptyList() else str.split("||")

    @TypeConverter
    fun fromQuestionSource(source: QuestionSource): String = source.name

    @TypeConverter
    fun toQuestionSource(name: String): QuestionSource =
        QuestionSource.valueOf(name)

    @TypeConverter
    fun fromDifficulty(d: Difficulty): String = d.name

    @TypeConverter
    fun toDifficulty(name: String): Difficulty = Difficulty.valueOf(name)
}

// Built-in questions bank
object BuiltInQuestions {
    val gentle = listOf(
        Question(questionText = "What is 5 + 7?", answer = "12", options = listOf("10", "11", "12", "13"), difficulty = Difficulty.GENTLE),
        Question(questionText = "What color is the sky?", answer = "Blue", options = listOf("Red", "Green", "Blue", "Yellow"), difficulty = Difficulty.GENTLE),
        Question(questionText = "How many days in a week?", answer = "7", options = listOf("5", "6", "7", "8"), difficulty = Difficulty.GENTLE),
        Question(questionText = "What is 3 × 4?", answer = "12", options = listOf("7", "10", "12", "14"), difficulty = Difficulty.GENTLE),
        Question(questionText = "Spell 'MORNING' backwards:", answer = "GNINROM", options = listOf("GNINROM", "GNIRNMO", "GNINOMR", "MORGING"), difficulty = Difficulty.GENTLE),
    )

    val focused = listOf(
        Question(questionText = "What is 17 × 8?", answer = "136", options = listOf("126", "136", "146", "156"), difficulty = Difficulty.FOCUSED),
        Question(questionText = "Unscramble: NROIM = ?", answer = "MINOR", options = listOf("MINOR", "MINER", "MANOR", "MIRON"), difficulty = Difficulty.FOCUSED),
        Question(questionText = "What is the square root of 144?", answer = "12", options = listOf("10", "11", "12", "13"), difficulty = Difficulty.FOCUSED),
        Question(questionText = "What planet is closest to the Sun?", answer = "Mercury", options = listOf("Venus", "Mercury", "Mars", "Earth"), difficulty = Difficulty.FOCUSED),
        Question(questionText = "What is 256 ÷ 16?", answer = "16", options = listOf("14", "15", "16", "17"), difficulty = Difficulty.FOCUSED),
        Question(questionText = "Type today's date (dd/mm/yyyy):", answer = "", options = emptyList(), difficulty = Difficulty.FOCUSED),
        Question(questionText = "What is 15% of 200?", answer = "30", options = listOf("25", "30", "35", "40"), difficulty = Difficulty.FOCUSED),
    )

    val intense = listOf(
        Question(questionText = "What is 47 × 23?", answer = "1081", options = listOf("1071", "1081", "1091", "1101"), difficulty = Difficulty.INTENSE),
        Question(questionText = "What is the chemical symbol for Gold?", answer = "Au", options = listOf("Go", "Gd", "Au", "Ag"), difficulty = Difficulty.INTENSE),
        Question(questionText = "Fibonacci: What comes after 21?", answer = "34", options = listOf("29", "32", "34", "42"), difficulty = Difficulty.INTENSE),
        Question(questionText = "What is log₂(64)?", answer = "6", options = listOf("5", "6", "7", "8"), difficulty = Difficulty.INTENSE),
        Question(questionText = "Rearrange: ANTILOPES → ?", answer = "NEOPLASIT", options = listOf("NEOPLASIT", "PLEONITAS", "ANTIPOLES", "PETALIONS"), difficulty = Difficulty.INTENSE),
        Question(questionText = "What is 999 × 9?", answer = "8991", options = listOf("8901", "8981", "8991", "9001"), difficulty = Difficulty.INTENSE),
    )

    fun getForDifficulty(d: Difficulty, count: Int): List<Question> {
        val pool = when (d) {
            Difficulty.GENTLE -> gentle
            Difficulty.FOCUSED -> focused
            Difficulty.INTENSE -> intense
        }
        return pool.shuffled().take(count.coerceAtMost(pool.size))
    }
}