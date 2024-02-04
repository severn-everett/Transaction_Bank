package com.severett.transactionbank.model.request

import java.math.BigDecimal

data class WithdrawRequest(val accountId: String, val amount: BigDecimal)
