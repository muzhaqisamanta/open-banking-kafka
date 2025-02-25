package open.banking.open_banking_kafka.kafka

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import open.banking.open_banking_kafka.kafka.dto.PaymentRequest
import open.banking.open_banking_kafka.kafka.dto.PaymentResponse
import open.banking.open_banking_kafka.kafka.enums.StatusEnum
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class KafkaConsumerService(
    private val kafkaTemplate: KafkaTemplate<String, String>
) {
    private val objectMapper = jacksonObjectMapper()

    @KafkaListener(topics = ["payment-requests-topic"], groupId = "payment-consumer-group")
    fun processPayment(message: String) {
        val payment = objectMapper.readValue(message, PaymentRequest::class.java)

        println("✅ Received payment request deserialized: $payment")

        val isApproved = payment.amount.toDouble() <= 500.00 // Approve if <= 500
        val response = PaymentResponse(
            transactionId = payment.transactionId,
            status = if (isApproved) StatusEnum.APPROVED else StatusEnum.REJECTED,
            message = if (isApproved) "Payment processed successfully" else "Insufficient funds"
        )
        val responseMessage = objectMapper.writeValueAsString(response)

        kafkaTemplate.send("payment-responses-topic", responseMessage)
        println("📤 Sent payment response: $responseMessage")
    }
}
