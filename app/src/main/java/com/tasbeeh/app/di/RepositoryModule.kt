package com.tasbeeh.app.di

import com.tasbeeh.app.data.local.datastore.SettingsDataStore
import com.tasbeeh.app.data.repository.DhikrRepositoryImpl
import com.tasbeeh.app.data.repository.SessionRepositoryImpl
import com.tasbeeh.app.domain.repository.DhikrRepository
import com.tasbeeh.app.domain.repository.SessionRepository
import com.tasbeeh.app.domain.repository.SettingsRepository
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
    abstract fun bindDhikrRepository(impl: DhikrRepositoryImpl): DhikrRepository

    @Binds
    @Singleton
    abstract fun bindSessionRepository(impl: SessionRepositoryImpl): SessionRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsDataStore): SettingsRepository
}
