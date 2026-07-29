package com.forgeflow.core.data.exercise

import com.forgeflow.core.common.di.ApplicationScope
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Singleton
class ExerciseSeedInitializer @Inject constructor(
    private val repository: ExerciseRepository,
    @param:ApplicationScope private val applicationScope: CoroutineScope,
) {
    fun initialize() {
        applicationScope.launch {
            repository.seedDefaults()
        }
    }
}
