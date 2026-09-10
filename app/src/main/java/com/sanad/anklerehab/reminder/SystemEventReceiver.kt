package com.sanad.anklerehab.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.sanad.anklerehab.RehabApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SystemEventReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val pending = goAsync()
        val app = context.applicationContext as RehabApplication
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                app.container.reminderScheduler.rescheduleAll()
            } finally {
                pending.finish()
            }
        }
    }
}
