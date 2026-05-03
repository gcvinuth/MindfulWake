package com.mindfulwake.data.repository

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mindfulwake.data.models.*
import kotlinx.coroutines.flow.Flow

// ===== DAOs =====
@Dao
interface AlarmDao {
    @Query("SELECT * FROM alarms ORDER BY hour ASC, minute ASC")
    fun getAllAlarms(): Flow<List<Alarm>>

    @Query("SELECT * FROM alarms WHERE id = :id")
    suspend fun getAlarmById(id: Int): Alarm?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarm: Alarm): Long

    @Update
    suspend fun updateAlarm(alarm: Alarm)

    @Delete
    suspend fun deleteAlarm(alarm: Alarm)

    @Query("UPDATE alarms SET isEnabled = :enabled WHERE id = :id")
    suspend fun setAlarmEnabled(id: Int, enabled: Boolean)

    @Query("SELECT * FROM alarms WHERE isEnabled = 1")
    suspend fun getEnabledAlarms(): List<Alarm>
}

@Dao
interface QuestionDao {
    @Query("SELECT * FROM questions WHERE isCustom = 1 ORDER BY createdAt DESC")
    fun getCustomQuestions(): Flow<List<Question>>

    @Query("SELECT * FROM questions WHERE difficulty = :difficulty ORDER BY RANDOM() LIMIT :count")
    suspend fun getQuestionsForDifficulty(difficulty: String, count: Int): List<Question>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: Question): Long

    @Update
    suspend fun updateQuestion(question: Question)

    @Delete
    suspend fun deleteQuestion(question: Question)
}

@Dao
interface AlarmStatDao {
    @Query("SELECT * FROM alarm_stats ORDER BY triggeredAt DESC LIMIT 30")
    fun getRecentStats(): Flow<List<AlarmStat>>

    @Insert
    suspend fun insertStat(stat: AlarmStat): Long

    @Query("SELECT COUNT(*) FROM alarm_stats")
    suspend fun getTotalAlarms(): Int

    @Query("SELECT AVG(CAST(questionsCorrect AS FLOAT) / CAST(questionsAnswered AS FLOAT)) FROM alarm_stats WHERE questionsAnswered > 0")
    suspend fun getAverageAccuracy(): Float?

    @Query("SELECT SUM(snoozedCount) FROM alarm_stats")
    suspend fun getTotalSnoozes(): Int
}

// ===== DATABASE =====
@Database(
    entities = [Alarm::class, Question::class, AlarmStat::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MindfulWakeDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
    abstract fun questionDao(): QuestionDao
    abstract fun alarmStatDao(): AlarmStatDao

    companion object {
        @Volatile private var INSTANCE: MindfulWakeDatabase? = null

        fun getDatabase(context: Context): MindfulWakeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MindfulWakeDatabase::class.java,
                    "mindfulwake_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// ===== REPOSITORIES =====
class AlarmRepository(private val db: MindfulWakeDatabase) {
    val allAlarms = db.alarmDao().getAllAlarms()

    suspend fun insert(alarm: Alarm) = db.alarmDao().insertAlarm(alarm)
    suspend fun update(alarm: Alarm) = db.alarmDao().updateAlarm(alarm)
    suspend fun delete(alarm: Alarm) = db.alarmDao().deleteAlarm(alarm)
    suspend fun setEnabled(id: Int, enabled: Boolean) = db.alarmDao().setAlarmEnabled(id, enabled)
    suspend fun getById(id: Int) = db.alarmDao().getAlarmById(id)
    suspend fun getEnabledAlarms() = db.alarmDao().getEnabledAlarms()
}

class QuestionRepository(private val db: MindfulWakeDatabase) {
    val customQuestions = db.questionDao().getCustomQuestions()

    suspend fun insert(q: Question) = db.questionDao().insertQuestion(q)
    suspend fun update(q: Question) = db.questionDao().updateQuestion(q)
    suspend fun delete(q: Question) = db.questionDao().deleteQuestion(q)

    suspend fun getQuestionsForAlarm(alarm: Alarm): List<Question> {
        val count = alarm.questionCount
        return when (alarm.questionSource) {
            com.mindfulwake.data.models.QuestionSource.BUILT_IN ->
                BuiltInQuestions.getForDifficulty(alarm.difficulty, count)
            com.mindfulwake.data.models.QuestionSource.MY_QUESTIONS ->
                db.questionDao().getQuestionsForDifficulty(alarm.difficulty.name, count)
            com.mindfulwake.data.models.QuestionSource.MIXED -> {
                val half = count / 2
                val builtin = BuiltInQuestions.getForDifficulty(alarm.difficulty, half)
                val custom = db.questionDao().getQuestionsForDifficulty(alarm.difficulty.name, count - half)
                (builtin + custom).shuffled()
            }
        }
    }
}

class StatsRepository(private val db: MindfulWakeDatabase) {
    val recentStats = db.alarmStatDao().getRecentStats()

    suspend fun insert(stat: AlarmStat) = db.alarmStatDao().insertStat(stat)
    suspend fun getTotalAlarms() = db.alarmStatDao().getTotalAlarms()
    suspend fun getAverageAccuracy() = db.alarmStatDao().getAverageAccuracy()
    suspend fun getTotalSnoozes() = db.alarmStatDao().getTotalSnoozes()
}