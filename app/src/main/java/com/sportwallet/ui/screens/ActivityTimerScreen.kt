package com.sportwallet.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sportwallet.domain.model.ActivityType
import com.sportwallet.domain.services.ActivityEarningService
import kotlinx.coroutines.delay
import kotlin.math.abs

private const val FLAT_CAP_CENTS = 400 // 4€ flat max/jour

@Composable
fun ActivityTimerScreen(
    iconRes: Int,
    activityType: ActivityType,
    dayFlatEarnedCents: Int, // flat réel déjà gagné aujourd’hui (0..400)
    onStop: (elapsedMs: Long) -> Unit
) {
    var isPaused by remember { mutableStateOf(false) }
    var elapsedMs by remember { mutableLongStateOf(0L) }

    val earningService = remember { ActivityEarningService() }

    // Tick toutes les secondes
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            if (!isPaused) elapsedMs += 1000L
        }
    }

    // ✅ Simulation au prorata (centime) : gain de l’activité si on stoppait maintenant
    val earnedSoFarCents = earningService.computeFlatEarnedCents(activityType, elapsedMs)

    // ✅ Progression flat simulée (capée à 4€)
    val simulatedFlatCents = (dayFlatEarnedCents + earnedSoFarCents).coerceAtMost(FLAT_CAP_CENTS)
    val progress = (simulatedFlatCents.toFloat() / FLAT_CAP_CENTS.toFloat()).coerceIn(0f, 1f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        // Icône en fond
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier
                .size(280.dp)
                .align(Alignment.Center)
                .alpha(0.08f)
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Chrono",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = formatDuration(elapsedMs),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(26.dp))

            // ✅ Barre progression flat (avec simulation)
            DayProgressBar(
                simulatedFlatCents = simulatedFlatCents,
                progress = progress
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { isPaused = !isPaused },
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors()
                ) {
                    Text(if (isPaused) "Reprendre" else "Pause")
                }

                OutlinedButton(
                    onClick = { onStop(elapsedMs) }, // ✅ calc réel côté repo
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Stop")
                }
            }
        }
    }
}

@Composable
private fun DayProgressBar(
    simulatedFlatCents: Int,
    progress: Float
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Progression du jour",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${formatEuro(simulatedFlatCents)} / 4,00€",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
        )
    }
}

private fun formatDuration(ms: Long): String {
    val totalSeconds = (ms / 1000L).toInt()
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return if (hours > 0) {
        "${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
    } else {
        "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
    }
}

private fun formatEuro(cents: Int): String {
    val absCents = abs(cents)
    val euros = absCents / 100
    val cent = absCents % 100
    val sign = if (cents < 0) "-" else ""
    return "$sign$euros," + cent.toString().padStart(2, '0') + "€"
}
