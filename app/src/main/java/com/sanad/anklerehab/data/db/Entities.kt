package com.sanad.anklerehab.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "session_records")
data class SessionRecordEntity(
    @PrimaryKey val programDay: Int,
    val status: String,
    val note: String = "",
    val completedAtMillis: Long? = null,
    val lastUpdatedAtMillis: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "exercise_checks",
    primaryKeys = ["programDay", "exerciseKey"]
)
data class ExerciseCheckEntity(
    val programDay: Int,
    val exerciseKey: String,
    val checked: Boolean,
    val updatedAtMillis: Long = System.currentTimeMillis()
)
