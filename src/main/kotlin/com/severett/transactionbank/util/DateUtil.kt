package com.severett.transactionbank.util

import java.time.LocalDateTime

object DateUtil {
    fun LocalDateTime.toDateStr() = "$dayOfYear-$year"
}