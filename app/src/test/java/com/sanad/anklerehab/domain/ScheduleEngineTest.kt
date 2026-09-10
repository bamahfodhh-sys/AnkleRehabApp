package com.sanad.anklerehab.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ScheduleEngineTest {
    private val start = LocalDate.of(2026, 9, 1)

    @Test
    fun phase2_doesNotAutoUnlockByCalendar() {
        val settings = RehabSettings(startDate = start)
        val state = ScheduleEngine.state(start.plusDays(14), settings)
        assertNull(state.todayProgramDay)
        assertEquals(2, state.transitionPendingToPhase)
    }

    @Test
    fun phase2_startsFromConfirmationDate() {
        val p2Start = LocalDate.of(2026, 9, 20)
        val settings = RehabSettings(startDate = start, phase2StartDate = p2Start)
        assertEquals(15, ScheduleEngine.state(p2Start, settings).todayProgramDay)
        assertEquals(p2Start, ScheduleEngine.scheduledDate(15, settings))
    }

    @Test
    fun phase3_requiresSecondConfirmation() {
        val p2Start = LocalDate.of(2026, 9, 20)
        val settings = RehabSettings(startDate = start, phase2StartDate = p2Start)
        val state = ScheduleEngine.state(p2Start.plusDays(14), settings)
        assertEquals(3, state.transitionPendingToPhase)
    }

    @Test
    fun futureSessions_areReadOnly() {
        val settings = RehabSettings(startDate = start)
        assertFalse(ScheduleEngine.canEditSession(5, start, settings))
        assertTrue(ScheduleEngine.canEditSession(1, start, settings))
    }

    @Test
    fun programCompletesOnlyAfterPhase3Duration() {
        val p2 = LocalDate.of(2026, 9, 20)
        val p3 = LocalDate.of(2026, 10, 10)
        val settings = RehabSettings(startDate = start, phase2StartDate = p2, phase3StartDate = p3)
        assertFalse(ScheduleEngine.state(p3.plusDays(13), settings).programComplete)
        assertTrue(ScheduleEngine.state(p3.plusDays(14), settings).programComplete)
    }
}
