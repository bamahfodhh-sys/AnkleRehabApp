package com.sanad.anklerehab

import android.app.Application
import com.sanad.anklerehab.reminder.NotificationChannels

class RehabApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        NotificationChannels.create(this)
    }
}
