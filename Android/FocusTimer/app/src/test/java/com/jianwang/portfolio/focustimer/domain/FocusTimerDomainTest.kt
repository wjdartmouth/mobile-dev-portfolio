package com.jianwang.portfolio.focustimer.domain

import com.jianwang.portfolio.focustimer.data.TimerPhase
import org.junit.Assert.assertEquals
import org.junit.Test

class FocusCyclePolicyTest {
    @Test
    fun shortBreakUntilLongBreakCadence() {
        assertEquals(TimerPhase.SHORT_BREAK, FocusCyclePolicy.nextPhase(TimerPhase.FOCUS, 1, 4))
        assertEquals(TimerPhase.SHORT_BREAK, FocusCyclePolicy.nextPhase(TimerPhase.FOCUS, 2, 4))
        assertEquals(TimerPhase.SHORT_BREAK, FocusCyclePolicy.nextPhase(TimerPhase.FOCUS, 3, 4))
    }

    @Test
    fun longBreakWhenCompletedFocusCountHitsCadence() {
        assertEquals(TimerPhase.LONG_BREAK, FocusCyclePolicy.nextPhase(TimerPhase.FOCUS, 4, 4))
        assertEquals(TimerPhase.LONG_BREAK, FocusCyclePolicy.nextPhase(TimerPhase.FOCUS, 8, 4))
    }

    @Test
    fun zeroCompletedFocusCountStaysOnShortBreak() {
        assertEquals(TimerPhase.SHORT_BREAK, FocusCyclePolicy.nextPhase(TimerPhase.FOCUS, 0, 4))
    }

    @Test
    fun everyFocusIsALongBreakWhenCadenceIsOne() {
        assertEquals(TimerPhase.LONG_BREAK, FocusCyclePolicy.nextPhase(TimerPhase.FOCUS, 1, 1))
    }

    @Test
    fun anyBreakReturnsToFocus() {
        assertEquals(TimerPhase.FOCUS, FocusCyclePolicy.nextPhase(TimerPhase.SHORT_BREAK, 4, 4))
        assertEquals(TimerPhase.FOCUS, FocusCyclePolicy.nextPhase(TimerPhase.LONG_BREAK, 4, 4))
    }
}

class TimerMathTest {
    @Test
    fun secondsScaleMinutesBySixty() {
        assertEquals(25 * 60, TimerMath.secondsFor(TimerPhase.FOCUS, 25, 5, 15))
        assertEquals(5 * 60, TimerMath.secondsFor(TimerPhase.SHORT_BREAK, 25, 5, 15))
        assertEquals(15 * 60, TimerMath.secondsFor(TimerPhase.LONG_BREAK, 25, 5, 15))
    }

    @Test
    fun zeroMinutePhaseIsZeroSeconds() {
        assertEquals(0, TimerMath.secondsFor(TimerPhase.FOCUS, 0, 5, 15))
    }

    @Test
    fun clockDisplayStaysZeroPadded() {
        assertEquals("00:00", TimerMath.clockDisplay(0))
        assertEquals("01:05", TimerMath.clockDisplay(65))
        assertEquals("61:00", TimerMath.clockDisplay(3660))
    }

    @Test
    fun durationFormattingMatchesSharedPolicy() {
        assertEquals("0:00", TimerMath.formatDuration(0))
        assertEquals("1:05", TimerMath.formatDuration(65))
        assertEquals("0:59", TimerMath.formatDuration(59))
        assertEquals("1h 00m", TimerMath.formatDuration(3600))
        assertEquals("1h 01m", TimerMath.formatDuration(3660))
    }
}
