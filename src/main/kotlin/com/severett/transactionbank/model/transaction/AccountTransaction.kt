package com.severett.transactionbank.model.transaction

import java.math.BigDecimal
import java.time.LocalDateTime

data class AccountTransaction(
    val type: TransactionType,
    val accountId: String,
    val serialNumber: Long,
    val amount: BigDecimal,
    val timestamp: LocalDateTime,
)
