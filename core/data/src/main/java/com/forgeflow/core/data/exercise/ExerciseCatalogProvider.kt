package com.forgeflow.core.data.exercise

import android.content.Context
import com.forgeflow.core.model.Equipment
import com.forgeflow.core.model.Exercise
import com.forgeflow.core.model.ExerciseId
import com.forgeflow.core.model.ExerciseMedia
import com.forgeflow.core.model.ExerciseMediaType
import com.forgeflow.core.model.MuscleGroup
import dagger.Binds
import dagger.Module
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

interface ExerciseCatalogProvider {
    fun create(timestamp: Instant): List<Exercise>

    companion object {
        fun empty(): ExerciseCatalogProvider = EmptyExerciseCatalogProvider
    }
}

@Singleton
class AssetExerciseCatalogProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) : ExerciseCatalogProvider {
    override fun create(timestamp: Instant): List<Exercise> {
        val content = context.assets
            .open(CATALOG_ASSET)
            .bufferedReader()
            .use { it.readText() }
        return decodeExerciseCatalog(content, timestamp)
    }

    private companion object {
        private const val CATALOG_ASSET = "exercise_catalog.json"
    }
}

internal fun decodeExerciseCatalog(
    content: String,
    timestamp: Instant,
): List<Exercise> {
    val items = Json.decodeFromString<List<CatalogExercise>>(content)
    require(items.isNotEmpty()) { "Exercise catalog cannot be empty" }
    require(items.map(CatalogExercise::id).distinct().size == items.size) {
        "Exercise catalog contains duplicate identifiers"
    }
    require(items.map(CatalogExercise::name).distinct().size == items.size) {
        "Exercise catalog contains duplicate names"
    }
    return items.map { item ->
        Exercise(
            id = ExerciseId(item.id),
            name = item.name,
            primaryMuscleGroup = MuscleGroup.valueOf(item.primaryMuscleGroup),
            secondaryMuscleGroups = item.secondaryMuscleGroups
                .mapTo(linkedSetOf(), MuscleGroup::valueOf),
            equipment = Equipment.valueOf(item.equipment),
            instructions = item.instructions,
            media = item.mediaUri?.let { uri ->
                ExerciseMedia(
                    uri = uri,
                    type = ExerciseMediaType.ANIMATED_IMAGE,
                )
            },
            isCustom = false,
            createdAt = timestamp,
            updatedAt = timestamp,
        )
    }
}

private data object EmptyExerciseCatalogProvider : ExerciseCatalogProvider {
    override fun create(timestamp: Instant): List<Exercise> = emptyList()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class ExerciseCatalogModule {
    @Binds
    abstract fun bindExerciseCatalogProvider(
        implementation: AssetExerciseCatalogProvider,
    ): ExerciseCatalogProvider
}

@Serializable
internal data class CatalogExercise(
    val id: String,
    val name: String,
    val primaryMuscleGroup: String,
    val secondaryMuscleGroups: List<String>,
    val equipment: String,
    val instructions: String,
    val mediaUri: String?,
)
