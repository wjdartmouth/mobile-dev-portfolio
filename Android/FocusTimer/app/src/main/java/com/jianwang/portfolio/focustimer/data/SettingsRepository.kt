package com.jianwang.portfolio.focustimer.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("timer_settings")

class SettingsRepository(private val context: Context) {
    private object Keys {
        val focus = intPreferencesKey("focus_min")
        val shortBreak = intPreferencesKey("short_break_min")
        val longBreak = intPreferencesKey("long_break_min")
        val untilLong = intPreferencesKey("until_long")
        val autoStart = booleanPreferencesKey("auto_start")
        val haptics = booleanPreferencesKey("haptics")
        val sound = booleanPreferencesKey("sound")
    }

    val settings: Flow<TimerSettings> = context.dataStore.data.map { p ->
        TimerSettings(
            focusMinutes = p[Keys.focus] ?: 25,
            shortBreakMinutes = p[Keys.shortBreak] ?: 5,
            longBreakMinutes = p[Keys.longBreak] ?: 15,
            sessionsUntilLongBreak = p[Keys.untilLong] ?: 4,
            autoStartNext = p[Keys.autoStart] ?: false,
            hapticsEnabled = p[Keys.haptics] ?: true,
            soundEnabled = p[Keys.sound] ?: true
        )
    }

    suspend fun update(transform: (TimerSettings) -> TimerSettings) {
        context.dataStore.edit { prefs ->
            val current = TimerSettings(
                focusMinutes = prefs[Keys.focus] ?: 25,
                shortBreakMinutes = prefs[Keys.shortBreak] ?: 5,
                longBreakMinutes = prefs[Keys.longBreak] ?: 15,
                sessionsUntilLongBreak = prefs[Keys.untilLong] ?: 4,
                autoStartNext = prefs[Keys.autoStart] ?: false,
                hapticsEnabled = prefs[Keys.haptics] ?: true,
                soundEnabled = prefs[Keys.sound] ?: true
            )
            val next = transform(current)
            prefs[Keys.focus] = next.focusMinutes
            prefs[Keys.shortBreak] = next.shortBreakMinutes
            prefs[Keys.longBreak] = next.longBreakMinutes
            prefs[Keys.untilLong] = next.sessionsUntilLongBreak
            prefs[Keys.autoStart] = next.autoStartNext
            prefs[Keys.haptics] = next.hapticsEnabled
            prefs[Keys.sound] = next.soundEnabled
        }
    }
}
