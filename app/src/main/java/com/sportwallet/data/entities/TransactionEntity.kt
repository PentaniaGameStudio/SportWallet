package com.sportwallet.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amountCents: Int,              // + = gain, - = dépense
    val label: String,                 // ex: "Vélo", "Achat: X"
    val timestampEpochMs: Long,        // System.currentTimeMillis()
    val dayKey: String                 // yyyy-MM-dd (pour agrégats rapides)
)
