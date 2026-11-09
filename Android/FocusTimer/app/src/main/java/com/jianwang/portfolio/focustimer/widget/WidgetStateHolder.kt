package com.jianwang.portfolio.focustimer.widget

object WidgetStateHolder {
    @Volatile var phase: String = "FOCUS"
    @Volatile var remainingSeconds: Int = 25 * 60
    @Volatile var isRunning: Boolean = false
    @Volatile var timeDisplay: String = "25:00"

    fun update(phase: String, remaining: Int, running: Boolean, display: String) {
        this.phase = phase
        remainingSeconds = remaining
        isRunning = running
        timeDisplay = display
    }
}
