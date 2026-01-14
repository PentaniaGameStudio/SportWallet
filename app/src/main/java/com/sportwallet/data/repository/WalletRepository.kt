package com.sportwallet.data.repository

import android.content.Context
import com.sportwallet.core.util.DateUtils
import com.sportwallet.data.db.DatabaseProvider
import com.sportwallet.data.entities.DailyStatsEntity
import com.sportwallet.data.entities.TransactionEntity
import com.sportwallet.domain.model.ActivityType
import com.sportwallet.domain.services.ActivityEarningService
import com.sportwallet.domain.services.StreakService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import java.time.LocalDate

data class WalletState(
    val balanceCents: Int,
    val dayFlatCents: Int,        // 0..400
    val streakDays: Int,
    val bonusPercent: Int,
    val bonusGrantedCents: Int    // 0 tant que pas donné
)


class WalletRepository(context: Context) {

    private val db = DatabaseProvider.get(context)
    private val txDao = db.transactionDao()
    private val dayDao = db.dailyStatsDao()

    private val earningService = ActivityEarningService()
    private val streakService = StreakService()

    fun observeBalanceCents(): Flow<Int> = txDao.observeBalanceCents()

    fun observeTodayStats(): Flow<DailyStatsEntity?> {
        val todayKey = DateUtils.localDateToKey(DateUtils.today())
        return dayDao.observe(todayKey)
    }

    fun observeWalletState(): Flow<WalletState> {
        val todayKey = DateUtils.localDateToKey(DateUtils.today())

        return combine(
            txDao.observeBalanceCents(),
            dayDao.observe(todayKey)
        ) { balance, stats ->
            val safe = stats ?: defaultStatsForDay(todayKey, streakDays = 0)

            WalletState(
                balanceCents = balance,
                dayFlatCents = safe.flatEarnedCents,
                streakDays = safe.streakDays,
                bonusPercent = safe.bonusPercent,
                bonusGrantedCents = safe.bonusGrantedCents
            )
        }
    }



    suspend fun ensureTodayInitialized() {
        val today = DateUtils.today()
        ensureDayInitialized(today)
    }

    private suspend fun ensureDayInitialized(date: LocalDate) {
        val dayKey = DateUtils.localDateToKey(date)
        val existing = dayDao.get(dayKey)
        if (existing != null) return

        val prevKey = DateUtils.localDateToKey(date.minusDays(1))
        val prev = dayDao.getPreviousDay(prevKey)

        // Si hier cap atteint => streak +1 sinon reset
        val prevReachedCap = prev?.let { it.flatEarnedCents >= 400 } ?: false
        val newStreak = if (prevReachedCap) ((prev?.streakDays ?: 0) + 1) else 0

        val bonusPercent = streakService.bonusPercentFromStreak(newStreak)

        dayDao.upsert(
            DailyStatsEntity(
                dayKey = dayKey,
                flatEarnedCents = 0,
                streakDays = newStreak,
                bonusPercent = bonusPercent,
                bonusGrantedCents = 0,
                updatedAtEpochMs = System.currentTimeMillis()
            )
        )
    }

    private fun defaultStatsForDay(dayKey: String, streakDays: Int): DailyStatsEntity {
        val bonusPercent = streakService.bonusPercentFromStreak(streakDays)

        return DailyStatsEntity(
            dayKey = dayKey,
            flatEarnedCents = 0,
            streakDays = streakDays,
            bonusPercent = bonusPercent,
            bonusGrantedCents = 0,
            updatedAtEpochMs = System.currentTimeMillis()
        )
    }


    suspend fun applyActivityStop(type: ActivityType, elapsedMs: Long) {
        val today = DateUtils.today()
        ensureDayInitialized(today)

        val dayKey = DateUtils.localDateToKey(today)
        val stats = dayDao.get(dayKey) ?: return

        val flatEarn = earningService.computeFlatEarnedCents(type, elapsedMs)
        if (flatEarn <= 0) return

        // 1) Crédit flat (mais plafonné à 4€)
        val remainingFlat = (400 - stats.flatEarnedCents).coerceAtLeast(0)
        val flatCredited = flatEarn.coerceAtMost(remainingFlat)

        if (flatCredited > 0) {
            val label = when (type) {
                ActivityType.BIKE -> "Vélo"
                ActivityType.WALK -> "Marche"
                ActivityType.OTHER -> "Autre"
                ActivityType.OFF_DAY -> "Journée Off"
            }

            txDao.insert(
                TransactionEntity(
                    amountCents = flatCredited,
                    label = label,
                    timestampEpochMs = System.currentTimeMillis(),
                    dayKey = dayKey
                )
            )
        }

        val newFlat = (stats.flatEarnedCents + flatCredited).coerceAtMost(400)

        // 2) Si on vient d’atteindre 4€ et que le bonus n’a pas encore été donné -> on le donne d’un coup
        var newBonusGranted = stats.bonusGrantedCents
        val reachedFlatCapNow = (stats.flatEarnedCents < 400) && (newFlat >= 400)

        if (reachedFlatCapNow && stats.bonusGrantedCents == 0 && stats.bonusPercent > 0) {
            val bonusCents = streakService.bonusAmountCents(stats.bonusPercent)

            if (bonusCents > 0) {
                txDao.insert(
                    TransactionEntity(
                        amountCents = bonusCents,
                        label = "Bonus streak",
                        timestampEpochMs = System.currentTimeMillis(),
                        dayKey = dayKey
                    )
                )
                newBonusGranted = bonusCents
            }
        }

        // 3) Update daily_stats
        dayDao.upsert(
            stats.copy(
                flatEarnedCents = newFlat,
                bonusGrantedCents = newBonusGranted,
                updatedAtEpochMs = System.currentTimeMillis()
            )
        )
    }

    /**
     * Admin: wipe total DB (dev only)
     */
    suspend fun adminResetDatabase() {
        withContext(Dispatchers.IO) {
            db.clearAllTables()
        }
    }

    /**
     * Admin: read daily stats for any dayKey (yyyy-MM-dd)
     */
    suspend fun adminGetDay(dayKey: String): DailyStatsEntity? {
        return withContext(Dispatchers.IO) {
            dayDao.get(dayKey)
        }
    }

    /**
     * Admin: overwrite/insert daily stats for any dayKey (yyyy-MM-dd)
     */
    suspend fun adminUpsertDay(
        dayKey: String,
        flatEarnedCents: Int,
        streakDays: Int,
        bonusPercent: Int,
        bonusGrantedCents: Int
    ) {
        withContext(Dispatchers.IO) {
            dayDao.upsert(
                DailyStatsEntity(
                    dayKey = dayKey,
                    flatEarnedCents = flatEarnedCents.coerceIn(0, 400),
                    streakDays = streakDays.coerceAtLeast(0),
                    bonusPercent = bonusPercent.coerceIn(0, 50),
                    bonusGrantedCents = bonusGrantedCents.coerceAtLeast(0),
                    updatedAtEpochMs = System.currentTimeMillis()
                )
            )
        }
    }

    /**
     * Admin: manual transaction (solde + / -)
     * amountCents peut être négatif => retrait
     */
    suspend fun adminInsertTransaction(
        dayKey: String,
        amountCents: Int,
        label: String
    ) {
        withContext(Dispatchers.IO) {
            txDao.insert(
                TransactionEntity(
                    amountCents = amountCents,
                    label = label.ifBlank { "Admin" },
                    timestampEpochMs = System.currentTimeMillis(),
                    dayKey = dayKey
                )
            )
        }
    }


}
