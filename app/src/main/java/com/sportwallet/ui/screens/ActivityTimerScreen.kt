package com.sportwallet.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.produceState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sportwallet.R
import com.sportwallet.data.entities.WishItemEntity
import com.sportwallet.domain.model.ActivityType
import com.sportwallet.domain.services.ActivityEarningService
import com.sportwallet.ui.utils.KeepScreenOn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.math.abs
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.unit.min

private const val FLAT_CAP_CENTS = 400 // 4€ flat max/jour

@Composable
fun ActivityTimerScreen(
    iconRes: Int,
    activityType: ActivityType,
    dayFlatEarnedCents: Int, // flat réel déjà gagné aujourd’hui (0..400)
    balanceCents: Int,
    favoriteItem: WishItemEntity?,
    elapsedMs: Long,
    isPaused: Boolean,
    onPauseToggle: () -> Unit,
    onTick: (Long) -> Unit,
    onStop: (elapsedMs: Long) -> Unit
) {
    KeepScreenOn(enabled = true)
    val earningService = remember { ActivityEarningService() }
    val currentOnTick by rememberUpdatedState(onTick)
    val currentIsPaused by rememberUpdatedState(isPaused)

    // Tick toutes les secondes
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            if (!currentIsPaused) currentOnTick(1000L)
        }
    }

    // ✅ Simulation au prorata (centime) : gain de l’activité si on stoppait maintenant
    val earnedSoFarCents = earningService.computeFlatEarnedCents(activityType, elapsedMs)

    // ✅ Progression flat simulée (capée à 4€)
    val simulatedFlatCents = (dayFlatEarnedCents + earnedSoFarCents).coerceAtMost(FLAT_CAP_CENTS)
    val progress = (simulatedFlatCents.toFloat() / FLAT_CAP_CENTS.toFloat()).coerceIn(0f, 1f)
    val projectedEarnedCents = (simulatedFlatCents - dayFlatEarnedCents).coerceAtLeast(0)
    val projectedBalanceCents = balanceCents + projectedEarnedCents

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
                .alpha(0.16f)
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (favoriteItem != null) {
                FavoriteWishProgressCard(
                    item = favoriteItem,
                    projectedBalanceCents = projectedBalanceCents
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

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
                    onClick = onPauseToggle,
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
private fun FavoriteWishProgressCard(
    item: WishItemEntity,
    projectedBalanceCents: Int
) {
    val progress = if (item.priceCents > 0) {
        (projectedBalanceCents.toFloat() / item.priceCents.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val imageSize = min(maxWidth * 0.75f, maxHeight * 0.4f)

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                WishItemImage(
                    imageUrl = item.imageUrl,
                    size = imageSize
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Solde projeté",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatEuro(projectedBalanceCents),
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = formatEuro(item.priceCents),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(999.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
            )
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

@Composable
private fun WishItemImage(
    imageUrl: String,
    size: Dp,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)
    val fallback = painterResource(R.drawable.ic_envies)
    val context = LocalContext.current
    val imageBitmap: ImageBitmap? = produceState<ImageBitmap?>(null, imageUrl) {
        value = if (imageUrl.isBlank()) {
            null
        } else {
            withContext(Dispatchers.IO) {
                runCatching {
                    val uri = Uri.parse(imageUrl)
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        BitmapFactory.decodeStream(stream)?.asImageBitmap()
                    }
                }.getOrNull()
            }
        }
    }.value

    if (imageBitmap != null) {
        val aspectRatio = imageBitmap.width.toFloat() / imageBitmap.height.toFloat()
        Image(
            bitmap = imageBitmap,
            contentDescription = null,
            modifier = modifier
                .height(size)
                .aspectRatio(aspectRatio, matchHeightConstraintsFirst = true)
                .clip(shape)
        )
    } else {
        Box(
            modifier = modifier
                .size(size)
                .clip(shape)
                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = fallback,
                contentDescription = null,
                modifier = Modifier.size(size * 0.7f)
            )
        }
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
