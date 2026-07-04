package com.example.instacreator.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context

@Database(entities = [AccountEntity::class], version = 1)
abstract class AccountDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao

    companion object {
        private var instance: AccountDatabase? = null

        fun getDatabase(context: Context): AccountDatabase {
            return instance ?: synchronized(this) {
                val db = Room.databaseBuilder(
                    context.applicationContext,
                    AccountDatabase::class.java,
                    "accounts.db"
                ).build()
                instance = db
                db
            }
        }
    }
}