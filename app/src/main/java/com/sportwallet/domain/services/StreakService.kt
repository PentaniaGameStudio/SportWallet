package com.sportwallet.domain.services

import kotlin.math.roundToInt

class StreakService {

    fun bonusPercentFromStreak(streakDays: Int): Int {
        // +10% par jour, max 50%
        return (streakDays * 10).coerceIn(0, 50)
    }

    fun bonusAmountCents(bonusPercent: Int): Int {
        // Bonus = bonus% de 4€ (base flat)
        return (400.0 * (bonusPercent / 100.0)).roundToInt()
    }

    fun totalMaxCents(bonusPercent: Int): Int {
        // Total max affichable : 4€ + bonus
        return 400 + bonusAmountCents(bonusPercent)
    }
}
