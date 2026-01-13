package com.sportwallet.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_stats")
data class DailyStatsEntity(
    @PrimaryKey val dayKey: String,    // yyyy-MM-dd
    val flatEarnedCents: Int,          // 0..400 (progression flat)
    val streakDays: Int,
    val bonusPercent: Int,             // 0..50
    val bonusGrantedCents: Int,        // 0 tant que pas donné, sinon montant donné
    val updatedAtEpochMs: Long
)
