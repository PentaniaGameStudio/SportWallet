package com.sportwallet.data.repository

import android.content.Context
import androidx.room.withTransaction
import com.sportwallet.core.util.DateUtils
import com.sportwallet.data.db.DatabaseProvider
import com.sportwallet.data.entities.PurchaseHistoryEntity
import com.sportwallet.data.entities.TransactionEntity
import com.sportwallet.data.entities.WishItemEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class WishlistRepository(context: Context) {
    private val db = DatabaseProvider.get(context)
    private val wishItemDao = db.wishItemDao()
    private val purchaseHistoryDao = db.purchaseHistoryDao()
    private val transactionDao = db.transactionDao()

    fun observeItems(): Flow<List<WishItemEntity>> = wishItemDao.observeAll()

    fun observeFavorite(): Flow<WishItemEntity?> = wishItemDao.observeFavorite()

    fun observeHistory(): Flow<List<PurchaseHistoryEntity>> = purchaseHistoryDao.observeAll()

    suspend fun addItem(name: String, imageUrl: String, priceCents: Int) {
        withContext(Dispatchers.IO) {
            wishItemDao.insert(
                WishItemEntity(
                    name = name.trim(),
                    imageUrl = imageUrl.trim(),
                    priceCents = priceCents.coerceAtLeast(0),
                    createdAtEpochMs = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun updateItem(item: WishItemEntity, name: String, imageUrl: String, priceCents: Int) {
        withContext(Dispatchers.IO) {
            wishItemDao.update(
                item.copy(
                    name = name.trim(),
                    imageUrl = imageUrl.trim(),
                    priceCents = priceCents.coerceAtLeast(0)
                )
            )
        }
    }

    suspend fun deleteItem(item: WishItemEntity) {
        withContext(Dispatchers.IO) {
            wishItemDao.delete(item)
        }
    }

    suspend fun setFavorite(itemId: Long) {
        withContext(Dispatchers.IO) {
            db.withTransaction {
                wishItemDao.clearFavorite()
                wishItemDao.setFavorite(itemId)
            }
        }
    }

    suspend fun purchaseItem(item: WishItemEntity) {
        withContext(Dispatchers.IO) {
            val todayKey = DateUtils.localDateToKey(DateUtils.today())
            db.withTransaction {
                transactionDao.insert(
                    TransactionEntity(
                        amountCents = -item.priceCents.coerceAtLeast(0),
                        label = "Achat: ${item.name}",
                        timestampEpochMs = System.currentTimeMillis(),
                        dayKey = todayKey
                    )
                )
                purchaseHistoryDao.insert(
                    PurchaseHistoryEntity(
                        itemName = item.name,
                        priceCents = item.priceCents,
                        purchasedAtEpochMs = System.currentTimeMillis()
                    )
                )
                wishItemDao.setPurchased(item.id)
            }
        }
    }
}
