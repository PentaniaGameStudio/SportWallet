package com.sportwallet.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sportwallet.R
import com.sportwallet.domain.model.ActivityType
import com.sportwallet.ui.components.ActivityButton
import com.sportwallet.ui.viewmodel.WalletViewModel
import com.sportwallet.ui.viewmodel.WishlistViewModel

@Composable
fun ActivityScreen() {
    val vm: WalletViewModel = viewModel()
    val walletState = vm.walletState.collectAsStateWithLifecycle().value
    val wishlistViewModel: WishlistViewModel = viewModel()
    val favoriteItem = wishlistViewModel.favoriteItem.collectAsStateWithLifecycle().value

    var runningType by rememberSaveable { mutableStateOf<ActivityType?>(null) }
    var runningIconRes by rememberSaveable { mutableStateOf<Int?>(null) }

    // ✅ Flat du jour (0..400)
    val dayFlatEarnedCents = walletState?.dayFlatCents ?: 0
    val balanceCents = walletState?.balanceCents ?: 0

    // ✅ Chrono plein écran + simulation
    if (runningType != null && runningIconRes != null) {
        ActivityTimerScreen(
            iconRes = runningIconRes!!,
            activityType = runningType!!,
            dayFlatEarnedCents = dayFlatEarnedCents,
            balanceCents = balanceCents,
            favoriteItem = favoriteItem,
            onStop = { elapsedMs ->
                vm.onActivityStopped(runningType!!, elapsedMs) // ✅ calcul réel
                runningType = null
                runningIconRes = null
            }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(
            space = 22.dp,
            alignment = Alignment.CenterVertically
        ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Activité", style = MaterialTheme.typography.headlineSmall)

        ActivityButton(
            title = "Vélo",
            subtitle = "10 minutes => 1€",
            iconRes = R.drawable.ic_velo,
            onClick = {
                runningType = ActivityType.BIKE
                runningIconRes = R.drawable.ic_velo
            }
        )

        ActivityButton(
            title = "Marche",
            subtitle = "15 minutes => 1€",
            iconRes = R.drawable.ic_marche,
            onClick = {
                runningType = ActivityType.WALK
                runningIconRes = R.drawable.ic_marche
            }
        )

        ActivityButton(
            title = "Autre",
            subtitle = "15 minutes => 1€",
            iconRes = R.drawable.ic_autre,
            onClick = {
                runningType = ActivityType.OTHER
                runningIconRes = R.drawable.ic_autre
            }
        )

        ActivityButton(
            title = "Journée Off",
            subtitle = "4€ Immédiat",
            iconRes = R.drawable.ic_sleep,
            onClick = {
                // Direct : calc réel sans chrono
                vm.onActivityStopped(ActivityType.OFF_DAY, 0L)
            }
        )
    }
}
