package com.jianwang.portfolio.focustimer

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.jianwang.portfolio.focustimer.data.AppDatabase
import com.jianwang.portfolio.focustimer.data.SettingsRepository

class FocusTimerApp : Application() {
    lateinit var database: AppDatabase
        private set
    lateinit var settingsRepository: SettingsRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.get(this)
        settingsRepository = SettingsRepository(this)
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Focus Sessions",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when a focus or break session ends"
            }
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "focus_sessions"
    }
}
