package com.sanad.anklerehab.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionPolicyTest {
    @Test
    fun completionRequiresEveryRequiredStepIncludingSecondTowelDose() {
        val plan = PlanCatalog.day(1)
        val all = plan.exercises.mapTo(linkedSetOf()) { it.key }
        assertTrue(SessionPolicy.allRequiredChecked(plan, all))
        all.remove("TOWEL_CURL_2")
        assertFalse(SessionPolicy.allRequiredChecked(plan, all))
    }

    @Test
    fun firstCheckMovesSessionToInProgress() {
        val plan = PlanCatalog.day(1)
        assertEquals(
            SessionStatus.IN_PROGRESS,
            SessionPolicy.nextStatusAfterToggle(setOf("ANKLE_CIRCLES"), SessionStatus.SCHEDULED)
        )
    }
}
