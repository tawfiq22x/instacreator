package com.example.instacreator.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Insert
    suspend fun insert(account: AccountEntity)

    @Query("SELECT * FROM accounts ORDER BY timestamp DESC")
    fun getAll(): Flow<List<AccountEntity>>
}