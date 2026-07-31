package com.forgeflow.core.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.forgeflow.core.common.di.ApplicationScope
import com.forgeflow.core.data.exercise.DefaultExerciseRepository
import com.forgeflow.core.data.exercise.ExerciseLocalDataSource
import com.forgeflow.core.data.exercise.ExerciseRepository
import com.forgeflow.core.data.exercise.RoomExerciseLocalDataSource
import com.forgeflow.core.data.profile.DataStoreProfileRepository
import com.forgeflow.core.data.profile.ProfileRepository
import com.forgeflow.core.data.nutrition.DataStoreNutritionRepository
import com.forgeflow.core.data.nutrition.NutritionRepository
import com.forgeflow.core.data.routine.DefaultRoutineRepository
import com.forgeflow.core.data.routine.RoutineRepository
import com.forgeflow.core.data.settings.DataStoreSettingsRepository
import com.forgeflow.core.data.settings.SettingsRepository
import com.forgeflow.core.data.workout.DefaultWorkoutRepository
import com.forgeflow.core.data.workout.WorkoutRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindExerciseLocalDataSource(
        implementation: RoomExerciseLocalDataSource,
    ): ExerciseLocalDataSource

    @Binds
    @Singleton
    abstract fun bindExerciseRepository(
        implementation: DefaultExerciseRepository,
    ): ExerciseRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        implementation: DataStoreSettingsRepository,
    ): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindProfileRepository(
        implementation: DataStoreProfileRepository,
    ): ProfileRepository

    @Binds
    @Singleton
    abstract fun bindNutritionRepository(
        implementation: DataStoreNutritionRepository,
    ): NutritionRepository

    @Binds
    @Singleton
    abstract fun bindRoutineRepository(
        implementation: DefaultRoutineRepository,
    ): RoutineRepository

    @Binds
    @Singleton
    abstract fun bindWorkoutRepository(
        implementation: DefaultWorkoutRepository,
    ): WorkoutRepository
}

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {
    @Provides
    @Singleton
    fun providePreferencesDataStore(
        @ApplicationContext context: Context,
        @ApplicationScope applicationScope: CoroutineScope,
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        scope = applicationScope,
        produceFile = { context.preferencesDataStoreFile(SETTINGS_FILE_NAME) },
    )

    private const val SETTINGS_FILE_NAME = "forgeflow_settings.preferences_pb"
}
