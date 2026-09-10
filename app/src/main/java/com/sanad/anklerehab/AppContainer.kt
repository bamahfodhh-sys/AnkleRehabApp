package com.sanad.anklerehab

import android.content.Context
import com.sanad.anklerehab.data.RehabRepository
import com.sanad.anklerehab.data.db.RehabDatabase
import com.sanad.anklerehab.data.settings.SettingsRepository
import com.sanad.anklerehab.reminder.ReminderScheduler

class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val database = RehabDatabase.get(appContext)
    val settingsRepository = SettingsRepository(appContext)
    val repository = RehabRepository(database, settingsRepository)
    val reminderScheduler = ReminderScheduler(appContext, settingsRepository)
}
