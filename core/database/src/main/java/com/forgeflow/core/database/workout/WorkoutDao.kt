package com.forgeflow.core.database.workout

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE status = 'ACTIVE' LIMIT 1")
    fun observeActive(): Flow<WorkoutRecord?>

    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE status = 'ACTIVE' LIMIT 1")
    suspend fun getActive(): WorkoutRecord?

    @Transaction
    @Query(
        """
        SELECT * FROM workout_sessions
        WHERE status = 'COMPLETED'
        ORDER BY finished_at_epoch_millis DESC
        """,
    )
    fun observeHistory(): Flow<List<WorkoutRecord>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSession(session: WorkoutSessionEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertExercises(exercises: List<WorkoutSessionExerciseEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSets(sets: List<WorkoutSetEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSet(set: WorkoutSetEntity)

    @Query("SELECT * FROM workout_sets WHERE id = :setId LIMIT 1")
    suspend fun getSet(setId: String): WorkoutSetEntity?

    @Query(
        """
        SELECT COALESCE(MAX(position), -1)
        FROM workout_sets
        WHERE session_exercise_id = :sessionExerciseId
        """,
    )
    suspend fun getLastSetPosition(sessionExerciseId: String): Int

    @Query(
        """
        SELECT * FROM workout_sets
        WHERE session_exercise_id = :sessionExerciseId
        ORDER BY position DESC
        LIMIT 1
        """,
    )
    suspend fun getLastSet(sessionExerciseId: String): WorkoutSetEntity?

    @Query(
        """
        UPDATE workout_sets
        SET weight_grams = :weightGrams,
            repetitions = :repetitions,
            is_completed = :isCompleted,
            completed_at_epoch_millis = :completedAt,
            updated_at_epoch_millis = :updatedAt
        WHERE id = :setId
        """,
    )
    suspend fun updateSet(
        setId: String,
        weightGrams: Long,
        repetitions: Int,
        isCompleted: Boolean,
        completedAt: Long?,
        updatedAt: Long,
    )

    @Query(
        """
        UPDATE workout_sessions
        SET status = 'DISCARDED',
            finished_at_epoch_millis = :timestamp,
            updated_at_epoch_millis = :timestamp
        WHERE status = 'ACTIVE'
        """,
    )
    suspend fun discardActive(timestamp: Long)

    @Query(
        """
        UPDATE workout_sessions
        SET status = 'COMPLETED',
            finished_at_epoch_millis = :timestamp,
            updated_at_epoch_millis = :timestamp
        WHERE id = :sessionId AND status = 'ACTIVE'
        """,
    )
    suspend fun finish(sessionId: String, timestamp: Long): Int

    @Transaction
    suspend fun replaceActive(
        timestamp: Long,
        session: WorkoutSessionEntity,
        exercises: List<WorkoutSessionExerciseEntity>,
        sets: List<WorkoutSetEntity>,
    ) {
        discardActive(timestamp)
        insertSession(session)
        insertExercises(exercises)
        insertSets(sets)
    }
}
