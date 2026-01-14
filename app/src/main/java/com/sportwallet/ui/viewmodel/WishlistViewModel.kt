package com.sportwallet.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sportwallet.data.entities.WishItemEntity
import com.sportwallet.data.repository.WishlistRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WishlistViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = WishlistRepository(app)

    val items = repo.observeItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val favoriteItem = repo.observeFavorite()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val history = repo.observeHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addItem(name: String, imageUrl: String, priceCents: Int) {
        viewModelScope.launch {
            repo.addItem(name, imageUrl, priceCents)
        }
    }

    fun deleteItem(item: WishItemEntity) {
        viewModelScope.launch {
            repo.deleteItem(item)
        }
    }

    fun setFavorite(itemId: Long) {
        viewModelScope.launch {
            repo.setFavorite(itemId)
        }
    }

    fun purchaseItem(item: WishItemEntity) {
        viewModelScope.launch {
            repo.purchaseItem(item)
        }
    }
}
