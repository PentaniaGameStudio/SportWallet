package com.sportwallet.ui.navigation

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.sportwallet.R

private data class BottomItem(
    val route: String,
    val label: String,
    @DrawableRes val iconRes: Int
)

@Composable
fun BottomNavBar(navController: NavHostController) {
    val items = listOf(
        BottomItem(Routes.HOME, "Accueil", R.drawable.ic_menu),
        BottomItem(Routes.WISHLIST, "Envies", R.drawable.ic_envies),
        BottomItem(Routes.HISTORY, "Historique", R.drawable.ic_historique),
        BottomItem(Routes.CALENDAR, "Calendrier", R.drawable.ic_calendar),
        BottomItem(Routes.ACTIVITY, "Activité", R.drawable.ic_activity)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        items.forEach { item ->
            val selected = currentRoute == item.route

            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (selected) {
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                        }

                        Icon(
                            painter = painterResource(id = item.iconRes),
                            contentDescription = item.label,
                            modifier = Modifier.size(32.dp),
                            tint = if (selected) Color.Unspecified
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                label = null
            )
        }
    }
}
