package open.banking.open_banking_kafka.kafka.controllers

import open.banking.open_banking_kafka.kafka.KafkaProducerService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/kafka")
class KafkaController(private val kafkaProducer: KafkaProducerService) {

    @PostMapping("/publish")
    fun sendMessage(@RequestParam topic: String, @RequestParam message: String): String {
        kafkaProducer.sendMessage(topic, message)
        return "✅ Message sent to Kafka topic: $topic"
    }
}
