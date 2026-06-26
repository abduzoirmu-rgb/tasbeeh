package com.tasbeeh.app.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.tasbeeh.app.data.entity.CategoryEntity
import com.tasbeeh.app.data.entity.DhikrEntity
import com.tasbeeh.app.data.entity.DuaItemEntity
import com.tasbeeh.app.data.entity.ReminderEntity
import com.tasbeeh.app.data.entity.SessionEntity
import com.tasbeeh.app.data.local.db.dao.CategoryDao
import com.tasbeeh.app.data.local.db.dao.DhikrDao
import com.tasbeeh.app.data.local.db.dao.DuaItemDao
import com.tasbeeh.app.data.local.db.dao.ReminderDao
import com.tasbeeh.app.data.local.db.dao.SessionDao

@Database(
    entities = [
        DhikrEntity::class,
        SessionEntity::class,
        CategoryEntity::class,
        DuaItemEntity::class,
        ReminderEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class TasbeehDatabase : RoomDatabase() {

    abstract fun dhikrDao(): DhikrDao
    abstract fun sessionDao(): SessionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun duaItemDao(): DuaItemDao
    abstract fun reminderDao(): ReminderDao

    companion object {
        const val DATABASE_NAME = "tasbeeh.db"

        val PREPOPULATE_CALLBACK = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                db.execSQL("INSERT INTO dhikrs (id, name, arabicText, targetCount, isCustom) VALUES (1, 'Субханаллах', 'سُبْحَانَ ٱللَّٰهِ', 33, 0)")
                db.execSQL("INSERT INTO dhikrs (id, name, arabicText, targetCount, isCustom) VALUES (2, 'Альхамдулиллах', 'ٱلْحَمْدُ لِلَّٰهِ', 33, 0)")
                db.execSQL("INSERT INTO dhikrs (id, name, arabicText, targetCount, isCustom) VALUES (3, 'Аллаху Акбар', 'ٱللَّٰهُ أَكْبَرُ', 34, 0)")
                db.execSQL("INSERT INTO dhikrs (id, name, arabicText, targetCount, isCustom) VALUES (4, 'Астагфируллах', 'أَسْتَغْفِرُ ٱللَّٰهَ', 100, 0)")
                db.execSQL("INSERT INTO dhikrs (id, name, arabicText, targetCount, isCustom) VALUES (5, 'Ля иляха илляллах', 'لَا إِلَٰهَ إِلَّا ٱللَّٰهُ', 100, 0)")
            }
        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `categories` (
                        `id` INTEGER NOT NULL,
                        `title` TEXT NOT NULL,
                        `icon` TEXT NOT NULL,
                        `order` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())

                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `dua_items` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `categoryId` INTEGER NOT NULL,
                        `arabicText` TEXT NOT NULL,
                        `transliteration` TEXT NOT NULL,
                        `translationRu` TEXT NOT NULL,
                        `source` TEXT,
                        `repeatCount` INTEGER,
                        `isFavorite` INTEGER NOT NULL DEFAULT 0,
                        FOREIGN KEY(`categoryId`) REFERENCES `categories`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """.trimIndent())
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_dua_items_categoryId` ON `dua_items` (`categoryId`)")

                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `reminders` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `title` TEXT NOT NULL,
                        `timeHour` INTEGER NOT NULL,
                        `timeMinute` INTEGER NOT NULL,
                        `daysOfWeek` TEXT NOT NULL,
                        `isEnabled` INTEGER NOT NULL,
                        `linkedZikrId` INTEGER
                    )
                """.trimIndent())
            }
        }

        val prepopulateCallback get() = PREPOPULATE_CALLBACK
    }
}
