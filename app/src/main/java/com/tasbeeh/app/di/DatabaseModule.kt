package com.tasbeeh.app.di

import android.content.Context
import androidx.room.Room
import com.tasbeeh.app.data.local.db.TasbeehDatabase
import com.tasbeeh.app.data.local.db.dao.DhikrDao
import com.tasbeeh.app.data.local.db.dao.SessionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideTasbeehDatabase(@ApplicationContext context: Context): TasbeehDatabase {
        return Room.databaseBuilder(
            context,
            TasbeehDatabase::class.java,
            TasbeehDatabase.DATABASE_NAME
        )
            .addCallback(TasbeehDatabase.prepopulateCallback)
            .build()
    }

    @Provides
    fun provideDhikrDao(database: TasbeehDatabase): DhikrDao = database.dhikrDao()

    @Provides
    fun provideSessionDao(database: TasbeehDatabase): SessionDao = database.sessionDao()
}
