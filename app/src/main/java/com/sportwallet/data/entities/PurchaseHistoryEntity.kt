package com.sportwallet.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "purchase_history")
data class PurchaseHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val itemName: String,
    val priceCents: Int,
    val purchasedAtEpochMs: Long
)
