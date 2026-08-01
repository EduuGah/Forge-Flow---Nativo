package com.forgeflow.core.data.exercise

import android.content.Context
import android.net.Uri
import com.forgeflow.core.common.di.IoDispatcher
import com.forgeflow.core.model.ExerciseId
import com.forgeflow.core.model.ExerciseMedia
import com.forgeflow.core.model.ExerciseMediaType
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

interface ExerciseMediaStore {
    suspend fun importPhoto(exerciseId: ExerciseId, sourceUri: String): ExerciseMedia

    suspend fun delete(exerciseId: ExerciseId)

    companion object {
        val None: ExerciseMediaStore = object : ExerciseMediaStore {
            override suspend fun importPhoto(
                exerciseId: ExerciseId,
                sourceUri: String,
            ): ExerciseMedia = ExerciseMedia(sourceUri, ExerciseMediaType.IMAGE)

            override suspend fun delete(exerciseId: ExerciseId) = Unit
        }
    }
}

@Singleton
class LocalExerciseMediaStore @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ExerciseMediaStore {
    override suspend fun importPhoto(
        exerciseId: ExerciseId,
        sourceUri: String,
    ): ExerciseMedia = withContext(ioDispatcher) {
        val source = Uri.parse(sourceUri)
        val directory = File(context.filesDir, DIRECTORY_NAME)
        val extension = when (context.contentResolver.getType(source)) {
            "image/png" -> "png"
            "image/webp" -> "webp"
            else -> "jpg"
        }
        directory.mkdirs()
        require(directory.isDirectory)

        val temporary = File.createTempFile("${exerciseId.value}-", ".$extension", directory)
        try {
            context.contentResolver.openInputStream(source).use { input ->
                requireNotNull(input)
                temporary.outputStream().use(input::copyTo)
            }
            require(temporary.length() > 0)

            val target = File(directory, "${exerciseId.value}.$extension")
            directory.listFiles()
                .orEmpty()
                .filter { it != temporary && it.name.startsWith(exerciseId.value) }
                .forEach(File::delete)
            if (!temporary.renameTo(target)) {
                temporary.copyTo(target, overwrite = true)
                temporary.delete()
            }
            ExerciseMedia(
                uri = target.absolutePath,
                type = ExerciseMediaType.IMAGE,
            )
        } catch (error: Throwable) {
            temporary.delete()
            throw error
        }
    }

    override suspend fun delete(exerciseId: ExerciseId) = withContext(ioDispatcher) {
        File(context.filesDir, DIRECTORY_NAME)
            .listFiles()
            .orEmpty()
            .filter { it.name.startsWith(exerciseId.value) }
            .forEach(File::delete)
    }

    private companion object {
        const val DIRECTORY_NAME = "exercise_media"
    }
}
