package com.sportwallet.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sportwallet.R
import com.sportwallet.ui.navigation.Routes
import com.sportwallet.ui.viewmodel.WalletViewModel

private const val FLAT_CAP_CENTS = 400 // 4€ flat max/jour

@Composable
fun HomeScreen(navController: NavHostController) {
    val vm: WalletViewModel = viewModel()
    val state = vm.walletState.collectAsStateWithLifecycle().value

    val balanceCents = state?.balanceCents ?: 0
    val dayFlatCents = state?.dayFlatCents ?: 0
    val streakDays = state?.streakDays ?: 0
    val bonusPercent = state?.bonusPercent ?: 0
    val bonusGrantedCents = state?.bonusGrantedCents ?: 0

    val progress = (dayFlatCents.toFloat() / FLAT_CAP_CENTS.toFloat()).coerceIn(0f, 1f)

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
        val tapCount = remember { mutableIntStateOf(0) }
        val tapWindowMs = 1200L

        LaunchedEffect(tapCount.intValue) {
            if (tapCount.intValue > 0) {
                delay(tapWindowMs)
                tapCount.intValue = 0
            }
        }

        Text(
            text = "Accueil",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable {
                tapCount.intValue++
                if (tapCount.intValue >= 5) {
                    tapCount.intValue = 0
                    navController.navigate(Routes.ADMIN)
                }
            }
        )

        InfoCard(
            title = "Solde",
            value = formatEuro(balanceCents),
            iconRes = R.drawable.ic_wallet
        )

        ProgressCard(
            title = "Progression",
            value = "${formatEuro(dayFlatCents)} / 4,00€",
            iconRes = R.drawable.ic_progress,
            progress = progress
        )

        InfoCard(
            title = "Streak",
            value = "$streakDays jour(s)",
            iconRes = R.drawable.ic_streak
        )

        // ✅ Optionnel : petit rappel du bonus streak (sans exposer de “règles” longues)
        // Tu peux supprimer ce bloc si tu n’en veux pas encore.
        if (bonusPercent > 0) {
            val bonusText = if (bonusGrantedCents > 0) {
                "Bonus : ${formatEuro(bonusGrantedCents)}"
            } else {
                "Bonus : +$bonusPercent%"
            }

            InfoCard(
                title = "Bonus",
                value = bonusText,
                iconRes = R.drawable.ic_streak
            )
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    value: String,
    @DrawableRes iconRes: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 420.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = title,
                modifier = Modifier.size(28.dp),
                tint = Color.Unspecified
            )

            Spacer(modifier = Modifier.size(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ProgressCard(
    title: String,
    value: String,
    @DrawableRes iconRes: Int,
    progress: Float
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 420.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = title,
                    modifier = Modifier.size(28.dp),
                    tint = Color.Unspecified
                )

                Spacer(modifier = Modifier.size(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
            )
        }
    }
}

private fun formatEuro(cents: Int): String {
    val abs = kotlin.math.abs(cents)
    val euros = abs / 100
    val cent = abs % 100
    val sign = if (cents < 0) "-" else ""
    return "$sign$euros," + cent.toString().padStart(2, '0') + "€"
}
