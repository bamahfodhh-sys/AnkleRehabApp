package com.sanad.anklerehab.data

import com.sanad.anklerehab.data.db.ExerciseCheckEntity
import androidx.room.withTransaction
import com.sanad.anklerehab.data.db.RehabDatabase
import com.sanad.anklerehab.data.db.SessionRecordEntity
import com.sanad.anklerehab.data.settings.SettingsRepository
import com.sanad.anklerehab.domain.PlanCatalog
import com.sanad.anklerehab.domain.RehabSettings
import com.sanad.anklerehab.domain.SessionPolicy
import com.sanad.anklerehab.domain.SessionStatus
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class RehabRepository(
    private val database: RehabDatabase,
    val settingsRepository: SettingsRepository
) {
    private val dao = database.rehabDao()
    val records: Flow<List<SessionRecordEntity>> = dao.observeSessionRecords()
    val checks: Flow<List<ExerciseCheckEntity>> = dao.observeExerciseChecks()
    val settings: Flow<RehabSettings> = settingsRepository.settings

    suspend fun initialize(today: LocalDate = LocalDate.now()) = settingsRepository.ensureInitialized(today)

    suspend fun getRecord(day: Int): SessionRecordEntity? = dao.getSessionRecord(day)

    suspend fun getCheckedKeys(day: Int): Set<String> =
        dao.getCheckedExercises(day).filter { it.checked }.mapTo(linkedSetOf()) { it.exerciseKey }

    suspend fun setExerciseChecked(programDay: Int, exerciseKey: String, checked: Boolean) {
        database.withTransaction {
            dao.upsertExerciseCheck(
                ExerciseCheckEntity(programDay, exerciseKey, checked, System.currentTimeMillis())
            )
            val checkedKeys = dao.getCheckedExercises(programDay).filter { it.checked }.mapTo(linkedSetOf()) { it.exerciseKey }
            val current = dao.getSessionRecord(programDay)
            val status = SessionPolicy.nextStatusAfterToggle(
                checkedKeys,
                current?.status?.let { runCatching { SessionStatus.valueOf(it) }.getOrNull() }
            )
            if (current?.status != SessionStatus.COMPLETED.name &&
                current?.status != SessionStatus.STOPPED_DUE_TO_SYMPTOMS.name &&
                current?.status != SessionStatus.SKIPPED.name
            ) {
                dao.upsertSessionRecord(
                    (current ?: SessionRecordEntity(programDay, status.name)).copy(
                        status = status.name,
                        lastUpdatedAtMillis = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    suspend fun updateNote(programDay: Int, note: String) {
        val current = dao.getSessionRecord(programDay)
        dao.upsertSessionRecord(
            (current ?: SessionRecordEntity(programDay, SessionStatus.SCHEDULED.name)).copy(
                note = note.take(1000),
                lastUpdatedAtMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun markCompleted(programDay: Int, phase2Days: Int): Boolean = database.withTransaction {
        val plan = PlanCatalog.day(programDay, phase2Days)
        val checked = dao.getCheckedExercises(programDay).filter { it.checked }.mapTo(linkedSetOf()) { it.exerciseKey }
        if (!SessionPolicy.allRequiredChecked(plan, checked)) return@withTransaction false
        val current = dao.getSessionRecord(programDay)
        val now = System.currentTimeMillis()
        dao.upsertSessionRecord(
            (current ?: SessionRecordEntity(programDay, SessionStatus.COMPLETED.name)).copy(
                status = SessionStatus.COMPLETED.name,
                completedAtMillis = now,
                lastUpdatedAtMillis = now
            )
        )
        true
    }

    suspend fun markStatus(programDay: Int, status: SessionStatus) {
        require(status in setOf(SessionStatus.PARTIAL, SessionStatus.SKIPPED, SessionStatus.STOPPED_DUE_TO_SYMPTOMS))
        val current = dao.getSessionRecord(programDay)
        dao.upsertSessionRecord(
            (current ?: SessionRecordEntity(programDay, status.name)).copy(
                status = status.name,
                completedAtMillis = null,
                lastUpdatedAtMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun reopen(programDay: Int) {
        val current = dao.getSessionRecord(programDay) ?: return
        val checked = getCheckedKeys(programDay)
        dao.upsertSessionRecord(
            current.copy(
                status = if (checked.isEmpty()) SessionStatus.SCHEDULED.name else SessionStatus.IN_PROGRESS.name,
                completedAtMillis = null,
                lastUpdatedAtMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun clearDay(programDay: Int) = database.withTransaction {
        dao.clearChecksForDay(programDay)
        dao.upsertSessionRecord(
            SessionRecordEntity(
                programDay = programDay,
                status = SessionStatus.SCHEDULED.name,
                note = "",
                completedAtMillis = null,
                lastUpdatedAtMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun confirmPhaseStart(phase: Int, date: LocalDate) = settingsRepository.confirmPhaseStart(phase, date)

    suspend fun shiftProgramStart(newStart: LocalDate) = settingsRepository.shiftProgramStart(newStart)

    suspend fun resetProgram(newStart: LocalDate) {
        database.withTransaction {
            dao.clearExerciseChecks()
            dao.clearSessionRecords()
        }
        settingsRepository.resetProgram(newStart)
    }
}
