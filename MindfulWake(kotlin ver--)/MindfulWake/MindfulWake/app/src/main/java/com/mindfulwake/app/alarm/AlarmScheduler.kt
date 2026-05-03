package com.mindfulwake.app.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.mindfulwake.app.data.Alarm
import java.util.Calendar

object AlarmScheduler {

    private const val REQUEST_BASE = 1000

    fun schedule(ctx: Context, alarm: Alarm) {
        if (!alarm.enabled) return
        val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val next = nextFireTime(alarm) ?: return

        val intent = Intent(ctx, AlarmReceiver::class.java).apply {
            putExtra("alarm_id", alarm.id)
        }
        val pi = PendingIntent.getBroadcast(
            ctx,
            alarmRequestCode(alarm.id),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (am.canScheduleExactAlarms()) {
                am.setAlarmClock(AlarmManager.AlarmClockInfo(next, pi), pi)
            } else {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next, pi)
            }
        } else {
            am.setAlarmClock(AlarmManager.AlarmClockInfo(next, pi), pi)
        }
    }

    fun cancel(ctx: Context, alarmId: String) {
        val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(ctx, AlarmReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            ctx,
            alarmRequestCode(alarmId),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        ) ?: return
        am.cancel(pi)
    }

    fun scheduleAll(ctx: Context, alarms: List<Alarm>) {
        alarms.forEach { if (it.enabled) schedule(ctx, it) else cancel(ctx, it.id) }
    }

    fun nextFireTime(alarm: Alarm): Long? {
        val now = Calendar.getInstance()
        for (dayOffset in 0..7) {
            val candidate = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, dayOffset)
                set(Calendar.HOUR_OF_DAY, alarm.hour24())
                set(Calendar.MINUTE, alarm.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            if (candidate.timeInMillis <= now.timeInMillis) continue
            val dow = candidate.get(Calendar.DAY_OF_WEEK) - 1 // 0=Sun..6=Sat
            if (alarm.days.isNotEmpty() && !alarm.days.contains(dow)) continue
            return candidate.timeInMillis
        }
        return null
    }

    private fun alarmRequestCode(alarmId: String): Int =
        REQUEST_BASE + (alarmId.hashCode() and 0xFFFF)
}
