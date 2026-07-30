package com.forgeflow.core.data.profile

import com.forgeflow.core.common.result.DataResult
import com.forgeflow.core.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun observeProfile(): Flow<UserProfile>

    suspend fun saveProfile(profile: UserProfile): DataResult<Unit>

    suspend fun importProgressPhoto(sourceUri: String): DataResult<Unit>

    suspend fun deleteProgressPhoto(photoId: String): DataResult<Unit>
}
