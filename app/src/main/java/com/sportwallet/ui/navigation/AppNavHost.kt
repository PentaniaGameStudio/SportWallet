package com.sportwallet.ui.navigation

import com.sportwallet.ui.screens.AdminScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sportwallet.ui.screens.ActivityScreen
import com.sportwallet.ui.screens.CalendarScreen
import com.sportwallet.ui.screens.HistoryScreen
import com.sportwallet.ui.screens.HomeScreen
import com.sportwallet.ui.screens.WishlistScreen
import com.sportwallet.ui.viewmodel.WalletViewModel

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController()
) {
    KeepScreenOn(enabled = true)
    val navBackStackEntry = navController.currentBackStackEntryAsState().value
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute != Routes.ADMIN) {
                BottomNavBar(navController)
            }
        }
    ) { padding ->
        AppNavGraph(
            navController = navController,
            padding = padding
        )
    }
}

@Composable
private fun KeepScreenOn(enabled: Boolean) {
    val view = LocalView.current
    DisposableEffect(view, enabled) {
        val previous = view.keepScreenOn
        view.keepScreenOn = enabled
        onDispose {
            view.keepScreenOn = previous
        }
    }
}

@Composable
private fun AppNavGraph(
    navController: NavHostController,
    padding: PaddingValues
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        modifier = Modifier.padding(padding)
    ) {
        composable(Routes.HOME) { HomeScreen(navController) }
        composable(Routes.WISHLIST) { WishlistScreen() }
        composable(Routes.HISTORY) { HistoryScreen() }
        composable(Routes.CALENDAR) { CalendarScreen() }
        composable(Routes.ACTIVITY) { ActivityScreen() }

        // ✅ ADMIN (route cachée, pas dans la BottomBar)
        composable(Routes.ADMIN) {
            val walletVm: WalletViewModel = viewModel()
            AdminScreen(
                onBack = { navController.popBackStack() },
                walletVm = walletVm
            )
        }
    }
}


