package com.severett.transactionbank.controller

import com.severett.transactionbank.service.AccountService
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal

private const val NON_POSITIVE_AMOUNT_MESSAGE = "Amount must be a positive number"

@RestController
class TransactionController(private val accountService: AccountService) {
    @PostMapping("/deposit")
    suspend fun deposit(
        @RequestParam
        @NotBlank
        accountId: String,
        @RequestParam
        @DecimalMin(value = "0.0", inclusive = false, message = NON_POSITIVE_AMOUNT_MESSAGE)
        amount: BigDecimal,
    ) {
        accountService.deposit(accountId = accountId, amount = amount)
    }

    @PostMapping("/withdraw")
    suspend fun withdraw(
        @RequestParam
        @NotBlank
        accountId: String,
        @RequestParam
        @DecimalMin(value = "0.0", inclusive = false, message = NON_POSITIVE_AMOUNT_MESSAGE)
        amount: BigDecimal,
    ) {
        accountService.withdraw(accountId = accountId, amount = amount)
    }

    @PostMapping("/transfer")
    suspend fun transfer(
        @RequestParam
        @NotBlank
        fromAccountId: String,
        @RequestParam
        @NotBlank
        toAccountId: String,
        @RequestParam
        @DecimalMin(value = "0.0", inclusive = false, message = NON_POSITIVE_AMOUNT_MESSAGE)
        amount: BigDecimal,
    ) {
        accountService.transfer(fromAccountId = fromAccountId, toAccountId = toAccountId, amount = amount)
    }
}
