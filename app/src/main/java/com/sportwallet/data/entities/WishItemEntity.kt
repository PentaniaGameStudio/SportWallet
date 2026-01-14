package com.sportwallet.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wish_items")
data class WishItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val imageUrl: String,
    val priceCents: Int,
    val isFavorite: Boolean = false,
    val createdAtEpochMs: Long
)
