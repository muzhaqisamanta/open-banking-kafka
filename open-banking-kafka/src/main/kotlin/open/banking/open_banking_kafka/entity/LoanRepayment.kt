package open.banking.open_banking_kafka.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import java.time.LocalDateTime

@Entity
data class LoanRepayment(
    @Id
    val repaymentId: String,
    val loanId: String,
    val accountId: String,
    val amountPaid: Double,
    var paymentDate: LocalDateTime
)