package com.sanad.anklerehab.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlanCatalogTest {
    @Test
    fun defaultPlan_has42DaysAnd34TrainingSessions() {
        assertEquals(42, PlanCatalog.TOTAL_PROGRAM_DAYS)
        assertEquals(34, PlanCatalog.totalTrainingDays(6))
    }

    @Test
    fun fiveDayPhase2_has32TrainingSessions() {
        assertEquals(32, PlanCatalog.totalTrainingDays(5))
    }

    @Test
    fun phase1_hasTwoSeparateTowelDoses() {
        val day = PlanCatalog.day(1)
        assertTrue(day.exercises.any { it.key == "TOWEL_CURL_1" })
        assertTrue(day.exercises.any { it.key == "TOWEL_CURL_2" && it.section == ExerciseSection.EXTRA })
    }

    @Test
    fun restDays_areNotTrainingDays() {
        assertFalse(PlanCatalog.day(7).trainingDay)
        assertFalse(PlanCatalog.day(14).trainingDay)
        assertFalse(PlanCatalog.day(35).trainingDay) // week 5 day 7
        assertFalse(PlanCatalog.day(41).trainingDay) // week 6 day 6
    }
}
