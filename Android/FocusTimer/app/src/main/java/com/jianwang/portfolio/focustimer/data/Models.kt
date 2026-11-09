package com.jianwang.portfolio.focustimer.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TimerPhase { FOCUS, SHORT_BREAK, LONG_BREAK }

@Entity(tableName = "focus_sessions")
data class FocusSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val phase: String,
    val plannedSeconds: Int,
    val completedSeconds: Int,
    val completedAt: Long,
    val wasCompleted: Boolean
)

data class TimerSettings(
    val focusMinutes: Int = 25,
    val shortBreakMinutes: Int = 5,
    val longBreakMinutes: Int = 15,
    val sessionsUntilLongBreak: Int = 4,
    val autoStartNext: Boolean = false,
    val hapticsEnabled: Boolean = true,
    val soundEnabled: Boolean = true
)
