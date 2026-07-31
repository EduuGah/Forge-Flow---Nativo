package com.forgeflow.core.data.profile

import com.forgeflow.core.common.result.DataResult
import com.forgeflow.core.model.BodyWeightEntry
import com.forgeflow.core.model.UserProfile
import com.forgeflow.core.model.Weight
import java.time.Instant
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun observeProfile(): Flow<UserProfile>

    suspend fun saveProfile(profile: UserProfile): DataResult<Unit>

    suspend fun importProfilePhoto(sourceUri: String): DataResult<Unit>

    suspend fun addBodyWeight(
        weight: Weight,
        measuredAt: Instant,
        bodyFatPercent: Double? = null,
    ): DataResult<Unit>

    suspend fun mergeBodyWeightEntries(entries: List<BodyWeightEntry>): DataResult<Int>

    suspend fun importProgressPhoto(sourceUri: String): DataResult<Unit>

    suspend fun deleteProgressPhoto(photoId: String): DataResult<Unit>
}
