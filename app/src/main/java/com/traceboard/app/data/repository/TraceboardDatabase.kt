package com.traceboard.app.data.repository

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.traceboard.app.data.model.ClipboardItem
import com.traceboard.app.data.model.TrackedWord

@Database(
    entities = [ClipboardItem::class, TrackedWord::class],
    version = 1,
    exportSchema = false
)
abstract class TraceboardDatabase : RoomDatabase() {
    abstract fun clipboardDao(): ClipboardDao
    abstract fun trackedWordDao(): TrackedWordDao

    companion object {
        @Volatile
        private var INSTANCE: TraceboardDatabase? = null

        fun getInstance(context: Context): TraceboardDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TraceboardDatabase::class.java,
                    "traceboard.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
