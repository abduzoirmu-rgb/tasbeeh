package com.tasbeeh.app.di

import com.tasbeeh.app.data.repository.CategoryRepositoryImpl
import com.tasbeeh.app.data.repository.DhikrRepositoryImpl
import com.tasbeeh.app.data.repository.DuaItemRepositoryImpl
import com.tasbeeh.app.data.repository.ReminderRepositoryImpl
import com.tasbeeh.app.data.repository.SessionRepositoryImpl
import com.tasbeeh.app.data.repository.SettingsRepositoryImpl
import com.tasbeeh.app.domain.repository.CategoryRepository
import com.tasbeeh.app.domain.repository.DhikrRepository
import com.tasbeeh.app.domain.repository.DuaItemRepository
import com.tasbeeh.app.domain.repository.ReminderRepository
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

    @Binds @Singleton abstract fun bindDhikrRepository(impl: DhikrRepositoryImpl): DhikrRepository
    @Binds @Singleton abstract fun bindSessionRepository(impl: SessionRepositoryImpl): SessionRepository
    @Binds @Singleton abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
    @Binds @Singleton abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository
    @Binds @Singleton abstract fun bindDuaItemRepository(impl: DuaItemRepositoryImpl): DuaItemRepository
    @Binds @Singleton abstract fun bindReminderRepository(impl: ReminderRepositoryImpl): ReminderRepository
}
