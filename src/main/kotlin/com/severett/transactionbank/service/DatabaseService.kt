package com.severett.transactionbank.service

import com.severett.transactionbank.model.exception.AccountMissingException
import com.severett.transactionbank.model.exception.InternalException
import com.severett.transactionbank.model.transaction.AccountTransaction
import com.severett.transactionbank.model.transaction.TransactionType
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime

private val logger = KotlinLogging.logger { }

private const val ACCOUNT_ID_PARAM = "accountId"
private const val TYPE_PARAM = "type"
private const val SERIAL_NUMBER_PARAM = "serial_number"
private const val AMOUNT_PARAM = "amount"
private const val TIMESTAMP_PARAM = "timestamp"

private const val UNIQUE_CONSTRAINT_VIOLATION = 23505

@Service
class DatabaseService(private val namedParameterJdbcTemplate: NamedParameterJdbcOperations) {

    suspend fun getAmount(accountId: String) = withContext(Dispatchers.IO) {
        try {
            namedParameterJdbcTemplate.queryForObject(
                "SELECT amount FROM account WHERE id = :$ACCOUNT_ID_PARAM",
                MapSqlParameterSource().addValue(ACCOUNT_ID_PARAM, accountId),
                BigDecimal::class.java,
            ) ?: throw AccountMissingException("Account '$accountId' not found")
        } catch (e: Exception) {
            logger.error(e) { "A database exception occurred" }
            throw InternalException(e.message ?: "")
        }
    }

    suspend fun getTransactions(accountId: String) = withContext(Dispatchers.IO) {
        try {
            namedParameterJdbcTemplate.query(
                "SELECT type, serial_number, amount, timestamp FROM transaction WHERE account_id = :$ACCOUNT_ID_PARAM",
                MapSqlParameterSource().addValue(ACCOUNT_ID_PARAM, accountId)
            ) { rs, rowNum ->
                AccountTransaction(
                    type = TransactionType.valueOf(rs.getString(TYPE_PARAM)),
                    accountId = accountId,
                    serialNumber = rs.getLong(SERIAL_NUMBER_PARAM),
                    amount = BigDecimal.valueOf(rs.getDouble(AMOUNT_PARAM)),
                    timestamp = LocalDateTime.parse(rs.getString(TIMESTAMP_PARAM))
                )
            }
        } catch (e: Exception) {
            logger.error(e) { "A database exception occurred" }
            throw InternalException(e.message ?: "")
        }
    }

    @Transactional
    suspend fun saveTransactions(vararg transactions: AccountTransaction) = withContext(Dispatchers.IO) {
        try {
            transactions.forEach { transaction ->
                namedParameterJdbcTemplate.update(
                    "INSERT INTO transaction (account_id, type, serial_number, amount) " +
                            "VALUES (:$ACCOUNT_ID_PARAM, :$TYPE_PARAM, :$SERIAL_NUMBER_PARAM, :$AMOUNT_PARAM)",
                    MapSqlParameterSource()
                        .addValue(ACCOUNT_ID_PARAM, transaction.accountId)
                        .addValue(TYPE_PARAM, transaction.type)
                        .addValue(SERIAL_NUMBER_PARAM, transaction.serialNumber)
                        .addValue(AMOUNT_PARAM, transaction.amount)
                )
            }
        } catch (e: Exception) {
            logger.error(e) { "A database exception occurred" }
            throw InternalException(e.message ?: "")
        }
    }

}
