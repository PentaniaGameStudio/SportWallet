package com.sportwallet.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sportwallet.data.entities.PurchaseHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PurchaseHistoryDao {
    @Query("SELECT * FROM purchase_history ORDER BY purchasedAtEpochMs DESC")
    fun observeAll(): Flow<List<PurchaseHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: PurchaseHistoryEntity): Long
}
