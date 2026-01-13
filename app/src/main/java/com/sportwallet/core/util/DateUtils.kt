package com.sportwallet.core.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

object DateUtils {
    private val zone: ZoneId = ZoneId.systemDefault()

    fun today(): LocalDate = LocalDate.now(zone)

    fun epochMillisToLocalDate(epochMillis: Long): LocalDate =
        Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate()

    fun localDateToKey(date: LocalDate): String = date.toString() // yyyy-MM-dd
}
