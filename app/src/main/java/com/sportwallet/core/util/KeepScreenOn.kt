package com.sportwallet.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView

@Composable
fun KeepScreenOn(enabled: Boolean) {
    val view = LocalView.current

    DisposableEffect(enabled) {
        if (enabled) {
            view.keepScreenOn = true
        } else {
            view.keepScreenOn = false
        }

        onDispose {
            view.keepScreenOn = false
        }
    }
}
