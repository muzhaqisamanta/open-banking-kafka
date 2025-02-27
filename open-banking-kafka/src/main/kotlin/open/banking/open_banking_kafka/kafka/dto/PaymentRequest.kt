package open.banking.open_banking_kafka.kafka.dto

data class PaymentRequest(
    val transactionId: String,
    val accountId: String,
    val amount: Double,
    val description: String
)