package com.severett.transactionbank.service

import com.severett.transactionbank.model.account.AccountTransaction
import com.severett.transactionbank.model.account.TransactionType
import com.severett.transactionbank.model.exception.AccountMissingException
import com.severett.transactionbank.model.exception.InternalException
import com.severett.transactionbank.model.exception.TransactionCollisionException
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.dao.DuplicateKeyException
import org.springframework.jdbc.core.ResultSetExtractor
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

private val logger = KotlinLogging.logger { }

private const val ACCOUNT_ID_PARAM = "accountId"
private const val TYPE_PARAM = "type"
private const val SERIAL_NUMBER_PARAM = "serial_number"
private const val AMOUNT_PARAM = "amount"
private const val TIMESTAMP_PARAM = "timestamp"

private const val UNIQUE_CONSTRAINT_VIOLATION = "transaction_account_id_serial_number_key"

@Service
class DatabaseService(private val namedParameterJdbcTemplate: NamedParameterJdbcOperations) {

    suspend fun getAmount(accountId: String) = withContext(Dispatchers.IO) {
        try {
            namedParameterJdbcTemplate.query(
                "SELECT amount FROM account WHERE id = :$ACCOUNT_ID_PARAM",
                MapSqlParameterSource().addValue(ACCOUNT_ID_PARAM, accountId),
                ResultSetExtractor { rs -> rs.takeIf { rs.next() }?.let { rs.getBigDecimal(AMOUNT_PARAM) } }
            )
        } catch (e: Exception) {
            logger.error(e) { "A database exception occurred" }
            throw InternalException(e.message ?: "")
        } ?: throw AccountMissingException("Account '$accountId' not found")
    }

    suspend fun getTransactions(accountId: String): List<AccountTransaction> = withContext(Dispatchers.IO) {
        try {
            namedParameterJdbcTemplate.query(
                "SELECT type, serial_number, amount, timestamp FROM transaction WHERE account_id = :$ACCOUNT_ID_PARAM",
                MapSqlParameterSource().addValue(ACCOUNT_ID_PARAM, accountId)
            ) { rs, _ ->
                AccountTransaction(
                    type = TransactionType.valueOf(rs.getString(TYPE_PARAM)),
                    accountId = accountId,
                    serialNumber = rs.getLong(SERIAL_NUMBER_PARAM),
                    amount = BigDecimal.valueOf(rs.getDouble(AMOUNT_PARAM)),
                    timestamp = rs.getTimestamp(TIMESTAMP_PARAM).toLocalDateTime()
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
                            "VALUES (:$ACCOUNT_ID_PARAM, CAST(:$TYPE_PARAM AS transaction_type), " +
                            ":$SERIAL_NUMBER_PARAM, :$AMOUNT_PARAM)",
                    MapSqlParameterSource()
                        .addValue(ACCOUNT_ID_PARAM, transaction.accountId)
                        .addValue(TYPE_PARAM, transaction.type.name)
                        .addValue(SERIAL_NUMBER_PARAM, transaction.serialNumber)
                        .addValue(AMOUNT_PARAM, transaction.amount)
                )
            }
        } catch (e: Exception) {
            if (e is DuplicateKeyException && e.message?.contains(UNIQUE_CONSTRAINT_VIOLATION) == true) {
                logger.debug(e) { "A transaction collision occurred" }
                throw TransactionCollisionException()
            } else {
                logger.error(e) { "A database exception occurred" }
                throw InternalException(e.message ?: "")
            }
        }
    }
}
