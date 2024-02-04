package com.severett.transactionbank.controller

import com.severett.transactionbank.model.exception.AccountMissingException
import com.severett.transactionbank.model.exception.InternalException
import com.severett.transactionbank.model.exception.TransactionDisallowedException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.method.annotation.HandlerMethodValidationException
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@ControllerAdvice
class ControllerAdvice : ResponseEntityExceptionHandler() {

    @ExceptionHandler(InternalException::class)
    fun handleInternalException(ie: InternalException) = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR)

    @ExceptionHandler(AccountMissingException::class)
    fun handleAccountMissingException(ame: AccountMissingException) = ProblemDetail.forStatusAndDetail(
        HttpStatus.NOT_FOUND,
        ame.message ?: ""
    )

    @ExceptionHandler(TransactionDisallowedException::class)
    fun handleTransactionDisallowedException(tde: TransactionDisallowedException) = ProblemDetail.forStatusAndDetail(
        HttpStatus.BAD_REQUEST,
        tde.message ?: ""
    )

    override fun handleHandlerMethodValidationException(
        ex: HandlerMethodValidationException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        exchange: ServerWebExchange,
    ): Mono<ResponseEntity<Any>> {
        val message = ex.allValidationResults.joinToString("; ") { validationResult ->
            val errors = validationResult.resolvableErrors.map { error ->
                error.defaultMessage ?: ""
            }
            "'${validationResult.methodParameter.parameterName}' => $errors"
        }
        return Mono.just(
            ResponseEntity.of(ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, message))
                .build()
        )
    }
}
