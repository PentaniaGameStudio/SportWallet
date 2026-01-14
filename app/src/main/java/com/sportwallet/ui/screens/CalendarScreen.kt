package com.sportwallet.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sportwallet.core.util.DateUtils
import com.sportwallet.data.entities.TransactionEntity
import com.sportwallet.domain.model.ActivityType
import com.sportwallet.ui.viewmodel.WalletViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

private data class DaySummary(
    val earnedCents: Int,
    val counts: Map<ActivityType, Int>
)

private data class ActivityDetail(
    val label: String,
    val minutes: Int?,
    val earnedCents: Int
)

@Composable
fun CalendarScreen(
    viewModel: WalletViewModel = viewModel()
) {
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf(DateUtils.today()) }

    val monthSummary = remember(transactions, currentMonth) {
        buildMonthSummary(transactions, currentMonth)
    }

    LaunchedEffect(currentMonth) {
        if (selectedDate.year != currentMonth.year || selectedDate.month != currentMonth.month) {
            selectedDate = currentMonth.atDay(1)
        }
    }

    val selectedDetails = remember(transactions, selectedDate) {
        buildDayDetails(transactions, selectedDate)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        CalendarHeader(
            month = currentMonth,
            onPrevious = { currentMonth = currentMonth.minusMonths(1) },
            onNext = { currentMonth = currentMonth.plusMonths(1) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf(
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY,
                DayOfWeek.SATURDAY,
                DayOfWeek.SUNDAY
            ).forEach { day ->
                Text(
                    text = day.getDisplayName(java.time.format.TextStyle.SHORT, Locale.FRENCH)
                        .replaceFirstChar { it.titlecase(Locale.FRENCH) },
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(monthSummary, key = { it.date?.toString() ?: "empty-${it.index}" }) { cell ->
                CalendarDayCell(
                    date = cell.date,
                    summary = cell.summary,
                    isSelected = cell.date == selectedDate,
                    onClick = { date -> selectedDate = date }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        DayDetailsSection(
            date = selectedDate,
            details = selectedDetails
        )
    }
}

private data class CalendarCell(
    val index: Int,
    val date: LocalDate?,
    val summary: DaySummary?
)

@Composable
private fun CalendarHeader(
    month: YearMonth,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("LLLL yyyy", Locale.FRENCH)
    val monthLabel = month.format(formatter)
        .replaceFirstChar { it.titlecase(Locale.FRENCH) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPrevious) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Mois précédent"
            )
        }

        Text(
            text = monthLabel,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        IconButton(onClick = onNext) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Mois suivant"
            )
        }
    }
}

@Composable
private fun CalendarDayCell(
    date: LocalDate?,
    summary: DaySummary?,
    isSelected: Boolean,
    onClick: (LocalDate) -> Unit
) {
    val background = summary?.let {
        dominantActivityColor(it.counts)?.copy(alpha = 0.2f)
    } ?: MaterialTheme.colorScheme.surfaceContainerHighest
    val amount = summary?.earnedCents ?: 0
    val border = if (isSelected) {
        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    } else {
        null
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(enabled = date != null) {
                if (date != null) {
                    onClick(date)
                }
            },
        colors = CardDefaults.cardColors(containerColor = background),
        border = border
    ) {
        if (date == null) {
            return@Card
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = formatCents(amount),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

private fun buildMonthSummary(
    transactions: List<TransactionEntity>,
    month: YearMonth
): List<CalendarCell> {
    val summariesByDay = buildDaySummaries(transactions, month)
    val firstOfMonth = month.atDay(1)
    val firstDayOfWeek = firstOfMonth.dayOfWeek
    val leadingEmpty = ((firstDayOfWeek.value - DayOfWeek.MONDAY.value) + 7) % 7
    val daysInMonth = month.lengthOfMonth()
    val totalCells = leadingEmpty + daysInMonth
    val list = mutableListOf<CalendarCell>()

    repeat(leadingEmpty) { index ->
        list.add(CalendarCell(index = index, date = null, summary = null))
    }

    for (day in 1..daysInMonth) {
        val date = month.atDay(day)
        val summary = summariesByDay[date]
        list.add(CalendarCell(index = list.size, date = date, summary = summary))
    }

    val trailing = (7 - (totalCells % 7)).takeIf { it in 1..6 } ?: 0
    repeat(trailing) { index ->
        list.add(CalendarCell(index = list.size + index, date = null, summary = null))
    }

    return list
}

private fun buildDaySummaries(
    transactions: List<TransactionEntity>,
    month: YearMonth
): Map<LocalDate, DaySummary> {
    val summaries = mutableMapOf<LocalDate, MutableDaySummary>()
    transactions
        .filter { it.amountCents > 0 }
        .forEach { tx ->
            val date = runCatching { LocalDate.parse(tx.dayKey) }.getOrNull() ?: return@forEach
            if (date.year != month.year || date.month != month.month) return@forEach
            val summary = summaries.getOrPut(date) { MutableDaySummary() }
            summary.earnedCents += tx.amountCents
            val activity = activityFromLabel(tx.label)
            if (activity != null) {
                summary.counts[activity] = (summary.counts[activity] ?: 0) + 1
            }
        }

    return summaries.mapValues { (_, value) ->
        DaySummary(
            earnedCents = value.earnedCents,
            counts = value.counts.toMap()
        )
    }
}

@Composable
private fun DayDetailsSection(
    date: LocalDate,
    details: List<ActivityDetail>
) {
    val formatter = DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy", Locale.FRENCH)
    val title = date.format(formatter).replaceFirstChar { it.titlecase(Locale.FRENCH) }
    val totalCents = details.sumOf { it.earnedCents }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Total gagné : ${formatCents(totalCents)}",
                style = MaterialTheme.typography.bodyMedium
            )

            if (details.isEmpty()) {
                Text(
                    text = "Aucune activité enregistrée.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                details.forEach { detail ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = detail.label,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            detail.minutes?.let { minutes ->
                                Text(
                                    text = "${minutes} min",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Text(
                            text = formatCents(detail.earnedCents),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

private fun buildDayDetails(
    transactions: List<TransactionEntity>,
    date: LocalDate
): List<ActivityDetail> {
    val dayKey = date.toString()
    val relevant = transactions.filter { it.dayKey == dayKey && it.amountCents > 0 }
    if (relevant.isEmpty()) return emptyList()

    val grouped = mutableMapOf<String, MutableActivityDetail>()
    relevant.forEach { tx ->
        val label = detailLabelFromTransaction(tx.label)
        val minutes = estimateMinutes(tx.label, tx.amountCents)
        val entry = grouped.getOrPut(label) { MutableActivityDetail(label = label) }
        entry.earnedCents += tx.amountCents
        if (minutes != null) {
            entry.minutes += minutes
            entry.hasMinutes = true
        }
    }

    return grouped.values.map { entry ->
        ActivityDetail(
            label = entry.label,
            minutes = if (entry.hasMinutes) entry.minutes else null,
            earnedCents = entry.earnedCents
        )
    }.sortedByDescending { it.earnedCents }
}

private class MutableActivityDetail(
    val label: String,
    var minutes: Int = 0,
    var earnedCents: Int = 0,
    var hasMinutes: Boolean = false
)

private fun detailLabelFromTransaction(label: String): String {
    return when (label.trim()) {
        "Vélo" -> "Vélo"
        "Marche" -> "Marche"
        "Autre" -> "Autre"
        "Repos", "Journée Off" -> "Repos"
        "Bonus streak" -> "Bonus"
        else -> label
    }
}

private fun estimateMinutes(label: String, earnedCents: Int): Int? {
    val euros = earnedCents / 100.0
    val minutes = when (label.trim()) {
        "Vélo" -> euros * 10.0
        "Marche", "Autre" -> euros * 15.0
        "Repos", "Journée Off", "Bonus streak" -> return null
        else -> return null
    }
    return minutes.roundToInt()
}

private class MutableDaySummary(
    var earnedCents: Int = 0,
    val counts: MutableMap<ActivityType, Int> = mutableMapOf()
)

private fun activityFromLabel(label: String): ActivityType? {
    return when (label.trim()) {
        "Vélo" -> ActivityType.BIKE
        "Marche" -> ActivityType.WALK
        "Autre" -> ActivityType.OTHER
        "Repos", "Journée Off" -> ActivityType.OFF_DAY
        else -> null
    }
}

private fun dominantActivityColor(counts: Map<ActivityType, Int>): Color? {
    val dominant = counts.maxByOrNull { it.value }?.key ?: return null
    return when (dominant) {
        ActivityType.WALK -> Color(0xFF4CAF50)
        ActivityType.BIKE -> Color(0xFF2196F3)
        ActivityType.OTHER -> Color(0xFFFFC107)
        ActivityType.OFF_DAY -> Color(0xFFFF9800)
    }
}

private fun formatCents(cents: Int): String {
    val absCents = abs(cents)
    val euros = absCents / 100
    val cent = absCents % 100
    return "$euros," + cent.toString().padStart(2, '0') + "€"
}
