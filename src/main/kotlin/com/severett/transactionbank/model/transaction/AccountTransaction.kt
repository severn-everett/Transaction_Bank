package com.severett.transactionbank.model.transaction

import java.math.BigDecimal
import java.time.Instant

data class AccountTransaction(
    val transactionType: TransactionType,
    val accountId: String,
    val serialNumber: Long,
    val amount: BigDecimal,
    val timestamp: Instant,
)
