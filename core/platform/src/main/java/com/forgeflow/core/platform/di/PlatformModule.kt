package com.forgeflow.core.platform.di

import com.forgeflow.core.platform.location.AndroidCurrentLocationProvider
import com.forgeflow.core.platform.location.CurrentLocationProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PlatformModule {
    @Binds
    @Singleton
    abstract fun bindCurrentLocationProvider(
        implementation: AndroidCurrentLocationProvider,
    ): CurrentLocationProvider
}
