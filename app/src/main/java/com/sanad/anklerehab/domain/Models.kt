package com.sanad.anklerehab.domain

import java.time.LocalDate

enum class SessionStatus {
    SCHEDULED,
    IN_PROGRESS,
    COMPLETED,
    PARTIAL,
    SKIPPED,
    STOPPED_DUE_TO_SYMPTOMS
}

enum class ExerciseSection { PREP, MAIN, EXTRA, RECOVERY }

data class ExerciseStep(
    val key: String,
    val title: String,
    val instruction: String,
    val dose: String,
    val section: ExerciseSection,
    val timerSeconds: Int? = null,
    val required: Boolean = true
)

data class DayPlan(
    val programDay: Int,
    val week: Int,
    val dayInWeek: Int,
    val phase: Int,
    val trainingDay: Boolean,
    val phaseTitle: String,
    val phaseSubtitle: String,
    val exercises: List<ExerciseStep>,
    val walkingGuidance: String
)

data class RehabSettings(
    val startDate: LocalDate,
    val phase2StartDate: LocalDate? = null,
    val phase3StartDate: LocalDate? = null,
    val phase2DaysPerWeek: Int = 6,
    val reminderEnabled: Boolean = false,
    val reminderHour: Int = 20,
    val reminderMinute: Int = 0,
    val towelReminderEnabled: Boolean = false,
    val towelReminderHour: Int = 14,
    val towelReminderMinute: Int = 0
)

data class ScheduleState(
    val todayProgramDay: Int?,
    val transitionPendingToPhase: Int?,
    val programComplete: Boolean,
    val beforeProgram: Boolean
)
