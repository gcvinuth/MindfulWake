package com.mindfulwake.app.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mindfulwake.app.data.Prefs

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        val alarmId = intent.getStringExtra("alarm_id") ?: return
        val alarms = Prefs.loadAlarms(ctx)
        val alarm = alarms.find { it.id == alarmId } ?: return
        if (!alarm.enabled) return

        // Reschedule for next occurrence
        AlarmScheduler.schedule(ctx, alarm)

        // Start foreground service to ring
        val serviceIntent = Intent(ctx, AlarmService::class.java).apply {
            putExtra("alarm_id", alarmId)
        }
        ctx.startForegroundService(serviceIntent)
    }
}
