package com.sportwallet.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sportwallet.data.dao.DailyStatsDao
import com.sportwallet.data.dao.PurchaseHistoryDao
import com.sportwallet.data.dao.TransactionDao
import com.sportwallet.data.dao.WishItemDao
import com.sportwallet.data.entities.DailyStatsEntity
import com.sportwallet.data.entities.PurchaseHistoryEntity
import com.sportwallet.data.entities.TransactionEntity
import com.sportwallet.data.entities.WishItemEntity

@Database(
    entities = [
        TransactionEntity::class,
        DailyStatsEntity::class,
        WishItemEntity::class,
        PurchaseHistoryEntity::class
    ],
    version = 3,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun dailyStatsDao(): DailyStatsDao
    abstract fun wishItemDao(): WishItemDao
    abstract fun purchaseHistoryDao(): PurchaseHistoryDao
}
