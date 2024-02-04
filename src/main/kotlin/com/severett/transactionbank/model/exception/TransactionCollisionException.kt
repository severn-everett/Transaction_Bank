package com.severett.transactionbank.model.exception

class TransactionCollisionException : Exception() {
    // Disable the expensive stack-trace generation, as it's not needed
    // for processing instances of this exception type.
    override fun fillInStackTrace() = this
}
