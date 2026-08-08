package com.example.eduapp.database

import android.content.Context
import androidx.room.Room

object DatabaseProvider {

    @Volatile
    private var instance: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase =
        instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "app_db"
            )
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
                .also { instance = it }
        }
}