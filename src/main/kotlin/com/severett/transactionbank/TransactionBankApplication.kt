package com.severett.transactionbank

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TransactionBankApplication

fun main(args: Array<String>) {
	runApplication<TransactionBankApplication>(*args)
}
