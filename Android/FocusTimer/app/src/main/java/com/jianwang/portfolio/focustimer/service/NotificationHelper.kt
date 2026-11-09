package com.jianwang.portfolio.focustimer.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.jianwang.portfolio.focustimer.FocusTimerApp
import com.jianwang.portfolio.focustimer.MainActivity
import com.jianwang.portfolio.focustimer.R
import com.jianwang.portfolio.focustimer.data.TimerPhase

object NotificationHelper {
    fun notifySessionEnd(context: Context, phase: TimerPhase) {
        val title = when (phase) {
            TimerPhase.FOCUS -> "Focus complete"
            TimerPhase.SHORT_BREAK -> "Break over"
            TimerPhase.LONG_BREAK -> "Long break over"
        }
        val body = when (phase) {
            TimerPhase.FOCUS -> "Nice work — time for a break."
            else -> "Ready to focus again?"
        }
        val intent = Intent(context, MainActivity::class.java)
        val pi = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, FocusTimerApp.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(pi)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        val nm = context.getSystemService(NotificationManager::class.java)
        nm.notify(1001, notification)
    }
}
