package com.mindfulwake.app.ui

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.mindfulwake.app.R
import com.mindfulwake.app.data.Prefs

class SettingsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        val settings = Prefs.loadSettings(requireContext())

        val soundSpinner = view.findViewById<Spinner>(R.id.sound_spinner)
        val snoozeToggle = view.findViewById<Switch>(R.id.snooze_toggle)
        val penaltyToggle = view.findViewById<Switch>(R.id.penalty_toggle)
        val timerToggle = view.findViewById<Switch>(R.id.timer_toggle)
        val autoAdjustToggle = view.findViewById<Switch>(R.id.auto_adjust_toggle)
        val gradualToggle = view.findViewById<Switch>(R.id.gradual_toggle)
        val bedtimeInput = view.findViewById<EditText>(R.id.bedtime_input)
        val bedtimeOffsetInput = view.findViewById<EditText>(R.id.bedtime_offset_input)
        val agendaInput = view.findViewById<EditText>(R.id.agenda_input)
        val saveBtn = view.findViewById<Button>(R.id.save_settings_btn)

        val soundOptions = arrayOf("classic", "gentle", "nature", "urgent")
        val soundLabels = arrayOf("Classic", "Gentle", "Nature", "Urgent")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, soundLabels)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        soundSpinner.adapter = adapter
        soundSpinner.setSelection(soundOptions.indexOf(settings.sound).coerceAtLeast(0))

        snoozeToggle.isChecked = settings.allowSnooze
        penaltyToggle.isChecked = settings.penaltyEnabled
        timerToggle.isChecked = settings.timerEnabled
        autoAdjustToggle.isChecked = settings.autoAdjust
        gradualToggle.isChecked = settings.gradualVolume
        bedtimeInput.setText(settings.bedtimeTarget)
        bedtimeOffsetInput.setText(settings.bedtimeOffsetHours.toString())
        agendaInput.setText(settings.agenda)

        saveBtn.setOnClickListener {
            settings.sound = soundOptions[soundSpinner.selectedItemPosition]
            settings.allowSnooze = snoozeToggle.isChecked
            settings.penaltyEnabled = penaltyToggle.isChecked
            settings.timerEnabled = timerToggle.isChecked
            settings.autoAdjust = autoAdjustToggle.isChecked
            settings.gradualVolume = gradualToggle.isChecked
            settings.bedtimeTarget = bedtimeInput.text.toString().ifEmpty { "22:30" }
            settings.bedtimeOffsetHours = bedtimeOffsetInput.text.toString().toIntOrNull() ?: 1
            settings.agenda = agendaInput.text.toString()
            Prefs.saveSettings(requireContext(), settings)
            Toast.makeText(requireContext(), "Settings saved ✓", Toast.LENGTH_SHORT).show()
        }

        return view
    }
}
