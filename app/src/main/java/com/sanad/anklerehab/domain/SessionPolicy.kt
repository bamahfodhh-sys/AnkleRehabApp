package com.sanad.anklerehab.domain

object SessionPolicy {
    fun allRequiredChecked(plan: DayPlan, checkedKeys: Set<String>): Boolean =
        plan.trainingDay && plan.exercises.filter { it.required }.all { it.key in checkedKeys }

    fun nextStatusAfterToggle(checkedKeys: Set<String>, current: SessionStatus?): SessionStatus {
        if (current == SessionStatus.COMPLETED || current == SessionStatus.STOPPED_DUE_TO_SYMPTOMS || current == SessionStatus.SKIPPED) {
            return current
        }
        return if (checkedKeys.isEmpty()) SessionStatus.SCHEDULED else SessionStatus.IN_PROGRESS
    }
}
