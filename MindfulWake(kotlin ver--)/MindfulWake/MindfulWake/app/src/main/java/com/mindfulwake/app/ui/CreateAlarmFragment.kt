package com.mindfulwake.app.ui

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.mindfulwake.app.R
import com.mindfulwake.app.alarm.AlarmScheduler
import com.mindfulwake.app.data.Alarm
import com.mindfulwake.app.data.Prefs

class CreateAlarmFragment : Fragment() {

    private var hour = 7
    private var minute = 0
    private var ampm = "AM"
    private var questionCount = 3
    private var difficulty = "medium"
    private val selectedDays = mutableListOf(1, 2, 3, 4, 5) // Mon-Fri

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_create_alarm, container, false)

        val hourDisplay = view.findViewById<TextView>(R.id.hour_display)
        val minuteDisplay = view.findViewById<TextView>(R.id.minute_display)
        val amBtn = view.findViewById<Button>(R.id.btn_am)
        val pmBtn = view.findViewById<Button>(R.id.btn_pm)
        val labelInput = view.findViewById<EditText>(R.id.alarm_label_input)
        val qCountSlider = view.findViewById<SeekBar>(R.id.question_count_slider)
        val qCountDisplay = view.findViewById<TextView>(R.id.question_count_display)
        val saveBtn = view.findViewById<Button>(R.id.save_alarm_btn)

        fun updateTimeDisplay() {
            hourDisplay.text = "%02d".format(hour)
            minuteDisplay.text = "%02d".format(minute)
        }

        view.findViewById<Button>(R.id.hour_up).setOnClickListener {
            hour = (hour % 12) + 1; updateTimeDisplay()
        }
        view.findViewById<Button>(R.id.hour_down).setOnClickListener {
            hour = (hour - 2 + 12) % 12 + 1; updateTimeDisplay()
        }
        view.findViewById<Button>(R.id.minute_up).setOnClickListener {
            minute = (minute + 5) % 60; updateTimeDisplay()
        }
        view.findViewById<Button>(R.id.minute_down).setOnClickListener {
            minute = (minute - 5 + 60) % 60; updateTimeDisplay()
        }

        fun updateAmPm() {
            amBtn.alpha = if (ampm == "AM") 1.0f else 0.4f
            pmBtn.alpha = if (ampm == "PM") 1.0f else 0.4f
        }
        amBtn.setOnClickListener { ampm = "AM"; updateAmPm() }
        pmBtn.setOnClickListener { ampm = "PM"; updateAmPm() }
        updateAmPm()

        qCountSlider.max = 9
        qCountSlider.progress = 2 // = 3 questions (1-indexed)
        qCountDisplay.text = "3 questions"
        qCountSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                questionCount = progress + 1
                qCountDisplay.text = "$questionCount question${if (questionCount > 1) "s" else ""}"
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        // Difficulty buttons
        val diffBtns = mapOf(
            "easy" to view.findViewById<Button>(R.id.btn_easy),
            "medium" to view.findViewById<Button>(R.id.btn_medium),
            "hard" to view.findViewById<Button>(R.id.btn_hard)
        )
        fun updateDiff() {
            diffBtns.forEach { (diff, btn) -> btn.alpha = if (diff == difficulty) 1.0f else 0.4f }
        }
        diffBtns.forEach { (diff, btn) ->
            btn.setOnClickListener { difficulty = diff; updateDiff() }
        }
        updateDiff()

        // Day buttons
        val dayIds = mapOf(
            0 to view.findViewById<ToggleButton>(R.id.btn_sun),
            1 to view.findViewById<ToggleButton>(R.id.btn_mon),
            2 to view.findViewById<ToggleButton>(R.id.btn_tue),
            3 to view.findViewById<ToggleButton>(R.id.btn_wed),
            4 to view.findViewById<ToggleButton>(R.id.btn_thu),
            5 to view.findViewById<ToggleButton>(R.id.btn_fri),
            6 to view.findViewById<ToggleButton>(R.id.btn_sat)
        )
        dayIds.forEach { (day, btn) ->
            btn.isChecked = selectedDays.contains(day)
            btn.setOnCheckedChangeListener { _, checked ->
                if (checked) { if (!selectedDays.contains(day)) selectedDays.add(day) }
                else selectedDays.remove(day)
            }
        }

        saveBtn.setOnClickListener {
            val alarm = Alarm(
                id = System.currentTimeMillis().toString(36) + (Math.random() * 1000).toInt().toString(36),
                hour = hour, minute = minute, ampm = ampm,
                label = labelInput.text.toString().trim().ifEmpty { "Morning Alarm" },
                questionCount = questionCount,
                difficulty = difficulty,
                days = selectedDays.toList(),
                enabled = true
            )
            val alarms = Prefs.loadAlarms(requireContext())
            alarms.add(alarm)
            Prefs.saveAlarms(requireContext(), alarms)
            AlarmScheduler.schedule(requireContext(), alarm)
            Toast.makeText(requireContext(), "Alarm set for ${alarm.displayTime()}", Toast.LENGTH_SHORT).show()
            labelInput.text.clear()
        }

        updateTimeDisplay()
        return view
    }
}
