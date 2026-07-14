package org.example.project.notifications

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import org.example.project.domain.calls.AndroidCallRuntime

class IncomingCallRingingService : Service() {
    private var ringtone: Ringtone? = null
    private val handler = Handler(Looper.getMainLooper())
    private val timeoutRunnable = Runnable { stopSelf() }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val payload = IncomingCallPayload.fromIntent(intent)
        if (payload == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        NotificationChannels.create(this)
        AndroidCallRuntime.initialize(this)
        AndroidCallRuntime.reportIncoming(
            payload.callId,
            payload.callerUserId,
            payload.callerName,
            payload.isVideo,
        )

        val notification = IncomingCallNotificationFactory.build(this, payload)
        try {
            startForeground(notificationId(payload.callId), notification)
        } catch (_: RuntimeException) {
            getSystemService(NotificationManager::class.java)
                ?.notify(notificationId(payload.callId), notification)
            stopSelf()
            return START_NOT_STICKY
        }
        startRinging()

        handler.removeCallbacks(timeoutRunnable)
        handler.postDelayed(timeoutRunnable, RING_TIMEOUT_MS)

        return START_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacks(timeoutRunnable)
        stopRinging()
        val callId = currentCallId
        if (!callId.isNullOrBlank()) {
            getSystemService(NotificationManager::class.java)?.cancel(notificationId(callId))
        }
        currentCallId = null
        super.onDestroy()
    }

    private fun startRinging() {
        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        ringtone = RingtoneManager.getRingtone(this, uri)?.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                isLooping = true
                audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .build()
            }
            play()
        }
        vibrate()
    }

    private fun vibrate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibrator = getSystemService(VibratorManager::class.java)?.defaultVibrator
            vibrator?.vibrate(
                VibrationEffect.createWaveform(longArrayOf(0, 500, 500), 0),
            )
        } else {
            @Suppress("DEPRECATION")
            val vibrator = getSystemService(VIBRATOR_SERVICE) as? Vibrator
            @Suppress("DEPRECATION")
            vibrator?.vibrate(longArrayOf(0, 500, 500), 0)
        }
    }

    private fun stopRinging() {
        ringtone?.stop()
        ringtone = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getSystemService(VibratorManager::class.java)?.defaultVibrator?.cancel()
        } else {
            @Suppress("DEPRECATION")
            (getSystemService(VIBRATOR_SERVICE) as? Vibrator)?.cancel()
        }
    }

    companion object {
        private const val RING_TIMEOUT_MS = 45_000L

        private var currentCallId: String? = null

        fun start(context: Context, data: Map<String, String>) {
            val payload = IncomingCallPayload.fromData(data) ?: return
            currentCallId = payload.callId
            val intent = Intent(context, IncomingCallRingingService::class.java).apply {
                payload.putInto(this)
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (_: RuntimeException) {
                NotificationChannels.create(context)
                context.getSystemService(NotificationManager::class.java)
                    ?.notify(notificationId(payload.callId), IncomingCallNotificationFactory.build(context, payload))
            }
        }

        fun stop(context: Context, callId: String?) {
            if (callId.isNullOrBlank()) return
            context.stopService(Intent(context, IncomingCallRingingService::class.java))
            context.getSystemService(NotificationManager::class.java)?.cancel(notificationId(callId))
        }

        private fun notificationId(callId: String): Int = ("ring-$callId").hashCode()
    }
}
