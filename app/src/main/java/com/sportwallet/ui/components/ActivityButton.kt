package com.sportwallet.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun ActivityButton(
    title: String,
    subtitle: String,
    @DrawableRes iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val shape = RoundedCornerShape(20.dp)

    // ✅ Parent "libre" : ne clippe pas, l'icône peut dépasser
    Box(
        modifier = modifier
            .width(300.dp)
            .height(120.dp)
            .alpha(if (enabled) 1f else 0.5f)
            .clickable(enabled = enabled, onClick = onClick)
    ) {
        // ✅ Fond "carte" (Surface), avec ombre + arrondi
        Surface(
            modifier = Modifier
                .width(250.dp)
                .height(90.dp)
                .offset(x = (45).dp)
                .offset(y = (15).dp)
                .shadow(elevation = 2.dp, shape = shape),
            shape = shape,
            color = MaterialTheme.colorScheme.surfaceContainerHighest
        ) {
            // Zone texte (on laisse de la place à gauche pour l’icône)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 86.dp, end = 16.dp, top = 18.dp, bottom = 18.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // ✅ Icône en overlay (hors "carte")
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = title,
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.CenterStart)
        )
    }
}

