package com.sportwallet.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sportwallet.data.entities.WishItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WishItemDao {
    @Query("SELECT * FROM wish_items ORDER BY createdAtEpochMs DESC")
    fun observeAll(): Flow<List<WishItemEntity>>

    @Query("SELECT * FROM wish_items WHERE isFavorite = 1 LIMIT 1")
    fun observeFavorite(): Flow<WishItemEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: WishItemEntity): Long

    @Delete
    suspend fun delete(item: WishItemEntity)

    @Query("UPDATE wish_items SET isFavorite = 0")
    suspend fun clearFavorite()

    @Query("UPDATE wish_items SET isFavorite = 1 WHERE id = :itemId")
    suspend fun setFavorite(itemId: Long)
}
