package com.sanad.anklerehab.domain

import java.time.LocalDate
import java.time.temporal.ChronoUnit

object ScheduleEngine {
    private const val PHASE_DAYS = 14L

    fun scheduledDate(programDay: Int, settings: RehabSettings): LocalDate? {
        require(programDay in 1..PlanCatalog.TOTAL_PROGRAM_DAYS)
        return when (programDay) {
            in 1..14 -> settings.startDate.plusDays((programDay - 1).toLong())
            in 15..28 -> settings.phase2StartDate?.plusDays((programDay - 15).toLong())
            else -> settings.phase3StartDate?.plusDays((programDay - 29).toLong())
        }
    }

    fun state(today: LocalDate, settings: RehabSettings): ScheduleState {
        if (today.isBefore(settings.startDate)) {
            return ScheduleState(null, null, programComplete = false, beforeProgram = true)
        }

        val p2 = settings.phase2StartDate
        if (p2 == null) {
            val elapsed = ChronoUnit.DAYS.between(settings.startDate, today)
            return if (elapsed < PHASE_DAYS) {
                ScheduleState((elapsed + 1).toInt(), null, false, false)
            } else {
                ScheduleState(null, 2, false, false)
            }
        }

        if (today.isBefore(p2)) return ScheduleState(null, null, false, false)
        val p2Elapsed = ChronoUnit.DAYS.between(p2, today)
        if (p2Elapsed < PHASE_DAYS) {
            return ScheduleState((15 + p2Elapsed).toInt(), null, false, false)
        }

        val p3 = settings.phase3StartDate
        if (p3 == null) return ScheduleState(null, 3, false, false)
        if (today.isBefore(p3)) return ScheduleState(null, null, false, false)

        val p3Elapsed = ChronoUnit.DAYS.between(p3, today)
        return if (p3Elapsed < PHASE_DAYS) {
            ScheduleState((29 + p3Elapsed).toInt(), null, false, false)
        } else {
            ScheduleState(null, null, programComplete = true, beforeProgram = false)
        }
    }

    fun canEditSession(programDay: Int, today: LocalDate, settings: RehabSettings): Boolean {
        val plan = PlanCatalog.day(programDay, settings.phase2DaysPerWeek)
        val scheduled = scheduledDate(programDay, settings) ?: return false
        return plan.trainingDay && !scheduled.isAfter(today)
    }

    fun previousPhaseForTransition(targetPhase: Int): IntRange = when (targetPhase) {
        2 -> PlanCatalog.phaseRange(1)
        3 -> PlanCatalog.phaseRange(2)
        else -> error("No previous phase for $targetPhase")
    }
}
