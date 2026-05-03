package com.mindfulwake.app.ui

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.mindfulwake.app.R
import com.mindfulwake.app.alarm.AlarmScheduler
import com.mindfulwake.app.data.Alarm
import com.mindfulwake.app.data.Prefs

class AlarmsFragment : Fragment() {

    private lateinit var listView: ListView
    private lateinit var emptyView: TextView
    private val alarms = mutableListOf<Alarm>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_alarms, container, false)
        listView = view.findViewById(R.id.alarm_list)
        emptyView = view.findViewById(R.id.empty_view)
        return view
    }

    override fun onResume() {
        super.onResume()
        loadAndRender()
    }

    private fun loadAndRender() {
        alarms.clear()
        alarms.addAll(Prefs.loadAlarms(requireContext()))
        renderAlarms()
    }

    private fun renderAlarms() {
        if (alarms.isEmpty()) {
            listView.visibility = View.GONE
            emptyView.visibility = View.VISIBLE
        } else {
            listView.visibility = View.VISIBLE
            emptyView.visibility = View.GONE
            val adapter = AlarmAdapter(requireContext(), alarms,
                onToggle = { alarm ->
                    alarm.enabled = !alarm.enabled
                    Prefs.saveAlarms(requireContext(), alarms)
                    if (alarm.enabled) AlarmScheduler.schedule(requireContext(), alarm)
                    else AlarmScheduler.cancel(requireContext(), alarm.id)
                    renderAlarms()
                },
                onDelete = { alarm ->
                    AlarmScheduler.cancel(requireContext(), alarm.id)
                    alarms.remove(alarm)
                    Prefs.saveAlarms(requireContext(), alarms)
                    renderAlarms()
                    Toast.makeText(requireContext(), "Alarm deleted", Toast.LENGTH_SHORT).show()
                }
            )
            listView.adapter = adapter
        }
    }
}

class AlarmAdapter(
    private val ctx: android.content.Context,
    private val alarms: List<Alarm>,
    private val onToggle: (Alarm) -> Unit,
    private val onDelete: (Alarm) -> Unit
) : BaseAdapter() {

    override fun getCount() = alarms.size
    override fun getItem(pos: Int) = alarms[pos]
    override fun getItemId(pos: Int) = pos.toLong()

    override fun getView(pos: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(ctx).inflate(R.layout.item_alarm, parent, false)
        val alarm = alarms[pos]

        view.findViewById<TextView>(R.id.alarm_time).text = alarm.displayTime()
        view.findViewById<TextView>(R.id.alarm_label).text = alarm.label
        view.findViewById<TextView>(R.id.alarm_days).text = getDayNames(alarm.days)
        view.findViewById<TextView>(R.id.alarm_badges).text =
            "${alarm.questionCount}Q · ${alarm.difficulty.replaceFirstChar { it.uppercase() }}"

        val toggle = view.findViewById<Switch>(R.id.alarm_toggle)
        toggle.isChecked = alarm.enabled
        toggle.setOnCheckedChangeListener(null)
        toggle.setOnCheckedChangeListener { _, _ -> onToggle(alarm) }

        view.findViewById<ImageButton>(R.id.alarm_delete).setOnClickListener { onDelete(alarm) }
        view.alpha = if (alarm.enabled) 1.0f else 0.5f
        return view
    }

    private fun getDayNames(days: List<Int>): String {
        val names = listOf("Sun","Mon","Tue","Wed","Thu","Fri","Sat")
        if (days.size == 7) return "Every day"
        if (days.size == 5 && listOf(1,2,3,4,5).all { days.contains(it) }) return "Weekdays"
        if (days.size == 2 && listOf(0,6).all { days.contains(it) }) return "Weekends"
        return days.sorted().joinToString(", ") { names[it] }
    }
}
