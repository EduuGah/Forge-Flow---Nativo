package com.forgeflow.core.database.di

import android.content.Context
import androidx.room.Room
import com.forgeflow.core.database.ForgeFlowDatabase
import com.forgeflow.core.database.MigrationRegistry
import com.forgeflow.core.database.exercise.ExerciseDao
import com.forgeflow.core.database.routine.RoutineDao
import com.forgeflow.core.database.workout.WorkoutDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): ForgeFlowDatabase = Room.databaseBuilder(
        context,
        ForgeFlowDatabase::class.java,
        ForgeFlowDatabase.NAME,
    )
        .addMigrations(*MigrationRegistry.all)
        .build()

    @Provides
    fun provideExerciseDao(database: ForgeFlowDatabase): ExerciseDao = database.exerciseDao()

    @Provides
    fun provideRoutineDao(database: ForgeFlowDatabase): RoutineDao = database.routineDao()

    @Provides
    fun provideWorkoutDao(database: ForgeFlowDatabase): WorkoutDao = database.workoutDao()
}
