package com.sanad.anklerehab.reminder

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.sanad.anklerehab.MainActivity
import com.sanad.anklerehab.R
import com.sanad.anklerehab.RehabApplication
import com.sanad.anklerehab.domain.PlanCatalog
import com.sanad.anklerehab.domain.ScheduleEngine
import com.sanad.anklerehab.domain.SessionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        val kind = intent.getStringExtra(EXTRA_KIND) ?: KIND_MAIN
        val app = context.applicationContext as RehabApplication
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val keepScheduling = maybeNotify(context, app, kind)
                if (keepScheduling) app.container.reminderScheduler.scheduleNextForKind(kind)
            } finally {
                pending.finish()
            }
        }
    }

    private suspend fun maybeNotify(context: Context, app: RehabApplication, kind: String): Boolean {
        val settings = app.container.repository.settings.first()
        val schedule = ScheduleEngine.state(LocalDate.now(), settings)
        if (schedule.programComplete) return false

        if (android.os.Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return true

        if (kind == KIND_MAIN && schedule.transitionPendingToPhase != null) {
            showNotification(
                context,
                7200 + schedule.transitionPendingToPhase,
                "مراجعة المرحلة التالية",
                "انتهت المدة الزمنية للمرحلة الحالية. افتح التطبيق لمراجعة التقدم وتأكيد بدء المرحلة التالية عند ملاءمتها لك."
            )
            return true
        }

        val day = schedule.todayProgramDay ?: return true
        val plan = PlanCatalog.day(day, settings.phase2DaysPerWeek)
        if (!plan.trainingDay) return true
        val record = app.container.repository.getRecord(day)
        if (record?.status in setOf(
                SessionStatus.COMPLETED.name,
                SessionStatus.STOPPED_DUE_TO_SYMPTOMS.name,
                SessionStatus.SKIPPED.name
            )
        ) return true

        if (kind == KIND_TOWEL) {
            if (plan.phase != 1) return true
            val checked = app.container.repository.getCheckedKeys(day)
            if ("TOWEL_CURL_2" in checked) return true
            showNotification(context, 7202, "كرشمة الفوطة · المرة الثانية", "باقي الجرعة الثانية اليوم: 10 مرات.")
        } else {
            showNotification(context, 7201, "موعد جلسة تأهيل الكاحل", "جلسة اليوم جاهزة. افتح التطبيق وابدأ خطوة بخطوة.")
        }
        return true
    }

    private fun showNotification(context: Context, id: Int, title: String, text: String) {
        val contentIntent = PendingIntent.getActivity(
            context,
            7300,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, NotificationChannels.REHAB_CHANNEL)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()
        NotificationManagerCompat.from(context).notify(id, notification)
    }

    companion object {
        const val EXTRA_KIND = "reminder_kind"
        const val KIND_MAIN = "main"
        const val KIND_TOWEL = "towel"
    }
}
