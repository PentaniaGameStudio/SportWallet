package com.sportwallet.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sportwallet.data.dao.DailyStatsDao
import com.sportwallet.data.dao.TransactionDao
import com.sportwallet.data.entities.DailyStatsEntity
import com.sportwallet.data.entities.TransactionEntity

@Database(
    entities = [TransactionEntity::class, DailyStatsEntity::class],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun dailyStatsDao(): DailyStatsDao
}
