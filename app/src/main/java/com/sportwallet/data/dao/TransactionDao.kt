package com.sportwallet.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sportwallet.data.entities.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(tx: TransactionEntity)

    @Query("SELECT COALESCE(SUM(amountCents), 0) FROM transactions")
    fun observeBalanceCents(): Flow<Int>

    @Query("SELECT COALESCE(SUM(amountCents), 0) FROM transactions WHERE dayKey = :dayKey AND amountCents > 0")
    suspend fun sumPositiveForDay(dayKey: String): Int

    @Query("SELECT * FROM transactions ORDER BY timestampEpochMs DESC LIMIT :limit")
    fun observeLatest(limit: Int): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY timestampEpochMs DESC")
    fun observeAll(): Flow<List<TransactionEntity>>

    @Query("SELECT COALESCE(SUM(amountCents), 0) FROM transactions WHERE dayKey = :dayKey AND amountCents > 0")
    fun observePositiveForDay(dayKey: String): Flow<Int>

}
