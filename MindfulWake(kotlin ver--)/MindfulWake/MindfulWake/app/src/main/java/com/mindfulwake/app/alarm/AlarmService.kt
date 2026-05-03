package com.mindfulwake.app.alarm

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.*
import android.os.VibrationEffect
import android.util.Log
import androidx.core.app.NotificationCompat
import com.mindfulwake.app.R
import com.mindfulwake.app.data.Prefs
import com.mindfulwake.app.ui.AlarmRingActivity

class AlarmService : Service() {

    companion object {
        const val CHANNEL_ID = "mindfulwake_alarm"
        const val NOTIF_ID = 101
        var currentAlarmId: String? = null

        fun stop(ctx: Context) {
            ctx.stopService(Intent(ctx, AlarmService::class.java))
        }
    }

    private var toneGenerator: ToneGenerator? = null
    private var vibrator: Vibrator? = null
    private var handler = Handler(Looper.getMainLooper())
    private var ringing = false

    override fun onBind(intent: Intent?) = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val alarmId = intent?.getStringExtra("alarm_id") ?: return START_NOT_STICKY
        currentAlarmId = alarmId

        val alarms = Prefs.loadAlarms(this)
        val alarm = alarms.find { it.id == alarmId }

        val notif = buildNotification(alarm?.label ?: "Alarm", alarm?.displayTime() ?: "")
        startForeground(NOTIF_ID, notif)

        // Launch ring activity
        val ringIntent = Intent(this, AlarmRingActivity::class.java).apply {
            putExtra("alarm_id", alarmId)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(ringIntent)

        startRinging()
        return START_STICKY
    }

    private fun startRinging() {
        ringing = true
        val settings = Prefs.loadSettings(this)

        // Vibrate
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        val vibPattern = longArrayOf(0, 500, 200, 500, 200, 500)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(vibPattern, 0))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(vibPattern, 0)
        }

        // Tone
        try {
            val stream = AudioManager.STREAM_ALARM
            toneGenerator = ToneGenerator(stream, 100)
            playToneLoop()
        } catch (e: Exception) {
            Log.w("AlarmService", "ToneGenerator error: $e")
        }
    }

    private fun playToneLoop() {
        if (!ringing) return
        toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 1500)
        handler.postDelayed({ playToneLoop() }, 2000)
    }

    override fun onDestroy() {
        ringing = false
        handler.removeCallbacksAndMessages(null)
        try { toneGenerator?.stopTone(); toneGenerator?.release() } catch (e: Exception) {}
        try { vibrator?.cancel() } catch (e: Exception) {}
        currentAlarmId = null
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "MindfulWake Alarms",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alarm notifications"
                enableVibration(true)
                setBypassDnd(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(label: String, time: String): Notification {
        val tapIntent = Intent(this, AlarmRingActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val pi = PendingIntent.getActivity(this, 0, tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("⏰ MindfulWake")
            .setContentText("$time — $label")
            .setSmallIcon(R.drawable.ic_alarm)
            .setContentIntent(pi)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setFullScreenIntent(pi, true)
            .setOngoing(true)
            .setAutoCancel(false)
            .build()
    }
}
