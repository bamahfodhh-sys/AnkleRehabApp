package com.sanad.anklerehab.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationChannels {
    const val REHAB_CHANNEL = "rehab_reminders"

    fun create(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                REHAB_CHANNEL,
                "تذكيرات التأهيل",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "تذكيرات جلسات تأهيل الكاحل والجرعة اليومية الثانية"
                enableVibration(true)
            }
            manager.createNotificationChannel(channel)
        }
    }
}
