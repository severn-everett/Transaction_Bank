package com.severett.transactionbank.util

import java.math.BigDecimal

object Constants {
    @JvmStatic
    val DAILY_DEPOSIT_LIMIT = BigDecimal.valueOf(5000L)
    @JvmStatic
    val OVERDRAFT_LIMIT = BigDecimal.valueOf(-200)
}