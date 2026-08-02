package com.forgeflow.app.auth

import android.content.Context
import com.forgeflow.core.model.ExperienceLevel
import com.forgeflow.core.model.TrainingGoal
import com.forgeflow.core.model.UserProfile
import com.forgeflow.core.model.Weight
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

@Singleton
class FirebaseProfileSyncRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    suspend fun download(userId: String): UserProfile? {
        if (!isConfigured()) return null
        return suspendCancellableCoroutine { continuation ->
            profileDocument(userId).get().addOnCompleteListener { task ->
                if (!continuation.isActive) return@addOnCompleteListener
                val snapshot = task.takeIf { it.isSuccessful }?.result
                val profile = snapshot
                    ?.takeIf { it.exists() }
                    ?.let {
                        UserProfile(
                            ownerUserId = userId,
                            displayName = it.getString(DISPLAY_NAME).orEmpty(),
                            birthYear = it.getLong(BIRTH_YEAR)?.toInt(),
                            heightCentimeters = it.getLong(HEIGHT_CENTIMETERS)?.toInt(),
                            bodyWeight = it.getLong(BODY_WEIGHT_GRAMS)?.let(Weight::fromGrams),
                            trainingGoal = it.getString(TRAINING_GOAL)
                                ?.let { stored ->
                                    TrainingGoal.entries.firstOrNull { entry ->
                                        entry.name == stored
                                    }
                                }
                                ?: TrainingGoal.HYPERTROPHY,
                            experienceLevel = it.getString(EXPERIENCE_LEVEL)
                                ?.let { stored ->
                                    ExperienceLevel.entries.firstOrNull { entry ->
                                        entry.name == stored
                                    }
                                }
                                ?: ExperienceLevel.INTERMEDIATE,
                        )
                    }
                continuation.resume(profile)
            }
        }
    }

    suspend fun upload(userId: String, profile: UserProfile): Boolean {
        if (!isConfigured()) return false
        val data = mapOf(
            SCHEMA_VERSION to CURRENT_SCHEMA_VERSION,
            DISPLAY_NAME to profile.displayName.trim(),
            BIRTH_YEAR to profile.birthYear,
            HEIGHT_CENTIMETERS to profile.heightCentimeters,
            BODY_WEIGHT_GRAMS to profile.bodyWeight?.grams,
            TRAINING_GOAL to profile.trainingGoal.name,
            EXPERIENCE_LEVEL to profile.experienceLevel.name,
            UPDATED_AT_EPOCH_MILLIS to System.currentTimeMillis(),
        )
        return suspendCancellableCoroutine { continuation ->
            profileDocument(userId).set(data, SetOptions.merge()).addOnCompleteListener { task ->
                if (continuation.isActive) continuation.resume(task.isSuccessful)
            }
        }
    }

    private fun profileDocument(userId: String) = FirebaseFirestore.getInstance()
        .collection(USERS_COLLECTION)
        .document(userId)
        .collection(PROFILE_COLLECTION)
        .document(MAIN_PROFILE_DOCUMENT)

    private fun isConfigured(): Boolean = FirebaseApp.getApps(context).isNotEmpty()

    private companion object {
        const val USERS_COLLECTION = "users"
        const val PROFILE_COLLECTION = "profile"
        const val MAIN_PROFILE_DOCUMENT = "main"
        const val CURRENT_SCHEMA_VERSION = 1
        const val SCHEMA_VERSION = "schemaVersion"
        const val DISPLAY_NAME = "displayName"
        const val BIRTH_YEAR = "birthYear"
        const val HEIGHT_CENTIMETERS = "heightCentimeters"
        const val BODY_WEIGHT_GRAMS = "bodyWeightGrams"
        const val TRAINING_GOAL = "trainingGoal"
        const val EXPERIENCE_LEVEL = "experienceLevel"
        const val UPDATED_AT_EPOCH_MILLIS = "updatedAtEpochMillis"
    }
}
