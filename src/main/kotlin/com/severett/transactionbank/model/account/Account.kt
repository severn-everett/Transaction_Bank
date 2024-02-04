package com.severett.transactionbank.model.account

import com.severett.transactionbank.util.DateUtil.toDateStr
import java.math.BigDecimal
import kotlin.math.max

class Account(amount: BigDecimal) {
    var amount = amount
        private set
    var serialNumber = 0L
        private set
    private val _depositCount = mutableMapOf<String, BigDecimal>()
    val depositCount: Map<String, BigDecimal>
        get() = _depositCount

    fun addTransaction(accountTransaction: AccountTransaction) {
        when (accountTransaction.type) {
            TransactionType.DEPOSIT -> {
                amount = amount.add(accountTransaction.amount)
                _depositCount.compute(accountTransaction.timestamp.toDateStr()) { _, currentAmount ->
                    currentAmount?.let { currentAmount.add(accountTransaction.amount) } ?: accountTransaction.amount
                }
            }

            TransactionType.WITHDRAWAL -> {
                amount = amount.subtract(accountTransaction.amount)
            }
        }
        serialNumber = max(serialNumber, accountTransaction.serialNumber)
    }
}