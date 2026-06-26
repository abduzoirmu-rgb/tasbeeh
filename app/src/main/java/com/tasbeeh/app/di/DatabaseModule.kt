package com.tasbeeh.app.di

import android.content.Context
import androidx.room.Room
import com.tasbeeh.app.data.local.db.TasbeehDatabase
import com.tasbeeh.app.data.local.db.dao.CategoryDao
import com.tasbeeh.app.data.local.db.dao.DhikrDao
import com.tasbeeh.app.data.local.db.dao.DuaItemDao
import com.tasbeeh.app.data.local.db.dao.ReminderDao
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
    fun provideTasbeehDatabase(@ApplicationContext context: Context): TasbeehDatabase =
        Room.databaseBuilder(
            context,
            TasbeehDatabase::class.java,
            TasbeehDatabase.DATABASE_NAME
        )
            .addMigrations(TasbeehDatabase.MIGRATION_1_2)
            .addCallback(TasbeehDatabase.prepopulateCallback)
            .build()

    @Provides fun provideDhikrDao(db: TasbeehDatabase): DhikrDao = db.dhikrDao()
    @Provides fun provideSessionDao(db: TasbeehDatabase): SessionDao = db.sessionDao()
    @Provides fun provideCategoryDao(db: TasbeehDatabase): CategoryDao = db.categoryDao()
    @Provides fun provideDuaItemDao(db: TasbeehDatabase): DuaItemDao = db.duaItemDao()
    @Provides fun provideReminderDao(db: TasbeehDatabase): ReminderDao = db.reminderDao()
}
