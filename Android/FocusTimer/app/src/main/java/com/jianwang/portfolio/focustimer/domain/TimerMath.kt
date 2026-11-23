package com.jianwang.portfolio.focustimer.domain

import com.jianwang.portfolio.focustimer.data.TimerPhase

object TimerMath {
    fun secondsFor(
        phase: TimerPhase,
        focusMinutes: Int,
        shortBreakMinutes: Int,
        longBreakMinutes: Int
    ): Int = when (phase) {
        TimerPhase.FOCUS -> focusMinutes * 60
        TimerPhase.SHORT_BREAK -> shortBreakMinutes * 60
        TimerPhase.LONG_BREAK -> longBreakMinutes * 60
    }

    /** Live countdown. `65` → `01:05`. Matches the previous ViewModel clock. */
    fun clockDisplay(totalSeconds: Int): String =
        "%02d:%02d".format(totalSeconds / 60, totalSeconds % 60)

    /** Shared duration copy. `0` → `0:00`, `65` → `1:05`, `3660` → `1h 01m`. */
    fun formatDuration(seconds: Int): String {
        val minutes = seconds / 60
        val secs = seconds % 60
        if (minutes >= 60) {
            val hours = minutes / 60
            val remainingMinutes = minutes % 60
            return "%dh %02dm".format(hours, remainingMinutes)
        }
        return "%d:%02d".format(minutes, secs)
    }
}
