package com.traceboard.app.data.repository

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.traceboard.app.data.model.ClipboardFolder
import com.traceboard.app.data.model.ClipboardItem
import com.traceboard.app.data.model.TrackedWord

@Database(
    entities = [ClipboardItem::class, TrackedWord::class, ClipboardFolder::class],
    version = 2,
    exportSchema = false
)
abstract class TraceboardDatabase : RoomDatabase() {
    abstract fun clipboardDao(): ClipboardDao
    abstract fun trackedWordDao(): TrackedWordDao
    abstract fun folderDao(): FolderDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE clipboard_items ADD COLUMN folderId INTEGER")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `clipboard_folders` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`name` TEXT NOT NULL, " +
                        "`createdAt` INTEGER NOT NULL)"
                )
            }
        }

        @Volatile
        private var INSTANCE: TraceboardDatabase? = null

        fun getInstance(context: Context): TraceboardDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TraceboardDatabase::class.java,
                    "traceboard.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}