package com.example.tagenglish.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.tagenglish.data.local.dao.TestResultDao
import com.example.tagenglish.data.local.dao.WordDao
import com.example.tagenglish.data.local.entities.TestResultEntity
import com.example.tagenglish.data.local.entities.UsageEntity
import com.example.tagenglish.data.local.entities.WordEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        WordEntity::class,
        UsageEntity::class,
        TestResultEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun wordDao(): WordDao
    abstract fun testResultDao(): TestResultDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tagenglish.db"
                )
                    .addCallback(SeedCallback(context))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    // ─── Seed inicial desde assets/words.json ─────────────────────────────────

    private class SeedCallback(
        private val context: Context
    ) : Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // Ejecutar seed en background al crear la DB por primera vez
            CoroutineScope(Dispatchers.IO).launch {
                val database = getInstance(context)
                DatabaseSeeder.seed(context, database)
            }
        }
    }
}
