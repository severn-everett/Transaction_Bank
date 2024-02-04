package com.severett.transactionbank.model.request

import java.math.BigDecimal

data class DepositRequest(val accountId: String, val amount: BigDecimal)
