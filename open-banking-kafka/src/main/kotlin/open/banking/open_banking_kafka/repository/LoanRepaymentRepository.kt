package open.banking.open_banking_kafka.repository

import open.banking.open_banking_kafka.entity.LoanRepayment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface LoanRepaymentRepository : JpaRepository<LoanRepayment, String> {
    fun findByLoanId(loanId: String): List<LoanRepayment>

    @Query("SELECT lr FROM LoanRepayment lr WHERE lr.loanId = :loanId ORDER BY lr.paymentDate DESC")
    fun findTopByLoanIdOrderByPaymentDateDesc(loanId: String): List<LoanRepayment>

}