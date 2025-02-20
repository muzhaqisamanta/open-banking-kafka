package open.banking.open_banking_kafka.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import java.time.LocalDateTime

@Entity
data class LoanPayment(
    @Id val accountNumber: String,
    val paymentAmount: Double,
    var paymentDate: LocalDateTime
)
