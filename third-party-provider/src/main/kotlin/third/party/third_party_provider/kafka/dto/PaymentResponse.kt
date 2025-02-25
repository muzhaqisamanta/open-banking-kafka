package third.party.third_party_provider.kafka.dto

import third.party.third_party_provider.kafka.enums.StatusEnum

data class PaymentResponse(
    val transactionId: String,
    val status: StatusEnum,
    val message: String
)
