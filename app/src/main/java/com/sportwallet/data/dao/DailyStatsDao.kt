package com.sportwallet.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.sportwallet.data.entities.DailyStatsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyStatsDao {

    @Query("SELECT * FROM daily_stats WHERE dayKey = :dayKey LIMIT 1")
    suspend fun get(dayKey: String): DailyStatsEntity?

    @Query("SELECT * FROM daily_stats WHERE dayKey = :dayKey LIMIT 1")
    fun observe(dayKey: String): Flow<DailyStatsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: DailyStatsEntity)

    @Query("SELECT * FROM daily_stats WHERE dayKey = :prevDayKey LIMIT 1")
    suspend fun getPreviousDay(prevDayKey: String): DailyStatsEntity?
}
