package com.forgeflow.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.forgeflow.core.database.exercise.ExerciseDao
import com.forgeflow.core.database.exercise.ExerciseEntity
import com.forgeflow.core.database.routine.RoutineDao
import com.forgeflow.core.database.routine.RoutineEntity
import com.forgeflow.core.database.routine.RoutineExerciseEntity
import com.forgeflow.core.database.workout.WorkoutDao
import com.forgeflow.core.database.workout.WorkoutSessionEntity
import com.forgeflow.core.database.workout.WorkoutSessionExerciseEntity
import com.forgeflow.core.database.workout.WorkoutSetEntity

@Database(
    entities = [
        ExerciseEntity::class,
        RoutineEntity::class,
        RoutineExerciseEntity::class,
        WorkoutSessionEntity::class,
        WorkoutSessionExerciseEntity::class,
        WorkoutSetEntity::class,
    ],
    version = 4,
    exportSchema = true,
)
abstract class ForgeFlowDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao

    abstract fun routineDao(): RoutineDao

    abstract fun workoutDao(): WorkoutDao

    companion object {
        const val NAME = "forgeflow.db"
    }
}
