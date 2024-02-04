package com.severett.transactionbank.model.request

import java.math.BigDecimal

data class WithdrawalRequest(val accountId: String, val amount: BigDecimal)
