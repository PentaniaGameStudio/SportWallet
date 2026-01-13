package com.sportwallet.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sportwallet.data.repository.WalletRepository
import com.sportwallet.domain.model.ActivityType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WalletViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = WalletRepository(app)

    val walletState = repo.observeWalletState()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        viewModelScope.launch { repo.ensureTodayInitialized() }
    }

    fun onActivityStopped(type: ActivityType, elapsedMs: Long) {
        viewModelScope.launch {
            repo.applyActivityStop(type, elapsedMs)
        }
    }

    // =========================
    // Admin
    // =========================

    fun resetDb() {
        viewModelScope.launch {
            repo.adminResetDatabase()
            repo.ensureTodayInitialized()
        }
    }

    fun adminUpsertDay(
        dayKey: String,
        flat: Int,
        streak: Int,
        bonusPercent: Int,
        bonusGranted: Int
    ) {
        viewModelScope.launch {
            repo.adminUpsertDay(
                dayKey = dayKey,
                flatEarnedCents = flat,
                streakDays = streak,
                bonusPercent = bonusPercent,
                bonusGrantedCents = bonusGranted
            )
        }
    }

    fun adminAddTransaction(
        dayKey: String,
        amountCents: Int, // peut être négatif => retrait
        label: String
    ) {
        viewModelScope.launch {
            repo.adminInsertTransaction(
                dayKey = dayKey,
                amountCents = amountCents,
                label = label
            )
        }
    }
}
