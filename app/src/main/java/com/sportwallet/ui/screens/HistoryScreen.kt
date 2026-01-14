package com.sportwallet.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sportwallet.data.entities.TransactionEntity
import com.sportwallet.ui.viewmodel.WalletViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

private enum class HistoryTab(val title: String) {
    GAINS("Gains"),
    PURCHASES("Achats")
}

@Composable
fun HistoryScreen(
    viewModel: WalletViewModel = viewModel()
) {
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    var selectedTab by rememberSaveable { mutableStateOf(HistoryTab.GAINS) }

    val gains = transactions.filter { it.amountCents > 0 }
    val purchases = transactions.filter { it.amountCents < 0 }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Historique",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        TabRow(selectedTabIndex = selectedTab.ordinal) {
            HistoryTab.values().forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    text = { Text(tab.title) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (selectedTab) {
            HistoryTab.GAINS -> TransactionList(
                emptyLabel = "Aucun gain pour le moment.",
                transactions = gains,
                amountPrefix = "+"
            )

            HistoryTab.PURCHASES -> TransactionList(
                emptyLabel = "Aucun achat pour le moment.",
                transactions = purchases,
                amountPrefix = "-"
            )
        }
    }
}

@Composable
private fun TransactionList(
    emptyLabel: String,
    transactions: List<TransactionEntity>,
    amountPrefix: String
) {
    if (transactions.isEmpty()) {
        Text(
            text = emptyLabel,
            style = MaterialTheme.typography.bodyMedium
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(transactions, key = { it.id }) { tx ->
            TransactionRow(tx = tx, amountPrefix = amountPrefix)
        }
    }
}

@Composable
private fun TransactionRow(
    tx: TransactionEntity,
    amountPrefix: String
) {
    val date = formatDayKey(tx.dayKey)
    val amount = formatCents(abs(tx.amountCents))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tx.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "$amountPrefix$amount",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

private fun formatCents(cents: Int): String {
    val euros = cents / 100
    val cent = cents % 100
    return "$euros," + cent.toString().padStart(2, '0') + "€"
}

private fun formatDayKey(dayKey: String): String {
    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.FRENCH)
    return runCatching {
        LocalDate.parse(dayKey).format(formatter)
    }.getOrElse { dayKey }
}
