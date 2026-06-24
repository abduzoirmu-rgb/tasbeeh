package com.tasbeeh.app.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.tasbeeh.app.data.entity.DhikrEntity
import com.tasbeeh.app.data.entity.SessionEntity
import com.tasbeeh.app.data.local.db.dao.DhikrDao
import com.tasbeeh.app.data.local.db.dao.SessionDao

@Database(
    entities = [DhikrEntity::class, SessionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class TasbeehDatabase : RoomDatabase() {

    abstract fun dhikrDao(): DhikrDao
    abstract fun sessionDao(): SessionDao

    companion object {
        const val DATABASE_NAME = "tasbeeh.db"

        val prepopulateCallback = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                db.execSQL(
                    """
                    INSERT INTO dhikrs (name, arabicText, targetCount, isCustom) VALUES
                    ('Субханаллах', 'سُبْحَانَ اللَّهِ', 33, 0),
                    ('Альхамдулиллях', 'الْحَمْدُ لِلَّهِ', 33, 0),
                    ('Аллаху Акбар', 'اللَّهُ أَكْبَرُ', 34, 0)
                    """.trimIndent()
                )
            }
        }
    }
}
