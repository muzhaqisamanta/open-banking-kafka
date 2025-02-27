package third.party.third_party_provider.kafka.controllers

import third.party.third_party_provider.kafka.service.KafkaPaymentProducerService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/payments")
class PaymentController(
    private val paymentProducerService: KafkaPaymentProducerService
) {
    @PostMapping
    fun makePayment(
        @RequestParam accountId: String,
        @RequestParam amount: Double,
        @RequestParam description: String
    ): String {
        paymentProducerService.sendPaymentRequest(accountId, amount, description)
        return "Payment request sent for $amount to account $accountId"
    }
}
