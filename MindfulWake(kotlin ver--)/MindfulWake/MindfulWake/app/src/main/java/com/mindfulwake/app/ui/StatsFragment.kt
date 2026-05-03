package com.mindfulwake.app.ui

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.mindfulwake.app.R
import com.mindfulwake.app.data.Prefs

class StatsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_stats, container, false)
        val stats = Prefs.loadStats(requireContext())

        view.findViewById<TextView>(R.id.streak_value).text = "${stats.streak}"

        val acc = if (stats.totalAnswered > 0)
            "${(stats.totalCorrect * 100 / stats.totalAnswered)}%"
        else "0%"
        view.findViewById<TextView>(R.id.accuracy_value).text = acc

        val fastestText = stats.fastestTimeMs?.let {
            val m = it / 60000; val s = (it % 60000) / 1000
            "%d:%02d".format(m, s)
        } ?: "--:--"
        view.findViewById<TextView>(R.id.fastest_value).text = fastestText

        val historyList = view.findViewById<LinearLayout>(R.id.history_list)
        historyList.removeAllViews()
        if (stats.history.isEmpty()) {
            val tv = TextView(requireContext()).apply { text = "No sessions yet." }
            historyList.addView(tv)
        } else {
            stats.history.take(5).forEach { h ->
                val tv = TextView(requireContext()).apply {
                    text = "${h.date}  •  ${h.accuracy}%  •  ${h.timeMs / 1000}s"
                    setPadding(0, 8, 0, 8)
                }
                historyList.addView(tv)
            }
        }

        return view
    }
}
