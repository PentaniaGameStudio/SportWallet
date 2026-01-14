package com.sportwallet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.sportwallet.data.entities.PurchaseHistoryEntity
import com.sportwallet.data.entities.WishItemEntity
import com.sportwallet.ui.viewmodel.WishlistViewModel
import kotlin.math.roundToInt

@Composable
fun WishlistScreen(viewModel: WishlistViewModel = viewModel()) {
    val items by viewModel.items.collectAsState()
    val favorite by viewModel.favoriteItem.collectAsState()
    val history by viewModel.history.collectAsState()

    var name by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var priceInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Envies",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Nouvel objet", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nom") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = imageUrl,
            onValueChange = { imageUrl = it },
            label = { Text("Image (URL)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = priceInput,
            onValueChange = { priceInput = it },
            label = { Text("Prix (€)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                val priceCents = parsePriceToCents(priceInput)
                if (name.isBlank() || priceCents == null) return@Button
                viewModel.addItem(name, imageUrl, priceCents)
                name = ""
                imageUrl = ""
                priceInput = ""
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ajouter")
        }

        if (favorite != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Favori actuel : ${favorite?.name}",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Objets", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        if (items.isEmpty()) {
            Text("Aucun objet pour le moment.")
        } else {
            items.forEach { item ->
                WishItemCard(
                    item = item,
                    onPurchase = { viewModel.purchaseItem(item) },
                    onDelete = { viewModel.deleteItem(item) },
                    onFavorite = { viewModel.setFavorite(item.id) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Historique des achats", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        if (history.isEmpty()) {
            Text("Aucun achat enregistré.")
        } else {
            history.forEach { entry ->
                PurchaseHistoryRow(entry = entry)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun WishItemCard(
    item: WishItemEntity,
    onPurchase: () -> Unit,
    onDelete: () -> Unit,
    onFavorite: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                WishItemImage(imageUrl = item.imageUrl)
                Spacer(modifier = Modifier.size(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = item.name, fontWeight = FontWeight.SemiBold)
                    Text(text = formatCents(item.priceCents))
                }
                IconButton(onClick = onFavorite) {
                    if (item.isFavorite) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = "Favori",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.FavoriteBorder,
                            contentDescription = "Mettre en favori"
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onPurchase) {
                    Text("Acheter")
                }
                OutlinedButton(onClick = onDelete) {
                    Text("Supprimer")
                }
            }
        }
    }
}

@Composable
private fun WishItemImage(imageUrl: String) {
    val shape = RoundedCornerShape(8.dp)
    if (imageUrl.isNotBlank()) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = Modifier
                .size(64.dp)
                .clip(shape)
        )
    } else {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(shape)
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = Icons.Filled.Image, contentDescription = null)
        }
    }
}

@Composable
private fun PurchaseHistoryRow(entry: PurchaseHistoryEntity) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(entry.itemName)
        Text(formatCents(entry.priceCents))
    }
}

private fun parsePriceToCents(input: String): Int? {
    val normalized = input.trim().replace(',', '.')
    val value = normalized.toDoubleOrNull() ?: return null
    return (value * 100).roundToInt().coerceAtLeast(0)
}

private fun formatCents(cents: Int): String {
    val euros = cents / 100.0
    return String.format("%.2f €", euros)
}
