package open.banking.open_banking_kafka.repository

import open.banking.open_banking_kafka.entity.Transaction
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TransactionRepository : JpaRepository<Transaction, String> {
    fun findAllByAccountId(accountId: String): List<Transaction>
}