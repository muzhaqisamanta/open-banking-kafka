package third.party.third_party_provider.kafka.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import third.party.third_party_provider.kafka.dto.PaymentRequest
import java.util.UUID

@Service
@EnableKafka
class KafkaPaymentProducerService(
    private val kafkaTemplate: KafkaTemplate<String, String>, private val objectMapper: ObjectMapper
) {
    fun sendPaymentRequest(accountId: String, amount: Double, description: String) {
        val payment = PaymentRequest(
            transactionId = UUID.randomUUID().toString(),
            accountId = accountId,
            amount = amount,
            description = description
        )
        val message = objectMapper.writeValueAsString(payment)

        kafkaTemplate.send("payment-requests-topic", message)
        println("📨 Sent message: $message to topic payment-requests-topic")
    }
}

