package open.banking.open_banking_kafka.repository

import open.banking.open_banking_kafka.entity.WithdrawDailyLimit
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface WithdrawDailyLimitRepository : JpaRepository<WithdrawDailyLimit, String> {
    fun findAllByAccountId(accountId: String): List<WithdrawDailyLimit>
}