package open.banking.open_banking_kafka.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import java.time.LocalDateTime
import kotlin.math.pow

@Entity
data class Loan(
    @Id
    val loanId: String,
    val accountId: String,
    val principalAmount: Double,
    var outstandingBalance: Double,
    val interestRate: Double,
    val termMonths: Int,
    var lastPaymentDate: LocalDateTime = LocalDateTime.now(),
    var isActive: Boolean = true
) {

    // 🔹 Calculate Monthly Payment
    //TODO: Fix logic to pay

    fun calculateMonthlyPayment(): Double {
        val monthlyRate = interestRate / 12 // Convert annual rate to monthly
        return if (monthlyRate == 0.0) {
            principalAmount / termMonths  // No interest case (simple division)
        } else {
            val factor = (1 + monthlyRate).pow(termMonths)
            (principalAmount * monthlyRate * factor) / (factor - 1)
        }
    }
}
