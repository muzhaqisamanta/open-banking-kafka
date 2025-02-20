package open.banking.open_banking_kafka.repository

import open.banking.open_banking_kafka.entity.DailyLimit
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DailyLimitRespository : JpaRepository<DailyLimit, String> {
}