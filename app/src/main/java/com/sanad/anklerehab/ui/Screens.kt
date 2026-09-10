package com.sanad.anklerehab.ui

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.sanad.anklerehab.domain.DayPlan
import com.sanad.anklerehab.domain.ExerciseSection
import com.sanad.anklerehab.domain.ExerciseStep
import com.sanad.anklerehab.domain.PlanCatalog
import com.sanad.anklerehab.domain.ScheduleEngine
import com.sanad.anklerehab.domain.SessionPolicy
import com.sanad.anklerehab.domain.SessionStatus
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.ceil

private val arabicLocale = Locale("ar")
private val dateFormatter = DateTimeFormatter.ofPattern("EEEE، d MMMM yyyy", arabicLocale)
private val shortDateFormatter = DateTimeFormatter.ofPattern("d MMM", arabicLocale)

@Composable
fun TodayScreen(state: AppUiState, viewModel: MainViewModel, onOpenSession: (Int) -> Unit) {
    if (state.loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().imePadding(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ScreenHeading("جلسة اليوم", state.today.format(dateFormatter))
        }
        item { OverallProgressCard(state) }

        when {
            state.schedule.beforeProgram -> item {
                InfoCard(
                    title = "البرنامج لم يبدأ بعد",
                    body = "تاريخ البداية: ${state.settings.startDate.format(dateFormatter)}"
                )
            }
            state.schedule.transitionPendingToPhase != null -> item {
                PhaseTransitionCard(state, viewModel, state.schedule.transitionPendingToPhase)
            }
            state.schedule.programComplete -> item {
                InfoCard(
                    title = "اكتملت مدة البرنامج",
                    body = "راجع السجل والتقدم. استمرار أو تعديل التمارين بعد هذه المرحلة يكون وفق تقييمك وتوجيه المختص."
                )
            }
            state.schedule.todayProgramDay != null -> {
                val day = state.schedule.todayProgramDay
                val plan = PlanCatalog.day(day, state.settings.phase2DaysPerWeek)
                if (!plan.trainingDay) {
                    item {
                        InfoCard(
                            title = "يوم راحة",
                            body = "اليوم جزء من الجدول لكنه ليس جلسة تدريب. لا تحتاج إلى تسجيل جلسة."
                        )
                    }
                } else {
                    item {
                        SessionDetail(
                            programDay = day,
                            state = state,
                            viewModel = viewModel,
                            compactHeader = true
                        )
                    }
                }
            }
            else -> item {
                InfoCard("لا توجد جلسة مجدولة اليوم", "افتح الخطة لمراجعة الأيام القادمة.")
            }
        }
    }
}

@Composable
private fun OverallProgressCard(state: AppUiState) {
    val due = state.dueTrainingSessions
    val complete = state.completedDueSessions
    val progress = if (due == 0) 0f else complete.toFloat() / due.toFloat()
    Card {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("الالتزام حتى اليوم", fontWeight = FontWeight.Bold)
                Text("$complete / $due")
            }
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
            Text(
                "الإجمالي: ${state.completedTotalSessions} من ${state.totalTrainingSessions} جلسة تدريب مخططة",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PhaseTransitionCard(state: AppUiState, viewModel: MainViewModel, targetPhase: Int) {
    val previousRange = ScheduleEngine.previousPhaseForTransition(targetPhase)
    val scheduled = previousRange.count { PlanCatalog.day(it, state.settings.phase2DaysPerWeek).trainingDay }
    val completed = previousRange.count { state.records[it]?.status == SessionStatus.COMPLETED.name }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("المرحلة ${targetPhase - 1} انتهت زمنيًا", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text("أنجزت $completed من $scheduled جلسة تدريب في هذه المرحلة.")
            Text(
                "التطبيق لا يقرر تلقائيًا أن زيادة التحميل مناسبة. ابدأ المرحلة التالية فقط عندما يكون الانتقال مناسبًا وفق توجيه المختص وقدرتك.",
                style = MaterialTheme.typography.bodyMedium
            )
            Button(onClick = { viewModel.confirmPhase(targetPhase) }, modifier = Modifier.fillMaxWidth()) {
                Text("تأكيد بدء المرحلة $targetPhase من اليوم")
            }
        }
    }
}

@Composable
fun SessionScreen(programDay: Int, state: AppUiState, viewModel: MainViewModel, onBack: () -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().imePadding(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            TextButton(onClick = onBack) { Text("← رجوع") }
        }
        item {
            SessionDetail(programDay, state, viewModel, compactHeader = false)
        }
    }
}

@Composable
private fun SessionDetail(
    programDay: Int,
    state: AppUiState,
    viewModel: MainViewModel,
    compactHeader: Boolean
) {
    val plan = PlanCatalog.day(programDay, state.settings.phase2DaysPerWeek)
    val scheduledDate = ScheduleEngine.scheduledDate(programDay, state.settings)
    val editable = ScheduleEngine.canEditSession(programDay, state.today, state.settings)
    val checked = state.checkedKeys[programDay].orEmpty()
    val record = state.records[programDay]
    val status = record?.status?.let { runCatching { SessionStatus.valueOf(it) }.getOrNull() }
    val lockedByStatus = status in setOf(SessionStatus.COMPLETED, SessionStatus.SKIPPED, SessionStatus.STOPPED_DUE_TO_SYMPTOMS)
    val canToggle = editable && !lockedByStatus
    val allDone = SessionPolicy.allRequiredChecked(plan, checked)
    var note by remember(programDay, record?.note) { mutableStateOf(record?.note.orEmpty()) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (!compactHeader) {
            ScreenHeading(
                "اليوم $programDay · الأسبوع ${plan.week}",
                scheduledDate?.format(dateFormatter) ?: "لم يُحدد تاريخ هذه المرحلة بعد"
            )
        }
        Card {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(plan.phaseTitle, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(plan.phaseSubtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (scheduledDate != null) Text(scheduledDate.format(dateFormatter), style = MaterialTheme.typography.bodySmall)
                StatusBadge(statusLabel(plan, scheduledDate, state.today, status))
            }
        }

        if (!plan.trainingDay) {
            InfoCard("يوم راحة", "هذا اليوم غير محسوب كجلسة تدريب.")
            return@Column
        }

        if (scheduledDate == null) {
            InfoCard("الجلسة غير مجدولة بعد", "ابدأ المرحلة السابقة وأكمل انتقال المرحلة قبل تسجيل هذه الجلسة.")
            return@Column
        }

        if (scheduledDate.isAfter(state.today)) {
            InfoCard("معاينة فقط 🔒", "يمكنك مشاهدة جلسة ${scheduledDate.format(shortDateFormatter)}، لكن لا يمكن تسجيلها قبل موعدها.")
        }

        ExerciseSectionBlock("قبل الجلسة", plan.exercises.filter { it.section == ExerciseSection.PREP }, checked, canToggle, programDay, viewModel)
        ExerciseSectionBlock("التمارين", plan.exercises.filter { it.section == ExerciseSection.MAIN }, checked, canToggle, programDay, viewModel)
        if (plan.exercises.any { it.section == ExerciseSection.EXTRA }) {
            ExerciseSectionBlock("جرعة إضافية خلال اليوم", plan.exercises.filter { it.section == ExerciseSection.EXTRA }, checked, canToggle, programDay, viewModel)
        }

        InfoCard("المشي والتحميل", plan.walkingGuidance)
        ExerciseSectionBlock("بعد الجلسة", plan.exercises.filter { it.section == ExerciseSection.RECOVERY }, checked, canToggle, programDay, viewModel)

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
            Text(
                "قاعدة أساسية: توقّف فورًا عن أي تمرين يسبب ألمًا حادًا، واكتفِ بالجهد الذي تتحمله دون ضغط مؤذٍ.",
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }

        if (editable) {
            OutlinedTextField(
                value = note,
                onValueChange = { note = it.take(1000) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                label = { Text("ملاحظات الجلسة") },
                supportingText = { Text("تبقى هذه الملاحظات على الجهاز ولا تدخل في النسخ الاحتياطي.") }
            )
            OutlinedButton(onClick = { viewModel.saveNote(programDay, note) }, modifier = Modifier.fillMaxWidth()) {
                Text("حفظ الملاحظة")
            }

            when (status) {
                SessionStatus.COMPLETED -> {
                    InfoCard("تمت الجلسة ✓", "يمكن إعادة فتحها إذا احتجت تعديل التسجيل.")
                    OutlinedButton(onClick = { viewModel.reopen(programDay) }, modifier = Modifier.fillMaxWidth()) { Text("إعادة فتح الجلسة") }
                }
                SessionStatus.SKIPPED -> {
                    InfoCard("تم تخطي الجلسة", "سُجل اليوم كتخطي. يمكنك إعادة فتحه لتعديل التسجيل.")
                    OutlinedButton(onClick = { viewModel.reopen(programDay) }, modifier = Modifier.fillMaxWidth()) { Text("إعادة فتح الجلسة") }
                }
                SessionStatus.STOPPED_DUE_TO_SYMPTOMS -> {
                    InfoCard("أوقفت الجلسة بسبب ألم/أعراض", "لم تُسجل كجلسة مكتملة.")
                    OutlinedButton(onClick = { viewModel.reopen(programDay) }, modifier = Modifier.fillMaxWidth()) { Text("إعادة فتح الجلسة") }
                }
                else -> {
                    val remaining = plan.exercises.count { it.required && it.key !in checked }
                    Button(
                        onClick = { viewModel.completeSession(programDay) },
                        enabled = allDone,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                    ) {
                        Text(if (allDone) "إنهاء الجلسة ✓" else "باقي $remaining خطوة قبل الإنهاء")
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { viewModel.markPartial(programDay) }, modifier = Modifier.weight(1f)) { Text("حفظ كجزئية") }
                        OutlinedButton(onClick = { viewModel.markSkipped(programDay) }, modifier = Modifier.weight(1f)) { Text("تخطي اليوم") }
                    }
                    OutlinedButton(onClick = { viewModel.markStopped(programDay) }, modifier = Modifier.fillMaxWidth()) {
                        Text("أوقفت بسبب ألم/أعراض")
                    }
                }
            }
        }
    }
}

@Composable
private fun ExerciseSectionBlock(
    title: String,
    steps: List<ExerciseStep>,
    checkedKeys: Set<String>,
    editable: Boolean,
    programDay: Int,
    viewModel: MainViewModel
) {
    if (steps.isEmpty()) return
    Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
    steps.forEach { step ->
        ExerciseCard(
            step = step,
            checked = step.key in checkedKeys,
            editable = editable,
            onChecked = { viewModel.toggleExercise(programDay, step.key, it) }
        )
    }
}

@Composable
private fun ExerciseCard(step: ExerciseStep, checked: Boolean, editable: Boolean, onChecked: (Boolean) -> Unit) {
    Card {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = checked, onCheckedChange = if (editable) onChecked else null)
                Column(Modifier.weight(1f)) {
                    Text(step.title, fontWeight = FontWeight.SemiBold)
                    Text(step.dose, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                }
            }
            Text(step.instruction, style = MaterialTheme.typography.bodyMedium)
            step.timerSeconds?.let { ExerciseTimer(it, enabled = editable) }
        }
    }
}

@Composable
private fun ExerciseTimer(totalSeconds: Int, enabled: Boolean) {
    var endAt by rememberSaveable { mutableLongStateOf(0L) }
    var remaining by rememberSaveable { mutableIntStateOf(totalSeconds) }
    LaunchedEffect(endAt) {
        while (endAt > 0L) {
            val left = ceil((endAt - System.currentTimeMillis()) / 1000.0).toInt().coerceAtLeast(0)
            remaining = left
            if (left <= 0) {
                endAt = 0L
                break
            }
            delay(250)
        }
    }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AssistChip(
            enabled = enabled,
            onClick = {
                remaining = totalSeconds
                endAt = System.currentTimeMillis() + totalSeconds * 1000L
            },
            label = { Text(if (endAt == 0L) "ابدأ المؤقت" else "إعادة المؤقت") }
        )
        Text(formatSeconds(if (endAt == 0L) totalSeconds else remaining), fontWeight = FontWeight.Bold)
    }
}

private fun formatSeconds(seconds: Int): String = "%d:%02d".format(seconds / 60, seconds % 60)

@Composable
fun PlanScreen(state: AppUiState, onOpenSession: (Int) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { ScreenHeading("الخطة", "6 أسابيع · ${state.totalTrainingSessions} جلسة تدريب حسب إعداد المرحلة الثانية") }
        (1..6).forEach { week ->
            item {
                Text("الأسبوع $week", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            val range = ((week - 1) * 7 + 1)..(week * 7)
            items(range.toList()) { day ->
                val plan = PlanCatalog.day(day, state.settings.phase2DaysPerWeek)
                val date = ScheduleEngine.scheduledDate(day, state.settings)
                DayRow(
                    day = day,
                    plan = plan,
                    date = date,
                    label = statusLabel(plan, date, state.today, state.records[day]?.status?.let { runCatching { SessionStatus.valueOf(it) }.getOrNull() }),
                    enabled = date != null,
                    onClick = { onOpenSession(day) }
                )
            }
        }
    }
}

@Composable
fun HistoryScreen(state: AppUiState, onOpenSession: (Int) -> Unit) {
    val due = (1..PlanCatalog.TOTAL_PROGRAM_DAYS).filter { day ->
        val date = ScheduleEngine.scheduledDate(day, state.settings)
        date != null && !date.isAfter(state.today) && PlanCatalog.day(day, state.settings.phase2DaysPerWeek).trainingDay
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { ScreenHeading("السجل", "كل الجلسات المستحقة حتى اليوم") }
        if (due.isEmpty()) item { InfoCard("لا يوجد سجل بعد", "سيظهر هنا تاريخ الجلسات بمجرد بدء البرنامج.") }
        var currentWeek = -1
        due.forEach { day ->
            val plan = PlanCatalog.day(day, state.settings.phase2DaysPerWeek)
            if (plan.week != currentWeek) {
                currentWeek = plan.week
                item { Text("الأسبوع ${plan.week}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium) }
            }
            item {
                val date = ScheduleEngine.scheduledDate(day, state.settings)
                val status = state.records[day]?.status?.let { runCatching { SessionStatus.valueOf(it) }.getOrNull() }
                DayRow(day, plan, date, statusLabel(plan, date, state.today, status), true) { onOpenSession(day) }
            }
        }
    }
}

@Composable
private fun DayRow(day: Int, plan: DayPlan, date: LocalDate?, label: String, enabled: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(enabled = enabled, onClick = onClick)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(day.toString(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Column(Modifier.weight(1f)) {
                Text(if (plan.trainingDay) "جلسة تدريب" else "راحة", fontWeight = FontWeight.SemiBold)
                Text(date?.format(shortDateFormatter) ?: "يُحدد عند بدء المرحلة", style = MaterialTheme.typography.bodySmall)
            }
            Text(label, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.End)
        }
    }
}

@Composable
fun SettingsScreen(state: AppUiState, viewModel: MainViewModel) {
    val context = LocalContext.current
    var pendingPermissionFor by remember { mutableStateOf<String?>(null) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        when (pendingPermissionFor) {
            "main" -> viewModel.setMainReminder(granted, state.settings.reminderHour, state.settings.reminderMinute)
            "towel" -> viewModel.setTowelReminder(granted, state.settings.towelReminderHour, state.settings.towelReminderMinute)
        }
        pendingPermissionFor = null
    }
    fun notificationGranted(): Boolean = Build.VERSION.SDK_INT < 33 ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    var pendingNewStart by remember { mutableStateOf<LocalDate?>(null) }
    var showResetConfirm by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { ScreenHeading("الإعدادات", "الجدول والتذكيرات محفوظة على الجهاز") }

        item {
            SettingCard("تاريخ البداية") {
                Text(state.settings.startDate.format(dateFormatter))
                OutlinedButton(onClick = {
                    val d = state.settings.startDate
                    DatePickerDialog(context, { _, y, m, day -> pendingNewStart = LocalDate.of(y, m + 1, day) }, d.year, d.monthValue - 1, d.dayOfMonth).show()
                }, modifier = Modifier.fillMaxWidth()) { Text("تغيير تاريخ البداية") }
            }
        }

        item {
            SettingCard("أيام المرحلة الثانية") {
                Text("الخطة الأصلية تسمح بـ5–6 أيام أسبوعيًا. اختر العدد قبل بدء المرحلة الثانية.")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(5, 6).forEach { days ->
                        FilterChip(
                            selected = state.settings.phase2DaysPerWeek == days,
                            onClick = { viewModel.setPhase2Days(days) },
                            enabled = state.settings.phase2StartDate == null,
                            label = { Text("$days أيام") }
                        )
                    }
                }
                if (state.settings.phase2StartDate != null) {
                    Text("تم تثبيت هذا الإعداد بعد بدء المرحلة الثانية.", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        item {
            SettingCard("تذكير الجلسة") {
                ReminderToggleRow(
                    enabled = state.settings.reminderEnabled,
                    time = formatTime(state.settings.reminderHour, state.settings.reminderMinute),
                    onToggle = { enable ->
                        if (enable && !notificationGranted()) {
                            pendingPermissionFor = "main"
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else viewModel.setMainReminder(enable, state.settings.reminderHour, state.settings.reminderMinute)
                    },
                    onTime = {
                        TimePickerDialog(context, { _, h, m -> viewModel.setMainReminder(state.settings.reminderEnabled, h, m) }, state.settings.reminderHour, state.settings.reminderMinute, false).show()
                    }
                )
                Text("التذكير غير دقيق بالدقيقة وقد يؤخره Android قليلًا لتوفير البطارية.", style = MaterialTheme.typography.bodySmall)
            }
        }

        item {
            SettingCard("تذكير كرشمة الفوطة الثانية") {
                ReminderToggleRow(
                    enabled = state.settings.towelReminderEnabled,
                    time = formatTime(state.settings.towelReminderHour, state.settings.towelReminderMinute),
                    onToggle = { enable ->
                        if (enable && !notificationGranted()) {
                            pendingPermissionFor = "towel"
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else viewModel.setTowelReminder(enable, state.settings.towelReminderHour, state.settings.towelReminderMinute)
                    },
                    onTime = {
                        TimePickerDialog(context, { _, h, m -> viewModel.setTowelReminder(state.settings.towelReminderEnabled, h, m) }, state.settings.towelReminderHour, state.settings.towelReminderMinute, false).show()
                    }
                )
                Text("يظهر فقط في المرحلة الأولى وفي أيام التدريب إذا لم تُسجل الجرعة الثانية.", style = MaterialTheme.typography.bodySmall)
            }
        }

        item {
            SettingCard("البيانات والخصوصية") {
                Text("لا يوجد حساب أو خادم أو تتبع. سجل الجلسات والملاحظات محفوظان محليًا ومُستبعدان من النسخ الاحتياطي، بينما تبقى إعدادات التطبيق قابلة للاستعادة.")
                OutlinedButton(onClick = { showResetConfirm = true }, modifier = Modifier.fillMaxWidth()) { Text("بدء البرنامج من جديد") }
            }
        }
    }

    pendingNewStart?.let { newDate ->
        AlertDialog(
            onDismissRequest = { pendingNewStart = null },
            title = { Text("تغيير تاريخ البداية") },
            text = { Text("اختر ما يحدث للتقدم الحالي. نقل الجدول يحافظ على إنجازات الأيام نفسها، بينما البدء من جديد يمسح السجل والـChecklist.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.shiftStartDate(newDate)
                    pendingNewStart = null
                }) { Text("نقل الجدول مع التقدم") }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.resetProgram(newDate)
                    pendingNewStart = null
                }) { Text("بدء جديد ومسح التقدم") }
            }
        )
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("مسح التقدم؟") },
            text = { Text("سيتم حذف سجل الجلسات وعلامات الـChecklist والملاحظات وبدء البرنامج من اليوم. لا يمكن التراجع.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.resetProgram(LocalDate.now())
                    showResetConfirm = false
                }) { Text("مسح وبدء جديد") }
            },
            dismissButton = { TextButton(onClick = { showResetConfirm = false }) { Text("إلغاء") } }
        )
    }
}

@Composable
private fun ReminderToggleRow(enabled: Boolean, time: String, onToggle: (Boolean) -> Unit, onTime: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(if (enabled) "مفعّل · $time" else "غير مفعّل", fontWeight = FontWeight.SemiBold)
        }
        TextButton(onClick = onTime) { Text("الوقت") }
        Switch(checked = enabled, onCheckedChange = onToggle)
    }
}

@Composable
private fun SettingCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

@Composable
private fun ScreenHeading(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun InfoCard(title: String, body: String) {
    Card {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, fontWeight = FontWeight.Bold)
            Text(body, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun StatusBadge(label: String) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(label, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), style = MaterialTheme.typography.labelMedium)
    }
}

private fun statusLabel(plan: DayPlan, date: LocalDate?, today: LocalDate, status: SessionStatus?): String {
    if (!plan.trainingDay) return "راحة"
    if (date == null) return "مقفلة"
    if (status == SessionStatus.COMPLETED) return "مكتملة ✓"
    if (status == SessionStatus.PARTIAL) return "جزئية"
    if (status == SessionStatus.SKIPPED) return "تم التخطي"
    if (status == SessionStatus.STOPPED_DUE_TO_SYMPTOMS) return "أوقفت لأعراض"
    if (status == SessionStatus.IN_PROGRESS) return "قيد التنفيذ"
    if (date.isAfter(today)) return "قادمة 🔒"
    if (date.isBefore(today)) return "فاتت"
    return "اليوم"
}

private fun formatTime(hour: Int, minute: Int): String {
    val h = if (hour % 12 == 0) 12 else hour % 12
    val amPm = if (hour < 12) "ص" else "م"
    return "%d:%02d %s".format(h, minute, amPm)
}
