package com.traceboard.app.service

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.content.pm.ServiceInfo
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.traceboard.app.R
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ClipboardManager
import com.traceboard.app.MainActivity
import com.traceboard.app.data.repository.ClipboardRepository
import com.traceboard.app.data.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ClipboardRecordingService : Service() {

    companion object {
        private const val ACTION_STOP = "com.traceboard.app.STOP_CLIPBOARD_RECORDING"
        private const val CHANNEL_ID = "clipboard_recording"

        fun start(context: Context) {
            val intent = Intent(context, ClipboardRecordingService::class.java)
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, ClipboardRecordingService::class.java))
        }
    }

    private val job = Job()
    private val scope = CoroutineScope(job + Dispatchers.IO)
    private lateinit var repo: ClipboardRepository
    private lateinit var settings: SettingsRepository
    private var pollingStarted = false

    override fun onCreate() {
        super.onCreate()
        repo = ClipboardRepository(this)
        settings = SettingsRepository(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }
        startAsForeground()
        if (!pollingStarted) {
            pollingStarted = true
            scope.launch { pollLoop() }
        }
        return START_STICKY
    }

    private suspend fun pollLoop() {
        while (true) {
            if (!settings.isRecording.first()) {
                stopSelf()
                return
            }
            val cm = getSystemService(ClipboardManager::class.java)
            repo.addFromClipboard(cm)
            delay(1500)
        }
    }

    private fun startAsForeground() {
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceCompat.startForeground(
                this,
                1,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(1, notification)
        }
    }

    private fun buildNotification(): Notification {
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "Perekaman Clipboard",
                NotificationManager.IMPORTANCE_LOW
            )
        )
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val stopIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, ClipboardRecordingService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Traceboard merekam clipboard")
            .setContentText("Sedang memantau teks yang disalin.")
            .setContentIntent(contentIntent)
            .addAction(0, "Berhenti", stopIntent)
            .setOngoing(true)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }
}