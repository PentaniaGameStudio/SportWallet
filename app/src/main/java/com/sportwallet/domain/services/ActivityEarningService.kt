package com.sportwallet.domain.services

import com.sportwallet.domain.model.ActivityType
import kotlin.math.floor

class ActivityEarningService {

    /**
     * Retourne le gain "flat" au centime près, calculé au prorata.
     * - Vélo : 10 min = 1€ => 600 s = 100 cents
     * - Marche : 15 min = 1€ => 900 s = 100 cents
     * - Autre : 15 min = 1€
     * - Off : 4€ instantané
     *
     * NOTE: on utilise floor pour ne jamais sur-créditer.
     */
    fun computeFlatEarnedCents(type: ActivityType, elapsedMs: Long): Int {
        return when (type) {
            ActivityType.BIKE -> prorataCents(elapsedMs, secondsPerEuro = 600)
            ActivityType.WALK -> prorataCents(elapsedMs, secondsPerEuro = 900)
            ActivityType.OTHER -> prorataCents(elapsedMs, secondsPerEuro = 900)
            ActivityType.OFF_DAY -> 400
        }
    }

    private fun prorataCents(elapsedMs: Long, secondsPerEuro: Int): Int {
        val seconds = elapsedMs.coerceAtLeast(0L) / 1000.0
        val centsPerSecond = 100.0 / secondsPerEuro.toDouble()
        return floor(seconds * centsPerSecond).toInt().coerceAtLeast(0)
    }
}
