package com.sanad.anklerehab.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.sanad.anklerehab.data.settings.SettingsRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class ReminderScheduler(
    private val context: Context,
    private val settingsRepository: SettingsRepository
) {
    suspend fun rescheduleAll() {
        val settings = settingsRepository.settings.first()
        if (settings.reminderEnabled) {
            scheduleNext(
                kind = ReminderReceiver.KIND_MAIN,
                hour = settings.reminderHour,
                minute = settings.reminderMinute,
                requestCode = MAIN_REQUEST_CODE
            )
        } else cancel(MAIN_REQUEST_CODE, ReminderReceiver.KIND_MAIN)

        if (settings.towelReminderEnabled) {
            scheduleNext(
                kind = ReminderReceiver.KIND_TOWEL,
                hour = settings.towelReminderHour,
                minute = settings.towelReminderMinute,
                requestCode = TOWEL_REQUEST_CODE
            )
        } else cancel(TOWEL_REQUEST_CODE, ReminderReceiver.KIND_TOWEL)
    }

    suspend fun scheduleNextForKind(kind: String) {
        val settings = settingsRepository.settings.first()
        when (kind) {
            ReminderReceiver.KIND_MAIN -> if (settings.reminderEnabled) {
                scheduleNext(kind, settings.reminderHour, settings.reminderMinute, MAIN_REQUEST_CODE)
            }
            ReminderReceiver.KIND_TOWEL -> if (settings.towelReminderEnabled) {
                scheduleNext(kind, settings.towelReminderHour, settings.towelReminderMinute, TOWEL_REQUEST_CODE)
            }
        }
    }

    private fun scheduleNext(kind: String, hour: Int, minute: Int, requestCode: Int) {
        val now = LocalDateTime.now()
        var target = LocalDateTime.of(LocalDate.now(), LocalTime.of(hour, minute))
        if (!target.isAfter(now)) target = target.plusDays(1)
        val millis = target.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            millis,
            pendingIntent(kind, requestCode)
        )
    }

    private fun cancel(requestCode: Int, kind: String) {
        context.getSystemService(AlarmManager::class.java).cancel(pendingIntent(kind, requestCode))
    }

    private fun pendingIntent(kind: String, requestCode: Int): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).putExtra(ReminderReceiver.EXTRA_KIND, kind)
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        private const val MAIN_REQUEST_CODE = 6101
        private const val TOWEL_REQUEST_CODE = 6102
    }
}
