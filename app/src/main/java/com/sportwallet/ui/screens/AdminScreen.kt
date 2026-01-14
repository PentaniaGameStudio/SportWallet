package com.sportwallet.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sportwallet.core.util.DateUtils
import com.sportwallet.data.entities.DailyStatsEntity
import com.sportwallet.ui.viewmodel.WalletViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun AdminScreen(
    onBack: () -> Unit,
    walletVm: WalletViewModel
) {
    val scroll = rememberScrollState()

    val todayKey = DateUtils.localDateToKey(DateUtils.today())

    val dayKey = remember { mutableStateOf(todayKey) }
    val coroutineScope = rememberCoroutineScope()

    // Daily stats
    val flat = remember { mutableStateOf("0") }
    val streak = remember { mutableStateOf("0") }
    val bonusPercent = remember { mutableStateOf("0") }
    val bonusGranted = remember { mutableStateOf("0") }

    // Transactions (solde)
    val txLabel = remember { mutableStateOf("Admin") }
    val txEuro = remember { mutableStateOf("0,00") }

    fun normalizedDayKey(): String = dayKey.value.trim().ifBlank { todayKey }

    fun applyStats(stats: DailyStatsEntity?) {
        if (stats == null) {
            flat.value = "0"
            streak.value = "0"
            bonusPercent.value = "0"
            bonusGranted.value = "0"
            return
        }

        flat.value = stats.flatEarnedCents.toString()
        streak.value = stats.streakDays.toString()
        bonusPercent.value = stats.bonusPercent.toString()
        bonusGranted.value = stats.bonusGrantedCents.toString()
    }

    fun reloadDayStats() {
        val targetKey = normalizedDayKey()
        if (targetKey != dayKey.value) {
            dayKey.value = targetKey
        }
        coroutineScope.launch {
            applyStats(walletVm.adminGetDay(targetKey))
        }
    }

    LaunchedEffect(todayKey) {
        applyStats(walletVm.adminGetDay(todayKey))
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(scroll),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f)
            ) { Text("Retour") }

            OutlinedButton(
                onClick = { walletVm.resetDb() },
                modifier = Modifier.weight(1f)
            ) { Text("Reset DB") }
        }

        Text(
            text = "Admin",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        // =========================
        // Jour ciblé
        // =========================
        Text("Jour ciblé", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = dayKey.value,
            onValueChange = { dayKey.value = it },
            label = { Text("DayKey (yyyy-MM-dd)") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = {
                    dayKey.value = todayKey
                    reloadDayStats()
                },
                modifier = Modifier.weight(1f)
            ) { Text("Aujourd'hui") }

            OutlinedButton(
                onClick = {
                    reloadDayStats()
                },
                modifier = Modifier.weight(1f)
            ) { Text("Recharger") }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // =========================
        // DailyStats
        // =========================
        Text("DailyStats", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = flat.value,
            onValueChange = { flat.value = it },
            label = { Text("Flat (cents) 0..400") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = streak.value,
            onValueChange = { streak.value = it },
            label = { Text("Streak days") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = bonusPercent.value,
            onValueChange = { bonusPercent.value = it },
            label = { Text("Bonus % (0..50)") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = bonusGranted.value,
            onValueChange = { bonusGranted.value = it },
            label = { Text("Bonus donné (cents)") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                val targetKey = normalizedDayKey()
                dayKey.value = targetKey
                walletVm.adminUpsertDay(
                    dayKey = targetKey,
                    flat = flat.value.toIntOrNull() ?: 0,
                    streak = streak.value.toIntOrNull() ?: 0,
                    bonusPercent = bonusPercent.value.toIntOrNull() ?: 0,
                    bonusGranted = bonusGranted.value.toIntOrNull() ?: 0
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sauver DailyStats")
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = {
                    flat.value = "0"
                    streak.value = "0"
                    bonusPercent.value = "0"
                    bonusGranted.value = "0"
                },
                modifier = Modifier.weight(1f)
            ) { Text("Clear champs") }

            OutlinedButton(
                onClick = {
                    // preset rapide pour tester bonus
                    flat.value = "400"
                    streak.value = "3"
                    bonusPercent.value = "30"
                    bonusGranted.value = "0"
                },
                modifier = Modifier.weight(1f)
            ) { Text("Preset test") }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // =========================
        // Solde: transactions manuelles
        // =========================
        Text("Solde (via transaction)", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = txLabel.value,
            onValueChange = { txLabel.value = it },
            label = { Text("Label") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = txEuro.value,
            onValueChange = { txEuro.value = it },
            label = { Text("Montant € (ex: 2,50 ou -1,00)") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                val targetKey = normalizedDayKey()
                dayKey.value = targetKey
                val cents = euroStringToCents(txEuro.value)
                walletVm.adminAddTransaction(
                    dayKey = targetKey,
                    amountCents = cents,
                    label = txLabel.value.ifBlank { "Admin" }
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ajouter transaction")
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = {
                    val targetKey = normalizedDayKey()
                    dayKey.value = targetKey
                    walletVm.adminAddTransaction(targetKey, 100, "Admin +1€")
                },
                modifier = Modifier.weight(1f)
            ) { Text("+1€") }

            OutlinedButton(
                onClick = {
                    val targetKey = normalizedDayKey()
                    dayKey.value = targetKey
                    walletVm.adminAddTransaction(targetKey, -100, "Admin -1€")
                },
                modifier = Modifier.weight(1f)
            ) { Text("-1€") }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = {
                    val targetKey = normalizedDayKey()
                    dayKey.value = targetKey
                    walletVm.adminAddTransaction(targetKey, 500, "Admin +5€")
                },
                modifier = Modifier.weight(1f)
            ) { Text("+5€") }

            OutlinedButton(
                onClick = {
                    val targetKey = normalizedDayKey()
                    dayKey.value = targetKey
                    walletVm.adminAddTransaction(targetKey, -500, "Admin -5€")
                },
                modifier = Modifier.weight(1f)
            ) { Text("-5€") }
        }
    }
}

private fun euroStringToCents(text: String): Int {
    // Parse simple "2.50" / "2,50" / "-1.00"
    val normalized = text.trim().replace(',', '.')
    val value = normalized.toDoubleOrNull() ?: 0.0
    return (value * 100.0).roundToInt()
}
