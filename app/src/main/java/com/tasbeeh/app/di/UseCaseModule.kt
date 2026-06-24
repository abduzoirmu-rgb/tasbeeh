package com.tasbeeh.app.di

import com.tasbeeh.app.domain.repository.DhikrRepository
import com.tasbeeh.app.domain.repository.SessionRepository
import com.tasbeeh.app.domain.usecase.GetDhikrsUseCase
import com.tasbeeh.app.domain.usecase.GetSessionsUseCase
import com.tasbeeh.app.domain.usecase.IncrementCounterUseCase
import com.tasbeeh.app.domain.usecase.SaveDhikrUseCase
import com.tasbeeh.app.domain.usecase.SaveSessionUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideIncrementCounterUseCase(): IncrementCounterUseCase = IncrementCounterUseCase()

    @Provides
    @Singleton
    fun provideSaveSessionUseCase(repo: SessionRepository): SaveSessionUseCase =
        SaveSessionUseCase(repo)

    @Provides
    @Singleton
    fun provideGetSessionsUseCase(repo: SessionRepository): GetSessionsUseCase =
        GetSessionsUseCase(repo)

    @Provides
    @Singleton
    fun provideGetDhikrsUseCase(repo: DhikrRepository): GetDhikrsUseCase =
        GetDhikrsUseCase(repo)

    @Provides
    @Singleton
    fun provideSaveDhikrUseCase(repo: DhikrRepository): SaveDhikrUseCase =
        SaveDhikrUseCase(repo)
}
