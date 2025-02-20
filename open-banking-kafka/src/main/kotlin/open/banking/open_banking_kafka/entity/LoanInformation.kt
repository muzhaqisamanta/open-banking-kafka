package open.banking.open_banking_kafka.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import java.time.LocalDateTime

@Entity
data class LoanInformation(
    @Id val accountNumber: String,
    val originalLoanAmount: Double,
    var remainingLoanAmount: Double,
    val fixedMonthlyPayment: Double,  // Fixed amount to be paid each month
    var lastPaymentDate: LocalDateTime,
    var isLoanActive: Boolean = true
)