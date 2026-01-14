package com.sportwallet.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.sportwallet.data.entities.WishItemEntity
import com.sportwallet.R
import com.sportwallet.ui.viewmodel.WishlistViewModel
import kotlin.math.roundToInt

private object WishlistPalette {
    val defaultCardColor = Color(0xFFF5F5F5)
    val purchasedCardColor = Color(0xFFE0E0E0)
    val favoriteCardColor = Color(0xFFFFF3E0)
}

@Composable
fun WishlistScreen(viewModel: WishlistViewModel = viewModel()) {
    val items by viewModel.items.collectAsState()
    val favorite by viewModel.favoriteItem.collectAsState()
    val displayedItems = remember(items, favorite) {
        items.filter { it.id != favorite?.id }
    }

    var showDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<WishItemEntity?>(null) }
    var dialogName by remember { mutableStateOf("") }
    var dialogImageUri by remember { mutableStateOf<String?>(null) }
    var dialogPriceInput by remember { mutableStateOf("") }
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        dialogImageUri = uri?.toString()
    }

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

        if (favorite != null) {
            Text(text = "Favori", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            FavoriteWishItemCard(
                item = favorite,
                onPurchase = { viewModel.purchaseItem(favorite) },
                onDelete = { viewModel.deleteItem(favorite) },
                onFavorite = { viewModel.setFavorite(favorite.id) },
                onEdit = { item ->
                    editingItem = item
                    dialogName = item.name
                    dialogImageUri = item.imageUrl
                    dialogPriceInput = formatPriceInput(item.priceCents)
                    showDialog = true
                }
            )
        } else {
            Text(
                text = "Aucun favori sélectionné.",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                editingItem = null
                dialogName = ""
                dialogImageUri = null
                dialogPriceInput = ""
                showDialog = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ajouter un objet")
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Objets", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        if (displayedItems.isEmpty()) {
            Text("Aucun objet pour le moment.")
        } else {
            displayedItems.forEach { item ->
                WishItemCard(
                    item = item,
                    onPurchase = { viewModel.purchaseItem(item) },
                    onDelete = { viewModel.deleteItem(item) },
                    onFavorite = { viewModel.setFavorite(item.id) },
                    onEdit = { editItem ->
                        editingItem = editItem
                        dialogName = editItem.name
                        dialogImageUri = editItem.imageUrl
                        dialogPriceInput = formatPriceInput(editItem.priceCents)
                        showDialog = true
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    if (showDialog) {
        WishItemDialog(
            title = if (editingItem == null) "Nouvel objet" else "Modifier l'objet",
            confirmLabel = if (editingItem == null) "Ajouter" else "Enregistrer",
            name = dialogName,
            imageUri = dialogImageUri,
            priceInput = dialogPriceInput,
            onNameChange = { dialogName = it },
            onPriceChange = { dialogPriceInput = it },
            onPickImage = {
                imagePicker.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onClearImage = { dialogImageUri = null },
            onDismiss = { showDialog = false },
            onConfirm = {
                val priceCents = parsePriceToCents(dialogPriceInput)
                if (dialogName.isBlank() || priceCents == null) return@WishItemDialog
                val imageValue = dialogImageUri.orEmpty()
                val item = editingItem
                if (item == null) {
                    viewModel.addItem(dialogName, imageValue, priceCents)
                } else {
                    viewModel.updateItem(item, dialogName, imageValue, priceCents)
                }
                showDialog = false
            }
        )
    }
}

@Composable
private fun WishItemCard(
    item: WishItemEntity,
    onPurchase: () -> Unit,
    onDelete: () -> Unit,
    onFavorite: () -> Unit,
    onEdit: (WishItemEntity) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {},
                onDoubleClick = { onEdit(item) }
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isPurchased) {
                WishlistPalette.purchasedCardColor
            } else {
                WishlistPalette.defaultCardColor
            }
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                WishItemImage(imageUrl = item.imageUrl, size = 64.dp)
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
private fun FavoriteWishItemCard(
    item: WishItemEntity,
    onPurchase: () -> Unit,
    onDelete: () -> Unit,
    onFavorite: () -> Unit,
    onEdit: (WishItemEntity) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {},
                onDoubleClick = { onEdit(item) }
            ),
        colors = CardDefaults.cardColors(containerColor = WishlistPalette.favoriteCardColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                WishItemImage(imageUrl = item.imageUrl, size = 96.dp)
                Spacer(modifier = Modifier.size(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(text = formatCents(item.priceCents))
                }
                IconButton(onClick = onFavorite) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = "Favori",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
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
private fun WishItemImage(imageUrl: String, size: Dp) {
    val shape = RoundedCornerShape(8.dp)
    val fallback: Painter = painterResource(R.drawable.obj_none)
    if (imageUrl.isNotBlank()) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = Modifier
                .size(size)
                .clip(shape),
            placeholder = fallback,
            error = fallback
        )
    } else {
        Image(
            painter = fallback,
            contentDescription = null,
            modifier = Modifier
                .size(size)
                .clip(shape)
        )
    }
}

@Composable
private fun WishItemDialog(
    title: String,
    confirmLabel: String,
    name: String,
    imageUri: String?,
    priceInput: String,
    onNameChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onPickImage: () -> Unit,
    onClearImage: () -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Annuler")
            }
        },
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                WishItemImage(imageUrl = imageUri.orEmpty(), size = 120.dp)
                OutlinedButton(onClick = onPickImage, modifier = Modifier.fillMaxWidth()) {
                    Text("Choisir une image depuis la galerie")
                }
                if (!imageUri.isNullOrBlank()) {
                    OutlinedButton(onClick = onClearImage, modifier = Modifier.fillMaxWidth()) {
                        Text("Retirer l'image")
                    }
                }
                Text(
                    text = if (imageUri.isNullOrBlank()) {
                        "Aucune image sélectionnée"
                    } else {
                        "Image sélectionnée"
                    },
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("Nom") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = priceInput,
                    onValueChange = onPriceChange,
                    label = { Text("Prix (€)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}

private fun parsePriceToCents(input: String): Int? {
    val normalized = input.trim().replace(',', '.')
    val value = normalized.toDoubleOrNull() ?: return null
    return (value * 100).roundToInt().coerceAtLeast(0)
}

private fun formatPriceInput(cents: Int): String {
    val euros = cents / 100.0
    return String.format("%.2f", euros)
}

private fun formatCents(cents: Int): String {
    val euros = cents / 100.0
    return String.format("%.2f €", euros)
}
