package open.banking.open_banking_kafka.repository

import open.banking.open_banking_kafka.entity.Loan
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface LoanRepository : JpaRepository<Loan, String> {
    fun findByAccountId(accountId: String): List<Loan>
    fun findByAccountIdAndIsActive(accountId: String, isActive: Boolean): Loan?

}