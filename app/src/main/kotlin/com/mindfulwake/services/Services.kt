package com.mindfulwake.services

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.*
import androidx.core.app.NotificationCompat
import com.mindfulwake.R
import com.mindfulwake.data.models.Alarm
import com.mindfulwake.data.repository.MindfulWakeDatabase
import com.mindfulwake.ui.screens.AlarmRingActivity
import kotlinx.coroutines.*
import java.util.Calendar

// ===== ALARM SCHEDULER =====
object AlarmScheduler {
    const val ACTION_TRIGGER = "com.mindfulwake.ALARM_TRIGGER"
    const val EXTRA_ALARM_ID = "alarm_id"
    const val CHANNEL_ID_ALARM = "mindfulwake_alarms"
    const val CHANNEL_ID_TIMER = "mindfulwake_timer"

    fun schedule(context: Context, alarm: Alarm) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerTime = getNextTriggerTime(alarm)

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_TRIGGER
            putExtra(EXTRA_ALARM_ID, alarm.id)
        }
        val pi = PendingIntent.getBroadcast(
            context, alarm.id, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(triggerTime, pi), pi)
            }
        } else {
            alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(triggerTime, pi), pi)
        }
    }

    fun cancel(context: Context, alarmId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_TRIGGER
        }
        val pi = PendingIntent.getBroadcast(
            context, alarmId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pi)
    }

    private fun getNextTriggerTime(alarm: Alarm): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, alarm.hour)
            set(Calendar.MINUTE, alarm.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (cal.timeInMillis <= System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
        // Handle repeat days
        if (alarm.repeatDays.isNotEmpty()) {
            while (!alarm.repeatDays.contains((cal.get(Calendar.DAY_OF_WEEK) + 5) % 7 + 1)) {
                cal.add(Calendar.DAY_OF_MONTH, 1)
            }
        }
        return cal.timeInMillis
    }

    fun createNotificationChannels(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val alarmChannel = NotificationChannel(
            CHANNEL_ID_ALARM, "Alarms",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "MindfulWake alarm notifications"
            setBypassDnd(true)
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 500, 200, 500)
            setSound(
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }

        val timerChannel = NotificationChannel(
            CHANNEL_ID_TIMER, "Timer & Stopwatch",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Timer and stopwatch ongoing notifications"
            setSound(null, null)
        }

        nm.createNotificationChannel(alarmChannel)
        nm.createNotificationChannel(timerChannel)
    }
}

// ===== ALARM RECEIVER =====
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getIntExtra(AlarmScheduler.EXTRA_ALARM_ID, -1)
        if (alarmId == -1) return

        // Start foreground alarm service
        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
}

// ===== BOOT RECEIVER =====
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_LOCKED_BOOT_COMPLETED) {
            CoroutineScope(Dispatchers.IO).launch {
                val db = MindfulWakeDatabase.getDatabase(context)
                val enabledAlarms = db.alarmDao().getEnabledAlarms()
                enabledAlarms.forEach { alarm ->
                    AlarmScheduler.schedule(context, alarm)
                }
            }
        }
    }
}

// ===== ALARM FOREGROUND SERVICE =====
class AlarmService : Service() {
    private var vibrator: Vibrator? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val alarmId = intent?.getIntExtra(AlarmScheduler.EXTRA_ALARM_ID, -1) ?: -1

        // Build notification
        val ringIntent = Intent(this, AlarmRingActivity::class.java).apply {
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pi = PendingIntent.getActivity(
            this, alarmId, ringIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, AlarmScheduler.CHANNEL_ID_ALARM)
            .setSmallIcon(R.drawable.ic_alarm)
            .setContentTitle("MindfulWake")
            .setContentText("Time to wake up! Answer questions to dismiss.")
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setFullScreenIntent(pi, true)
            .setContentIntent(pi)
            .setOngoing(true)
            .setAutoCancel(false)
            .build()

        startForeground(alarmId + 1000, notification)

        // Launch ring activity
        val activityIntent = Intent(this, AlarmRingActivity::class.java).apply {
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(activityIntent)

        // Vibrate
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 800, 400), 0))

        // Reschedule for repeat alarms
        scope.launch {
            val db = MindfulWakeDatabase.getDatabase(this@AlarmService)
            val alarm = db.alarmDao().getAlarmById(alarmId)
            alarm?.let {
                if (it.repeatDays.isNotEmpty()) {
                    AlarmScheduler.schedule(this@AlarmService, it)
                } else {
                    db.alarmDao().setAlarmEnabled(alarmId, false)
                }
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        vibrator?.cancel()
        scope.cancel()
        super.onDestroy()
    }

    fun stopAlarm() {
        vibrator?.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
}

// ===== TIMER SERVICE =====
class TimerService : Service() {
    private val binder = TimerBinder()
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    var timerMs: Long = 0L
    var remainingMs: Long = 0L
    var isTimerRunning = false
    var stopwatchMs: Long = 0L
    var isStopwatchRunning = false

    private var timerJob: Job? = null
    private var stopwatchJob: Job? = null

    inner class TimerBinder : Binder() {
        fun getService() = this@TimerService
    }

    override fun onBind(intent: Intent?) = binder
    override fun onCreate() { super.onCreate() }

    fun startTimer(durationMs: Long) {
        remainingMs = durationMs
        timerMs = durationMs
        isTimerRunning = true
        timerJob?.cancel()
        timerJob = scope.launch {
            while (remainingMs > 0 && isTimerRunning) {
                delay(100)
                remainingMs -= 100
                updateTimerNotification()
            }
            if (remainingMs <= 0) {
                onTimerFinished()
            }
        }
    }

    fun pauseTimer() { isTimerRunning = false; timerJob?.cancel() }
    fun resumeTimer() { startTimer(remainingMs) }
    fun resetTimer() { isTimerRunning = false; timerJob?.cancel(); remainingMs = timerMs }

    fun startStopwatch() {
        isStopwatchRunning = true
        stopwatchJob?.cancel()
        stopwatchJob = scope.launch {
            while (isStopwatchRunning) {
                delay(10)
                stopwatchMs += 10
            }
        }
    }

    fun pauseStopwatch() { isStopwatchRunning = false; stopwatchJob?.cancel() }
    fun resetStopwatch() { isStopwatchRunning = false; stopwatchJob?.cancel(); stopwatchMs = 0 }

    private fun onTimerFinished() {
        isTimerRunning = false
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(this, AlarmScheduler.CHANNEL_ID_TIMER)
            .setSmallIcon(R.drawable.ic_timer)
            .setContentTitle("Timer Finished!")
            .setContentText("Your MindfulWake timer is done.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        nm.notify(9999, notification)

        // Vibrate briefly
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            @Suppress("DEPRECATION") getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 300, 200, 300, 200, 600), -1))
    }

    private fun updateTimerNotification() {
        val mins = remainingMs / 60000
        val secs = (remainingMs % 60000) / 1000
        val notification = NotificationCompat.Builder(this, AlarmScheduler.CHANNEL_ID_TIMER)
            .setSmallIcon(R.drawable.ic_timer)
            .setContentTitle("Timer Running")
            .setContentText(String.format("%02d:%02d remaining", mins, secs))
            .setOngoing(true)
            .setSilent(true)
            .build()
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(8888, notification)
    }

    override fun onDestroy() { scope.cancel(); super.onDestroy() }
}