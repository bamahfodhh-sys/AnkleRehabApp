package com.sanad.anklerehab.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sanad.anklerehab.AppContainer
import com.sanad.anklerehab.data.db.SessionRecordEntity
import com.sanad.anklerehab.domain.PlanCatalog
import com.sanad.anklerehab.domain.RehabSettings
import com.sanad.anklerehab.domain.ScheduleEngine
import com.sanad.anklerehab.domain.ScheduleState
import com.sanad.anklerehab.domain.SessionStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate


data class AppUiState(
    val loading: Boolean = true,
    val today: LocalDate = LocalDate.now(),
    val settings: RehabSettings = RehabSettings(LocalDate.now()),
    val schedule: ScheduleState = ScheduleState(null, null, false, false),
    val records: Map<Int, SessionRecordEntity> = emptyMap(),
    val checkedKeys: Map<Int, Set<String>> = emptyMap(),
    val totalTrainingSessions: Int = 34,
    val dueTrainingSessions: Int = 0,
    val completedDueSessions: Int = 0,
    val completedTotalSessions: Int = 0
)

class MainViewModel(private val container: AppContainer) : ViewModel() {
    private val repository = container.repository
    private val minuteTicker = flow {
        while (true) {
            emit(LocalDate.now())
            delay(60_000)
        }
    }

    val state = combine(
        repository.settings,
        repository.records,
        repository.checks,
        minuteTicker
    ) { settings, records, checks, today ->
        val recordMap = records.associateBy { it.programDay }
        val checkedMap = checks.filter { it.checked }.groupBy { it.programDay }
            .mapValues { (_, list) -> list.mapTo(linkedSetOf()) { it.exerciseKey } }
        val total = PlanCatalog.totalTrainingDays(settings.phase2DaysPerWeek)
        val dueDays = (1..PlanCatalog.TOTAL_PROGRAM_DAYS).filter { day ->
            val date = ScheduleEngine.scheduledDate(day, settings)
            date != null && !date.isAfter(today) && PlanCatalog.day(day, settings.phase2DaysPerWeek).trainingDay
        }
        val completedTotal = recordMap.values.count { it.status == SessionStatus.COMPLETED.name }
        val completedDue = dueDays.count { recordMap[it]?.status == SessionStatus.COMPLETED.name }
        AppUiState(
            loading = false,
            today = today,
            settings = settings,
            schedule = ScheduleEngine.state(today, settings),
            records = recordMap,
            checkedKeys = checkedMap,
            totalTrainingSessions = total,
            dueTrainingSessions = dueDays.size,
            completedDueSessions = completedDue,
            completedTotalSessions = completedTotal
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppUiState())

    init {
        viewModelScope.launch {
            repository.initialize()
            container.reminderScheduler.rescheduleAll()
        }
    }

    fun toggleExercise(programDay: Int, key: String, checked: Boolean) = viewModelScope.launch {
        val s = repository.settingsRepository.current()
        if (!ScheduleEngine.canEditSession(programDay, LocalDate.now(), s)) return@launch
        repository.setExerciseChecked(programDay, key, checked)
    }

    fun completeSession(programDay: Int) = viewModelScope.launch {
        val s = repository.settingsRepository.current()
        if (!ScheduleEngine.canEditSession(programDay, LocalDate.now(), s)) return@launch
        repository.markCompleted(programDay, s.phase2DaysPerWeek)
    }

    fun markPartial(programDay: Int) = markStatusIfEditable(programDay, SessionStatus.PARTIAL)
    fun markSkipped(programDay: Int) = markStatusIfEditable(programDay, SessionStatus.SKIPPED)
    fun markStopped(programDay: Int) = markStatusIfEditable(programDay, SessionStatus.STOPPED_DUE_TO_SYMPTOMS)

    private fun markStatusIfEditable(programDay: Int, status: SessionStatus) = viewModelScope.launch {
        val s = repository.settingsRepository.current()
        if (!ScheduleEngine.canEditSession(programDay, LocalDate.now(), s)) return@launch
        repository.markStatus(programDay, status)
    }

    fun reopen(programDay: Int) = viewModelScope.launch {
        val s = repository.settingsRepository.current()
        if (!ScheduleEngine.canEditSession(programDay, LocalDate.now(), s)) return@launch
        repository.reopen(programDay)
    }

    fun saveNote(programDay: Int, note: String) = viewModelScope.launch {
        val s = repository.settingsRepository.current()
        if (!ScheduleEngine.canEditSession(programDay, LocalDate.now(), s)) return@launch
        repository.updateNote(programDay, note)
    }

    fun confirmPhase(phase: Int) = viewModelScope.launch {
        val s = repository.settingsRepository.current()
        val pending = ScheduleEngine.state(LocalDate.now(), s).transitionPendingToPhase
        if (pending != phase) return@launch
        repository.confirmPhaseStart(phase, LocalDate.now())
        if (phase == 2 && s.towelReminderEnabled) {
            repository.settingsRepository.setTowelReminder(false, s.towelReminderHour, s.towelReminderMinute)
        }
        container.reminderScheduler.rescheduleAll()
    }

    fun setMainReminder(enabled: Boolean, hour: Int, minute: Int) = viewModelScope.launch {
        repository.settingsRepository.setReminder(enabled, hour, minute)
        container.reminderScheduler.rescheduleAll()
    }

    fun setTowelReminder(enabled: Boolean, hour: Int, minute: Int) = viewModelScope.launch {
        repository.settingsRepository.setTowelReminder(enabled, hour, minute)
        container.reminderScheduler.rescheduleAll()
    }

    fun setPhase2Days(days: Int) = viewModelScope.launch {
        val current = repository.settingsRepository.current()
        if (current.phase2StartDate == null) repository.settingsRepository.setPhase2DaysPerWeek(days)
    }

    fun shiftStartDate(newStart: LocalDate) = viewModelScope.launch {
        repository.shiftProgramStart(newStart)
        container.reminderScheduler.rescheduleAll()
    }

    fun resetProgram(newStart: LocalDate) = viewModelScope.launch {
        repository.resetProgram(newStart)
        container.reminderScheduler.rescheduleAll()
    }
}

class MainViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(container) as T
    }
}
