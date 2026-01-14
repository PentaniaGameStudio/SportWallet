package com.sportwallet.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
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
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sportwallet.data.entities.WishItemEntity
import com.sportwallet.R
import com.sportwallet.ui.viewmodel.WalletViewModel
import com.sportwallet.ui.viewmodel.WishlistViewModel
import kotlin.math.roundToInt
import android.graphics.BitmapFactory
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

// Palette des couleurs personnalisables pour l'onglet Envies.
private object WishlistPalette {
    val defaultCardColor   = Color(0xFFF1E7EB) // Gris rosé clair
    val purchasedCardColor = Color(0xFFE7C3A2) // Beige doré doux
    val favoriteCardColor  = Color(0xFFE5A4B3) // Rose poudré
}

@Composable
fun WishlistScreen(
    viewModel: WishlistViewModel = viewModel(),
    walletViewModel: WalletViewModel = viewModel()
) {
    val items by viewModel.items.collectAsState()
    val favorite by viewModel.favoriteItem.collectAsState()
    val walletState by walletViewModel.walletState.collectAsState()
    val displayedItems = remember(items, favorite) {
        items.filter { it.id != favorite?.id }
    }

    var showDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<WishItemEntity?>(null) }
    var dialogName by remember { mutableStateOf("") }
    var dialogImageUri by remember { mutableStateOf<String?>(null) }
    var dialogPriceInput by remember { mutableStateOf("") }
    var pendingPurchase by remember { mutableStateOf<WishItemEntity?>(null) }
    var showInsufficientFunds by remember { mutableStateOf(false) }
    val balanceCents = walletState?.balanceCents ?: 0
    val context = LocalContext.current
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        dialogImageUri = uri?.toString()
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
        }
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
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(16.dp))

        val favoriteItem = favorite
        if (favoriteItem != null) {
            Text(
                text = "Favori",
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(8.dp))
            FavoriteWishItemCard(
                item = favoriteItem,
                onPurchase = {
                    if (balanceCents >= favoriteItem.priceCents) {
                        pendingPurchase = favoriteItem
                    } else {
                        showInsufficientFunds = true
                    }
                },
                onDelete = { viewModel.deleteItem(favoriteItem) },
                onFavorite = { viewModel.setFavorite(favoriteItem.id) },
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
                style = MaterialTheme.typography.bodyMedium,
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
        Text(
            text = "Objets",
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (displayedItems.isEmpty()) {
            Text("Aucun objet pour le moment.")
        } else {
            displayedItems.forEach { item ->
                WishItemCard(
                    item = item,
                    onPurchase = {
                        if (balanceCents >= item.priceCents) {
                            pendingPurchase = item
                        } else {
                            showInsufficientFunds = true
                        }
                    },
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

    if (pendingPurchase != null) {
        val item = pendingPurchase
        AlertDialog(
            onDismissRequest = { pendingPurchase = null },
            confirmButton = {
                Button(onClick = {
                    item?.let { viewModel.purchaseItem(it) }
                    pendingPurchase = null
                }) {
                    Text("Acheter")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { pendingPurchase = null }) {
                    Text("Annuler")
                }
            },
            title = { Text("Confirmation") },
            text = {
                Text(
                    text = "Êtes vous sur de vouloir acheter ?"
                )
            }
        )
    }

    if (showInsufficientFunds) {
        AlertDialog(
            onDismissRequest = { showInsufficientFunds = false },
            confirmButton = {
                Button(onClick = { showInsufficientFunds = false }) {
                    Text("OK")
                }
            },
            title = { Text("Achat impossible") },
            text = {
                Text(
                    text = "Vous n'avez pas assez."
                )
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
            .pointerInput(item) {
                detectTapGestures(
                    onDoubleTap = { onEdit(item) }
                )
            },
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
                    Text(
                        text = item.name,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                    Text(text = formatCents(item.priceCents), color = Color.Black)
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
            .pointerInput(item) {
                detectTapGestures(
                    onDoubleTap = { onEdit(item) }
                )
            },
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
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Black
                    )
                    Text(text = formatCents(item.priceCents), color = Color.Black)
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
    val fallback: Painter = painterResource(R.drawable.ic_envies)
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
            modifier = Modifier
                .height(size)
                .aspectRatio(aspectRatio, matchHeightConstraintsFirst = true)
                .clip(shape)
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
                val keyboardController = LocalSoftwareKeyboardController.current

                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("Nom") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done,
                        autoCorrect = false
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                        }
                    )
                )

                OutlinedTextField(
                    value = priceInput,
                    onValueChange = { newValue ->
                        onPriceChange(sanitizeEuroInput(newValue))
                    },
                    label = { Text("Prix (€)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                        }
                    )
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
    return String.format(Locale.FRANCE, "%.2f", euros)
}

private fun formatCents(cents: Int): String {
    val euros = cents / 100.0
    return String.format(Locale.FRANCE, "%.2f €", euros)
}

private fun sanitizeEuroInput(input: String): String {
    // Normalise ',' en '.' pour simplifier
    val raw = input.trim().replace(',', '.')

    // Garde seulement chiffres et '.'
    val allowed = raw.filter { it.isDigit() || it == '.' }

    // Coupe à 1 seul point
    val firstDotIndex = allowed.indexOf('.')
    if (firstDotIndex < 0) return allowed

    val before = allowed.substring(0, firstDotIndex)
    val afterRaw = allowed.substring(firstDotIndex + 1).replace(".", "")
    val after = afterRaw.take(2) // 2 décimales max

    return "$before.$after"
}
