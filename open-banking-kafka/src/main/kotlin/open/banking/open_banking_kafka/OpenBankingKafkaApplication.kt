package open.banking.open_banking_kafka

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class OpenBankingKafkaApplication

fun main(args: Array<String>) {
	runApplication<OpenBankingKafkaApplication>(*args)
}