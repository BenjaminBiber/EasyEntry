package com.easyentry.app.di

import com.easyentry.app.data.repository.DeviceGroupRepository
import com.easyentry.app.data.repository.DeviceGroupRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindDeviceGroupRepository(
        impl: DeviceGroupRepositoryImpl
    ): DeviceGroupRepository
}
