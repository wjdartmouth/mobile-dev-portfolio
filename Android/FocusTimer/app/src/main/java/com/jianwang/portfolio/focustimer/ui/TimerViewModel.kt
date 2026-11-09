package com.jianwang.portfolio.focustimer.ui

import android.app.Application
import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jianwang.portfolio.focustimer.FocusTimerApp
import com.jianwang.portfolio.focustimer.data.FocusSession
import com.jianwang.portfolio.focustimer.data.TimerPhase
import com.jianwang.portfolio.focustimer.data.TimerSettings
import com.jianwang.portfolio.focustimer.service.NotificationHelper
import com.jianwang.portfolio.focustimer.widget.FocusTimerWidget
import com.jianwang.portfolio.focustimer.widget.WidgetStateHolder
import java.util.Calendar
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TimerUiState(
    val phase: TimerPhase = TimerPhase.FOCUS,
    val remainingSeconds: Int = 25 * 60,
    val totalSeconds: Int = 25 * 60,
    val isRunning: Boolean = false,
    val completedFocusCount: Int = 0,
    val settings: TimerSettings = TimerSettings()
) {
    val progress: Float
        get() = if (totalSeconds == 0) 0f else 1f - remainingSeconds.toFloat() / totalSeconds
    val timeDisplay: String
        get() = "%02d:%02d".format(remainingSeconds / 60, remainingSeconds % 60)
}

class TimerViewModel(app: Application) : AndroidViewModel(app) {
    private val focusApp = app as FocusTimerApp
    private val db = focusApp.database
    private val settingsRepo = focusApp.settingsRepository

    private val _ui = MutableStateFlow(TimerUiState())
    val ui: StateFlow<TimerUiState> = _ui.asStateFlow()

    val history = db.sessions().observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private var tickJob: Job? = null
    private var elapsedInCurrent = 0

    init {
        viewModelScope.launch {
            settingsRepo.settings.collect { s ->
                val cur = _ui.value
                if (!cur.isRunning) {
                    val seconds = secondsFor(cur.phase, s)
                    _ui.value = cur.copy(
                        settings = s,
                        totalSeconds = seconds,
                        remainingSeconds = seconds
                    )
                    publishWidget()
                } else {
                    _ui.value = cur.copy(settings = s)
                }
            }
        }
        viewModelScope.launch {
            val start = startOfDayMillis()
            db.sessions().observeFocusCountToday(start).collect { count ->
                _ui.value = _ui.value.copy(completedFocusCount = count)
            }
        }
    }

    fun toggle() {
        if (_ui.value.isRunning) pause() else start()
    }

    fun start() {
        val state = _ui.value
        if (state.isRunning) return
        _ui.value = state.copy(isRunning = true)
        tickJob?.cancel()
        tickJob = viewModelScope.launch {
            while (true) {
                delay(1_000)
                tick()
            }
        }
        publishWidget()
    }

    fun pause() {
        tickJob?.cancel()
        tickJob = null
        _ui.value = _ui.value.copy(isRunning = false)
        publishWidget()
    }

    fun reset() {
        pause()
        elapsedInCurrent = 0
        val s = _ui.value
        val seconds = secondsFor(s.phase, s.settings)
        _ui.value = s.copy(remainingSeconds = seconds, totalSeconds = seconds)
        publishWidget()
    }

    fun skip() {
        viewModelScope.launch { completeCurrent(wasCompleted = false) }
    }

    fun updateSettings(transform: (TimerSettings) -> TimerSettings) {
        viewModelScope.launch { settingsRepo.update(transform) }
    }

    private suspend fun tick() {
        val s = _ui.value
        if (!s.isRunning) return
        if (s.remainingSeconds <= 1) {
            completeCurrent(wasCompleted = true)
        } else {
            elapsedInCurrent += 1
            _ui.value = s.copy(remainingSeconds = s.remainingSeconds - 1)
            if (s.remainingSeconds % 5 == 0) publishWidget()
        }
    }

    private suspend fun completeCurrent(wasCompleted: Boolean) {
        pause()
        val s = _ui.value
        val completedSeconds = if (wasCompleted) s.totalSeconds else elapsedInCurrent
        db.sessions().insert(
            FocusSession(
                phase = s.phase.name,
                plannedSeconds = s.totalSeconds,
                completedSeconds = completedSeconds.coerceAtLeast(0),
                completedAt = System.currentTimeMillis(),
                wasCompleted = wasCompleted && s.phase == TimerPhase.FOCUS
            )
        )
        if (wasCompleted) {
            NotificationHelper.notifySessionEnd(getApplication(), s.phase)
            if (s.settings.hapticsEnabled) vibrate()
        }
        elapsedInCurrent = 0
        val nextPhase = nextPhase(s.phase, s.completedFocusCount + if (s.phase == TimerPhase.FOCUS && wasCompleted) 1 else 0, s.settings)
        val seconds = secondsFor(nextPhase, s.settings)
        _ui.value = s.copy(
            phase = nextPhase,
            remainingSeconds = seconds,
            totalSeconds = seconds,
            isRunning = false
        )
        publishWidget()
        if (wasCompleted && s.settings.autoStartNext) start()
    }

    private fun nextPhase(current: TimerPhase, focusDone: Int, settings: TimerSettings): TimerPhase {
        return when (current) {
            TimerPhase.FOCUS -> {
                if (focusDone > 0 && focusDone % settings.sessionsUntilLongBreak == 0)
                    TimerPhase.LONG_BREAK else TimerPhase.SHORT_BREAK
            }
            TimerPhase.SHORT_BREAK, TimerPhase.LONG_BREAK -> TimerPhase.FOCUS
        }
    }

    private fun secondsFor(phase: TimerPhase, settings: TimerSettings): Int = when (phase) {
        TimerPhase.FOCUS -> settings.focusMinutes * 60
        TimerPhase.SHORT_BREAK -> settings.shortBreakMinutes * 60
        TimerPhase.LONG_BREAK -> settings.longBreakMinutes * 60
    }

    private fun vibrate() {
        val ctx = getApplication<Application>()
        val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val vm = ctx.getSystemService(VibratorManager::class.java)
            vm.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            ctx.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    private fun publishWidget() {
        val s = _ui.value
        WidgetStateHolder.update(s.phase.name, s.remainingSeconds, s.isRunning, s.timeDisplay)
        viewModelScope.launch {
            FocusTimerWidget().updateAll(getApplication())
        }
    }

    private fun startOfDayMillis(): Long {
        val c = Calendar.getInstance()
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }
}
