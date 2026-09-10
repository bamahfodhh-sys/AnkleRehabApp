package com.sanad.anklerehab.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RehabDao {
    @Query("SELECT * FROM session_records ORDER BY programDay")
    fun observeSessionRecords(): Flow<List<SessionRecordEntity>>

    @Query("SELECT * FROM exercise_checks ORDER BY programDay, exerciseKey")
    fun observeExerciseChecks(): Flow<List<ExerciseCheckEntity>>

    @Query("SELECT * FROM session_records WHERE programDay = :programDay LIMIT 1")
    suspend fun getSessionRecord(programDay: Int): SessionRecordEntity?

    @Query("SELECT * FROM exercise_checks WHERE programDay = :programDay AND checked = 1")
    suspend fun getCheckedExercises(programDay: Int): List<ExerciseCheckEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSessionRecord(record: SessionRecordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertExerciseCheck(check: ExerciseCheckEntity)

    @Query("DELETE FROM exercise_checks WHERE programDay = :programDay")
    suspend fun clearChecksForDay(programDay: Int)

    @Query("DELETE FROM session_records")
    suspend fun clearSessionRecords()

    @Query("DELETE FROM exercise_checks")
    suspend fun clearExerciseChecks()
}
