package open.banking.open_banking_kafka.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
data class Account(
    @Id
    val accountNumber: String,
    var balance: Double
)
