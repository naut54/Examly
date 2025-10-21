package com.octal.examly.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.octal.examly.presentation.ui.activity.MainActivity
import com.octal.examly.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TimerService : Service() {

    private val binder = TimerBinder()
    private var countDownTimer: CountDownTimer? = null

    private val _timeRemaining = MutableStateFlow(0L)
    val timeRemaining: StateFlow<Long> = _timeRemaining.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _isFinished = MutableStateFlow(false)
    val isFinished: StateFlow<Boolean> = _isFinished.asStateFlow()

    inner class TimerBinder : Binder() {
        fun getService(): TimerService = this@TimerService
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    fun startTimer(durationMillis: Long) {
        stopTimer()

        _timeRemaining.value = durationMillis
        _isRunning.value = true
        _isFinished.value = false

        countDownTimer = object : CountDownTimer(durationMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _timeRemaining.value = millisUntilFinished
                updateNotification(millisUntilFinished)
            }

            override fun onFinish() {
                _timeRemaining.value = 0L
                _isRunning.value = false
                _isFinished.value = true
                showTimeUpNotification()
            }
        }.start()

        startForeground(NOTIFICATION_ID, createNotification(_timeRemaining.value))
    }

    fun pauseTimer() {
        countDownTimer?.cancel()
        _isRunning.value = false
    }

    fun resumeTimer() {
        if (_timeRemaining.value > 0 && !_isRunning.value) {
            startTimer(_timeRemaining.value)
        }
    }

    fun stopTimer() {
        countDownTimer?.cancel()
        countDownTimer = null
        _timeRemaining.value = 0L
        _isRunning.value = false
        _isFinished.value = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Timer Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Temporizador de examen en curso"
                setSound(null, null)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(timeRemaining: Long) = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("Examen en curso")
        .setContentText(formatTime(timeRemaining))
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setOngoing(true)
        .setContentIntent(createPendingIntent())
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setSound(null)
        .build()

    private fun updateNotification(timeRemaining: Long) {
        val notification = createNotification(timeRemaining)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun showTimeUpNotification() {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("¡Tiempo agotado!")
            .setContentText("El tiempo del examen ha finalizado")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(createPendingIntent())
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(TIME_UP_NOTIFICATION_ID, notification)

        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    private fun createPendingIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        return PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun formatTime(millis: Long): String {
        val totalSeconds = millis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return if (hours > 0) {
            String.format("Tiempo restante: %02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("Tiempo restante: %02d:%02d", minutes, seconds)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTimer()
    }

    companion object {
        private const val CHANNEL_ID = "timer_service_channel"
        private const val NOTIFICATION_ID = 1
        private const val TIME_UP_NOTIFICATION_ID = 2
    }
}