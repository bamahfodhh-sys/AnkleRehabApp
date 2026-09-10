package com.sanad.anklerehab.data.settings

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sanad.anklerehab.domain.RehabSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate

private val Context.rehabDataStore by preferencesDataStore(name = "rehab_settings")

class SettingsRepository(private val context: Context) {
    private object Keys {
        val START_EPOCH_DAY = longPreferencesKey("start_epoch_day")
        val PHASE2_START_EPOCH_DAY = longPreferencesKey("phase2_start_epoch_day")
        val PHASE3_START_EPOCH_DAY = longPreferencesKey("phase3_start_epoch_day")
        val PHASE2_DAYS_PER_WEEK = intPreferencesKey("phase2_days_per_week")
        val REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
        val REMINDER_HOUR = intPreferencesKey("reminder_hour")
        val REMINDER_MINUTE = intPreferencesKey("reminder_minute")
        val TOWEL_REMINDER_ENABLED = booleanPreferencesKey("towel_reminder_enabled")
        val TOWEL_REMINDER_HOUR = intPreferencesKey("towel_reminder_hour")
        val TOWEL_REMINDER_MINUTE = intPreferencesKey("towel_reminder_minute")
    }

    val settings: Flow<RehabSettings> = context.rehabDataStore.data.map { prefs -> prefs.toSettings() }

    suspend fun ensureInitialized(today: LocalDate = LocalDate.now()) {
        val prefs = context.rehabDataStore.data.first()
        if (prefs[Keys.START_EPOCH_DAY] == null) {
            context.rehabDataStore.edit { it[Keys.START_EPOCH_DAY] = today.toEpochDay() }
        }
    }

    suspend fun current(): RehabSettings = settings.first()

    suspend fun setReminder(enabled: Boolean, hour: Int, minute: Int) {
        context.rehabDataStore.edit {
            it[Keys.REMINDER_ENABLED] = enabled
            it[Keys.REMINDER_HOUR] = hour.coerceIn(0, 23)
            it[Keys.REMINDER_MINUTE] = minute.coerceIn(0, 59)
        }
    }

    suspend fun setTowelReminder(enabled: Boolean, hour: Int, minute: Int) {
        context.rehabDataStore.edit {
            it[Keys.TOWEL_REMINDER_ENABLED] = enabled
            it[Keys.TOWEL_REMINDER_HOUR] = hour.coerceIn(0, 23)
            it[Keys.TOWEL_REMINDER_MINUTE] = minute.coerceIn(0, 59)
        }
    }

    suspend fun setPhase2DaysPerWeek(days: Int) {
        context.rehabDataStore.edit { it[Keys.PHASE2_DAYS_PER_WEEK] = days.coerceIn(5, 6) }
    }

    suspend fun confirmPhaseStart(phase: Int, date: LocalDate) {
        context.rehabDataStore.edit {
            when (phase) {
                2 -> it[Keys.PHASE2_START_EPOCH_DAY] = date.toEpochDay()
                3 -> it[Keys.PHASE3_START_EPOCH_DAY] = date.toEpochDay()
            }
        }
    }

    suspend fun shiftProgramStart(newStart: LocalDate) {
        val old = current()
        val delta = newStart.toEpochDay() - old.startDate.toEpochDay()
        context.rehabDataStore.edit {
            it[Keys.START_EPOCH_DAY] = newStart.toEpochDay()
            old.phase2StartDate?.let { date -> it[Keys.PHASE2_START_EPOCH_DAY] = date.toEpochDay() + delta }
            old.phase3StartDate?.let { date -> it[Keys.PHASE3_START_EPOCH_DAY] = date.toEpochDay() + delta }
        }
    }

    suspend fun resetProgram(newStart: LocalDate) {
        context.rehabDataStore.edit { prefs ->
            val reminderEnabled = prefs[Keys.REMINDER_ENABLED] ?: false
            val reminderHour = prefs[Keys.REMINDER_HOUR] ?: 20
            val reminderMinute = prefs[Keys.REMINDER_MINUTE] ?: 0
            val towelEnabled = prefs[Keys.TOWEL_REMINDER_ENABLED] ?: false
            val towelHour = prefs[Keys.TOWEL_REMINDER_HOUR] ?: 14
            val towelMinute = prefs[Keys.TOWEL_REMINDER_MINUTE] ?: 0
            val phase2Days = prefs[Keys.PHASE2_DAYS_PER_WEEK] ?: 6
            prefs.clear()
            prefs[Keys.START_EPOCH_DAY] = newStart.toEpochDay()
            prefs[Keys.REMINDER_ENABLED] = reminderEnabled
            prefs[Keys.REMINDER_HOUR] = reminderHour
            prefs[Keys.REMINDER_MINUTE] = reminderMinute
            prefs[Keys.TOWEL_REMINDER_ENABLED] = towelEnabled
            prefs[Keys.TOWEL_REMINDER_HOUR] = towelHour
            prefs[Keys.TOWEL_REMINDER_MINUTE] = towelMinute
            prefs[Keys.PHASE2_DAYS_PER_WEEK] = phase2Days
        }
    }

    private fun Preferences.toSettings(): RehabSettings {
        val today = LocalDate.now()
        return RehabSettings(
            startDate = LocalDate.ofEpochDay(this[Keys.START_EPOCH_DAY] ?: today.toEpochDay()),
            phase2StartDate = this[Keys.PHASE2_START_EPOCH_DAY]?.let(LocalDate::ofEpochDay),
            phase3StartDate = this[Keys.PHASE3_START_EPOCH_DAY]?.let(LocalDate::ofEpochDay),
            phase2DaysPerWeek = (this[Keys.PHASE2_DAYS_PER_WEEK] ?: 6).coerceIn(5, 6),
            reminderEnabled = this[Keys.REMINDER_ENABLED] ?: false,
            reminderHour = this[Keys.REMINDER_HOUR] ?: 20,
            reminderMinute = this[Keys.REMINDER_MINUTE] ?: 0,
            towelReminderEnabled = this[Keys.TOWEL_REMINDER_ENABLED] ?: false,
            towelReminderHour = this[Keys.TOWEL_REMINDER_HOUR] ?: 14,
            towelReminderMinute = this[Keys.TOWEL_REMINDER_MINUTE] ?: 0
        )
    }
}
