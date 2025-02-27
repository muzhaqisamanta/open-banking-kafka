package third.party.third_party_provider.kafka.service

import com.fasterxml.jackson.databind.ObjectMapper
import third.party.third_party_provider.kafka.enums.StatusEnum
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service
import third.party.third_party_provider.kafka.dto.PaymentResponse

@Service
class PaymentResponseConsumer(private val objectMapper: ObjectMapper) {

    @KafkaListener(topics = ["payment-responses-topic"], groupId = "vodafone-group")
    fun handlePaymentResponse(message: String) {
        println("📩 Received raw payment response: $message")

        val response = objectMapper.readValue(message, PaymentResponse::class.java)
        println("✅ Deserialized payment response: $response")

        if (response.status == StatusEnum.APPROVED) {
            println("✅ Payment approved! Updating Vodafone records...")
        } else {
            println("❌ Payment rejected! Notifying customer...")
        }
    }
}
