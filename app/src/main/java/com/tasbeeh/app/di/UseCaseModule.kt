package com.tasbeeh.app.di

import com.tasbeeh.app.domain.repository.CategoryRepository
import com.tasbeeh.app.domain.repository.DhikrRepository
import com.tasbeeh.app.domain.repository.DuaItemRepository
import com.tasbeeh.app.domain.repository.ReminderRepository
import com.tasbeeh.app.domain.repository.SessionRepository
import com.tasbeeh.app.domain.repository.SettingsRepository
import com.tasbeeh.app.domain.usecase.DeleteReminderUseCase
import com.tasbeeh.app.domain.usecase.GetCategoriesUseCase
import com.tasbeeh.app.domain.usecase.GetDhikrsUseCase
import com.tasbeeh.app.domain.usecase.GetDuasByCategoryUseCase
import com.tasbeeh.app.domain.usecase.GetRemindersUseCase
import com.tasbeeh.app.domain.usecase.GetSessionsUseCase
import com.tasbeeh.app.domain.usecase.IncrementCounterUseCase
import com.tasbeeh.app.domain.usecase.PremiumAccessUseCase
import com.tasbeeh.app.domain.usecase.SaveDhikrUseCase
import com.tasbeeh.app.domain.usecase.SaveReminderUseCase
import com.tasbeeh.app.domain.usecase.SaveSessionUseCase
import com.tasbeeh.app.domain.usecase.ToggleFavoriteUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides @Singleton fun provideIncrementCounterUseCase() = IncrementCounterUseCase()
    @Provides @Singleton fun provideSaveSessionUseCase(r: SessionRepository) = SaveSessionUseCase(r)
    @Provides @Singleton fun provideGetSessionsUseCase(r: SessionRepository) = GetSessionsUseCase(r)
    @Provides @Singleton fun provideGetDhikrsUseCase(r: DhikrRepository) = GetDhikrsUseCase(r)
    @Provides @Singleton fun provideSaveDhikrUseCase(r: DhikrRepository) = SaveDhikrUseCase(r)
    @Provides @Singleton fun provideGetCategoriesUseCase(r: CategoryRepository) = GetCategoriesUseCase(r)
    @Provides @Singleton fun provideGetDuasByCategoryUseCase(r: DuaItemRepository) = GetDuasByCategoryUseCase(r)
    @Provides @Singleton fun provideGetRemindersUseCase(r: ReminderRepository) = GetRemindersUseCase(r)
    @Provides @Singleton fun provideSaveReminderUseCase(r: ReminderRepository) = SaveReminderUseCase(r)
    @Provides @Singleton fun provideDeleteReminderUseCase(r: ReminderRepository) = DeleteReminderUseCase(r)
    @Provides @Singleton fun provideToggleFavoriteUseCase(r: DuaItemRepository) = ToggleFavoriteUseCase(r)
    @Provides @Singleton fun providePremiumAccessUseCase(r: SettingsRepository) = PremiumAccessUseCase(r)
}
