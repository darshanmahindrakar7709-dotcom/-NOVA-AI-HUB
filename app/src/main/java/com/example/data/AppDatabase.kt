package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ToolHistoryEntity::class,
        FavoriteToolEntity::class,
        NoteEntity::class,
        FlashcardDeckEntity::class,
        FlashcardEntity::class,
        TodoEntity::class,
        ChatMessageEntity::class,
        AppSettingEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun toolDao(): ToolDao
    abstract fun notesDao(): NotesDao
    abstract fun flashcardsDao(): FlashcardsDao
    abstract fun todoDao(): TodoDao
    abstract fun chatDao(): ChatDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nova_ai_hub_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
