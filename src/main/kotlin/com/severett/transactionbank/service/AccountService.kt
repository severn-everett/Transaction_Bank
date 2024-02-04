package com.severett.transactionbank.service

import com.severett.transactionbank.model.account.Account
import com.severett.transactionbank.model.account.AccountTransaction
import com.severett.transactionbank.model.account.TransactionType
import com.severett.transactionbank.model.exception.TransactionCollisionException
import com.severett.transactionbank.model.exception.TransactionDisallowedException
import com.severett.transactionbank.util.Constants.DAILY_DEPOSIT_LIMIT
import com.severett.transactionbank.util.Constants.OVERDRAFT_LIMIT
import com.severett.transactionbank.util.DateUtil.toDateStr
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class AccountService(private val dbService: DatabaseService) {

    suspend fun deposit(accountId: String, amount: BigDecimal) = execute {
        val account = getAccount(accountId)
        if (canDeposit(account = account, amount = amount)) {
            dbService.saveTransactions(
                AccountTransaction(
                    type = TransactionType.DEPOSIT,
                    accountId = accountId,
                    serialNumber = account.serialNumber + 1L,
                    amount = amount,
                    timestamp = LocalDateTime.now(),
                ),
            )
        } else {
            throw TransactionDisallowedException("Transaction would exceed daily deposit limit of $DAILY_DEPOSIT_LIMIT")
        }
    }

    suspend fun withdraw(accountId: String, amount: BigDecimal) = execute {
        val account = getAccount(accountId)
        if (canWithdraw(account = account, amount = amount)) {
            dbService.saveTransactions(
                AccountTransaction(
                    type = TransactionType.WITHDRAWAL,
                    accountId = accountId,
                    serialNumber = account.serialNumber + 1L,
                    amount = amount,
                    timestamp = LocalDateTime.now(),
                ),
            )
        } else {
            throw TransactionDisallowedException("Transaction would exceed overdraft limit of $OVERDRAFT_LIMIT")
        }
    }

    suspend fun transfer(fromAccountId: String, toAccountId: String, amount: BigDecimal) = execute {
        val fromAccount = getAccount(fromAccountId)
        val toAccount = getAccount(toAccountId)
        when {
            !canTransfer(account = fromAccount, amount = amount) -> {
                throw TransactionDisallowedException("Unable to overdraft on transfer")
            }

            !canDeposit(account = toAccount, amount = amount) -> {
                throw TransactionDisallowedException(
                    "Transaction would exceed daily deposit limit of $DAILY_DEPOSIT_LIMIT"
                )
            }

            else -> {
                val timestamp = LocalDateTime.now()
                dbService.saveTransactions(
                    AccountTransaction(
                        type = TransactionType.WITHDRAWAL,
                        accountId = fromAccountId,
                        serialNumber = fromAccount.serialNumber + 1L,
                        amount = amount,
                        timestamp = timestamp,
                    ),
                    AccountTransaction(
                        type = TransactionType.DEPOSIT,
                        accountId = fromAccountId,
                        serialNumber = fromAccount.serialNumber + 1L,
                        amount = amount,
                        timestamp = timestamp,
                    ),
                )
            }
        }
    }

    private suspend inline fun execute(serviceAction: () -> Unit) {
        var isSuccessful = false
        while (!isSuccessful) {
            try {
                serviceAction.invoke()
                isSuccessful = true
            } catch (e: Exception) {
                // Only abort if a transaction collision hasn't occurred
                if (e !is TransactionCollisionException) throw e
            }
        }
    }

    private suspend fun getAccount(accountId: String) =
        dbService.getTransactions(accountId).fold(Account(dbService.getAmount(accountId))) { account, transaction ->
            account.addTransaction(transaction)
            account
        }

    private fun canDeposit(account: Account, amount: BigDecimal): Boolean {
        val todayDateStr = LocalDateTime.now().toDateStr()
        val todayDepositAmount = account.depositCount[todayDateStr] ?: BigDecimal.ZERO
        return todayDepositAmount.add(amount) <= DAILY_DEPOSIT_LIMIT
    }

    private fun canWithdraw(account: Account, amount: BigDecimal) =
        account.amount.subtract(amount) >= OVERDRAFT_LIMIT

    private fun canTransfer(account: Account, amount: BigDecimal) =
        account.amount.subtract(amount) >= BigDecimal.ZERO
}
