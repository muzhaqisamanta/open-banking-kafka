package open.banking.open_banking_kafka.repository

import open.banking.open_banking_kafka.entity.Account
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AccountRepository : JpaRepository<Account, String> {
//    fun findByAccountNumber(accountNumber: String)
}