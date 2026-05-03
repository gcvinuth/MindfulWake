package com.mindfulwake.app.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mindfulwake.app.data.Prefs

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val alarms = Prefs.loadAlarms(ctx)
            AlarmScheduler.scheduleAll(ctx, alarms)
        }
    }
}
