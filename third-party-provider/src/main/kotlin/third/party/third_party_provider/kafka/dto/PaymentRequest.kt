package third.party.third_party_provider.kafka.dto

data class PaymentRequest(
    val transactionId: String,
    val accountId: String,
    val amount: Double,
    val description: String
)