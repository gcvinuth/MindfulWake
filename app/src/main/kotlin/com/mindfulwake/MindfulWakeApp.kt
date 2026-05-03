package com.mindfulwake

import android.app.Application
import com.mindfulwake.services.AlarmScheduler

class MindfulWakeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AlarmScheduler.createNotificationChannels(this)
    }
}