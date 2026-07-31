package com.forgeflow.core.database.workout

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

data class ImportedWorkoutEntities(
    val session: WorkoutSessionEntity,
    val exercises: List<WorkoutSessionExerciseEntity>,
    val sets: List<WorkoutSetEntity>,
)

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
    suspend fun insertExercise(exercise: WorkoutSessionExerciseEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSets(sets: List<WorkoutSetEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSet(set: WorkoutSetEntity)

    @Query("SELECT COUNT(*) FROM workout_sessions WHERE id = :sessionId")
    suspend fun sessionCount(sessionId: String): Int

    @Transaction
    suspend fun insertImportedWorkout(workout: ImportedWorkoutEntities): Boolean {
        if (sessionCount(workout.session.id) > 0) return false
        insertSession(workout.session)
        insertExercises(workout.exercises)
        insertSets(workout.sets)
        return true
    }

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

    @Query("UPDATE workout_sets SET set_type = :setType, updated_at_epoch_millis = :updatedAt WHERE id = :setId")
    suspend fun updateSetType(setId: String, setType: String, updatedAt: Long)

    @Query("DELETE FROM workout_sets WHERE id = :setId")
    suspend fun deleteSet(setId: String)

    @Query("SELECT * FROM workout_session_exercises WHERE id = :id LIMIT 1")
    suspend fun getSessionExercise(id: String): WorkoutSessionExerciseEntity?

    @Query(
        "SELECT * FROM workout_session_exercises WHERE session_id = :sessionId ORDER BY position",
    )
    suspend fun getSessionExercises(sessionId: String): List<WorkoutSessionExerciseEntity>

    @Query(
        "SELECT COALESCE(MAX(position), -1) FROM workout_session_exercises WHERE session_id = :sessionId",
    )
    suspend fun getLastExercisePosition(sessionId: String): Int

    @Query("UPDATE workout_session_exercises SET notes = :notes WHERE id = :id")
    suspend fun updateExerciseNotes(id: String, notes: String)

    @Query(
        """
        UPDATE workout_session_exercises
        SET exercise_id = :exerciseId,
            exercise_name_snapshot = :name,
            muscle_group_snapshot = :muscleGroup,
            media_uri_snapshot = :mediaUri,
            media_type_snapshot = :mediaType,
            media_thumbnail_uri_snapshot = :mediaThumbnailUri
        WHERE id = :id
        """,
    )
    suspend fun replaceExercise(
        id: String,
        exerciseId: String,
        name: String,
        muscleGroup: String,
        mediaUri: String?,
        mediaType: String?,
        mediaThumbnailUri: String?,
    )

    @Query("DELETE FROM workout_session_exercises WHERE id = :id")
    suspend fun deleteExercise(id: String)

    @Query("UPDATE workout_session_exercises SET position = :position WHERE id = :id")
    suspend fun updateExercisePosition(id: String, position: Int)

    @Transaction
    suspend fun moveExercise(id: String, direction: Int) {
        val exercise = getSessionExercise(id) ?: return
        val ordered = getSessionExercises(exercise.sessionId)
        val currentIndex = ordered.indexOfFirst { it.id == id }
        if (currentIndex < 0 || ordered.isEmpty()) return
        val targetIndex = (currentIndex + direction).coerceIn(0, ordered.lastIndex)
        if (targetIndex == currentIndex) return
        val target = ordered[targetIndex]
        updateExercisePosition(exercise.id, -1)
        updateExercisePosition(target.id, exercise.position)
        updateExercisePosition(exercise.id, target.position)
    }

    @Query("DELETE FROM workout_sessions WHERE id = :sessionId AND status = 'COMPLETED'")
    suspend fun deleteCompletedWorkout(sessionId: String): Int

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
            location_latitude = :locationLatitude,
            location_longitude = :locationLongitude,
            location_accuracy_meters = :locationAccuracyMeters,
            location_captured_at_epoch_millis = :locationCapturedAt,
            location_label = :locationLabel,
            updated_at_epoch_millis = :timestamp
        WHERE id = :sessionId AND status = 'ACTIVE'
        """,
    )
    suspend fun finish(
        sessionId: String,
        timestamp: Long,
        locationLatitude: Double?,
        locationLongitude: Double?,
        locationAccuracyMeters: Float?,
        locationCapturedAt: Long?,
        locationLabel: String?,
    ): Int

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
