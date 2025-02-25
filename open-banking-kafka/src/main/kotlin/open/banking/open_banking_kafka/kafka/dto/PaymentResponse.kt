package open.banking.open_banking_kafka.kafka.dto

import open.banking.open_banking_kafka.kafka.enums.StatusEnum

data class PaymentResponse(
    val transactionId: String,
    val status: StatusEnum,
    val message: String
)