package open.banking.open_banking_kafka.kafka

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

@Service
class KafkaConsumerService {

    @KafkaListener(topics = ["banking-transactions"], groupId = "banking-group")
    fun listen(record: ConsumerRecord<String, String>) {
        println("📥 Received message: ${record.value()} from topic: ${record.topic()}")
    }
}
