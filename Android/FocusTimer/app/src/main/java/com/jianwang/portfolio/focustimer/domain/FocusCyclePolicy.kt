package com.jianwang.portfolio.focustimer.domain

import com.jianwang.portfolio.focustimer.data.TimerPhase

object FocusCyclePolicy {
    fun nextPhase(
        current: TimerPhase,
        completedFocusCount: Int,
        sessionsUntilLongBreak: Int
    ): TimerPhase {
        return when (current) {
            TimerPhase.FOCUS -> {
                if (completedFocusCount > 0 && completedFocusCount % sessionsUntilLongBreak == 0) {
                    TimerPhase.LONG_BREAK
                } else {
                    TimerPhase.SHORT_BREAK
                }
            }
            TimerPhase.SHORT_BREAK, TimerPhase.LONG_BREAK -> TimerPhase.FOCUS
        }
    }
}
