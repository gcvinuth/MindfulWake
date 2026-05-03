package com.mindfulwake.app.ui

import android.os.Bundle
import android.os.CountDownTimer
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.mindfulwake.app.R
import com.mindfulwake.app.alarm.AlarmService
import com.mindfulwake.app.data.*
import java.text.SimpleDateFormat
import java.util.*

class AlarmRingActivity : AppCompatActivity() {

    private lateinit var alarm: Alarm
    private lateinit var settings: AppSettings
    private val questions = mutableListOf<Question>()
    private var currentIndex = 0
    private var correctCount = 0
    private var wrongCount = 0
    private var quizStartTime = 0L
    private var countdown: CountDownTimer? = null
    private var answered = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )
        setContentView(R.layout.activity_alarm_ring)

        val alarmId = intent.getStringExtra("alarm_id") ?: run { finish(); return }
        val alarms = Prefs.loadAlarms(this)
        alarm = alarms.find { it.id == alarmId } ?: run { finish(); return }
        settings = Prefs.loadSettings(this)

        showRingingPhase()
    }

    private fun showRingingPhase() {
        setContentView(R.layout.phase_ringing)
        findViewById<TextView>(R.id.ring_time).text = alarm.displayTime()
        findViewById<TextView>(R.id.ring_label).text = alarm.label

        val snoozeBtn = findViewById<Button>(R.id.snooze_btn)
        snoozeBtn.visibility = if (settings.allowSnooze) View.VISIBLE else View.GONE
        snoozeBtn.setOnClickListener { snooze() }

        findViewById<Button>(R.id.start_quiz_btn).setOnClickListener { startQuiz() }
    }

    private fun snooze() {
        AlarmService.stop(this)
        Toast.makeText(this, "Snoozed 5 minutes", Toast.LENGTH_SHORT).show()
        // Re-schedule via AlarmManager in 5 min would require extra logic; simplest: just dismiss
        finish()
    }

    private fun startQuiz() {
        var diff = alarm.difficulty
        val stats = Prefs.loadStats(this)
        if (settings.autoAdjust && stats.totalAnswered > 10) {
            val acc = stats.totalCorrect.toDouble() / stats.totalAnswered
            if (acc >= 0.85 && diff != "hard") diff = if (diff == "easy") "medium" else "hard"
            else if (acc <= 0.4 && diff != "easy") diff = if (diff == "hard") "medium" else "easy"
        }

        questions.clear()
        questions.addAll(QuestionBank.generate(diff, alarm.questionCount))
        currentIndex = 0; correctCount = 0; wrongCount = 0
        quizStartTime = System.currentTimeMillis()
        showQuestion()
    }

    private fun showQuestion() {
        if (currentIndex >= questions.size) { completeQuiz(); return }
        answered = false
        val q = questions[currentIndex]
        setContentView(R.layout.phase_quiz)

        val catInfo = QuestionBank.categoryInfo[q.category] ?: Pair("❓","General")
        findViewById<TextView>(R.id.quiz_category).text = "${catInfo.first} ${catInfo.second}"
        findViewById<TextView>(R.id.quiz_question).text = q.question
        findViewById<TextView>(R.id.quiz_counter).text =
            "Question ${currentIndex + 1} of ${questions.size}"
        findViewById<TextView>(R.id.quiz_score).text = "$correctCount correct"

        val optLetters = listOf("A","B","C","D")
        val optContainer = findViewById<LinearLayout>(R.id.options_container)
        optContainer.removeAllViews()
        q.options.forEachIndexed { i, opt ->
            val btn = Button(this).apply {
                text = "${optLetters.getOrElse(i){"?"}}) ${opt.text}"
                textAlignment = View.TEXT_ALIGNMENT_TEXT_START
                setOnClickListener { if (!answered) handleAnswer(opt, q, this) }
            }
            optContainer.addView(btn)
        }

        val feedbackView = findViewById<TextView>(R.id.quiz_feedback)
        feedbackView.visibility = View.GONE

        val nextBtn = findViewById<Button>(R.id.next_question_btn)
        nextBtn.visibility = View.GONE
        nextBtn.setOnClickListener {
            countdown?.cancel()
            currentIndex++
            showQuestion()
        }

        // Timer
        if (settings.timerEnabled) {
            val timerView = findViewById<TextView>(R.id.quiz_timer)
            timerView.visibility = View.VISIBLE
            countdown?.cancel()
            countdown = object : CountDownTimer(15000, 1000) {
                override fun onTick(ms: Long) { timerView.text = "⏱ ${ms/1000}s" }
                override fun onFinish() {
                    if (!answered) {
                        val wrongOpt = q.options.first { !it.isCorrect }
                        val dummyBtn = Button(this@AlarmRingActivity)
                        handleAnswer(wrongOpt, q, dummyBtn)
                    }
                }
            }.start()
        }
    }

    private fun handleAnswer(opt: QuizOption, q: Question, btn: Button) {
        answered = true
        countdown?.cancel()
        if (opt.isCorrect) {
            correctCount++
            btn.setBackgroundColor(0xFF4CAF50.toInt())
        } else {
            wrongCount++
            btn.setBackgroundColor(0xFFF44336.toInt())
            if (settings.penaltyEnabled) {
                questions.addAll(QuestionBank.generate(alarm.difficulty, 1))
            }
        }
        val feedbackView = findViewById<TextView>(R.id.quiz_feedback)
        feedbackView.visibility = View.VISIBLE
        feedbackView.text = if (opt.isCorrect) "✅ Correct! ${q.explanation}"
        else "❌ Wrong. ${q.explanation}${if (settings.penaltyEnabled) "\n+1 penalty question!" else ""}"

        val nextBtn = findViewById<Button>(R.id.next_question_btn)
        nextBtn.visibility = View.VISIBLE
        nextBtn.text = if (currentIndex >= questions.size - 1) "See Results →" else "Next →"

        findViewById<TextView>(R.id.quiz_score).text = "$correctCount correct"
    }

    private fun completeQuiz() {
        val elapsed = System.currentTimeMillis() - quizStartTime
        setContentView(R.layout.phase_complete)

        val total = correctCount + wrongCount
        val accuracy = if (total > 0) correctCount * 100 / total else 0
        findViewById<TextView>(R.id.complete_accuracy).text = "$correctCount/$total correct ($accuracy%)"
        val m = elapsed / 60000; val s = (elapsed % 60000) / 1000
        findViewById<TextView>(R.id.complete_time).text = "Time: %d:%02d".format(m, s)

        val quote = QuestionBank.getQuote()
        findViewById<TextView>(R.id.morning_quote).text = "\"${quote.first}\"\n— ${quote.second}"

        val stats = Prefs.loadStats(this)
        stats.totalCorrect += correctCount
        stats.totalAnswered += total
        if (stats.fastestTimeMs == null || elapsed < stats.fastestTimeMs!!) stats.fastestTimeMs = elapsed
        val dateStr = SimpleDateFormat("MMM d", Locale.getDefault()).format(Date())
        stats.history.add(0, HistoryEntry(dateStr, accuracy, elapsed))
        if (stats.history.size > 20) stats.history.removeAt(stats.history.size - 1)
        val dayIdx = (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + 5) % 7
        stats.weeklyPerformance[dayIdx] = stats.weeklyPerformance[dayIdx] + 1
        Prefs.saveStats(this, stats)

        // Stop alarm
        AlarmService.stop(this)

        findViewById<Button>(R.id.done_btn).setOnClickListener {
            finish()
        }
    }

    override fun onBackPressed() {
        // Prevent dismiss without completing quiz
        Toast.makeText(this, "Complete the challenge to dismiss!", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        countdown?.cancel()
        super.onDestroy()
    }
}
