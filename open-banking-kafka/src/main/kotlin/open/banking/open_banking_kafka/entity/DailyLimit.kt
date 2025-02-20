package open.banking.open_banking_kafka.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import java.time.LocalDateTime

@Entity
//TODO: Change to TransactionLimit
data class DailyLimit (
    @Id val accountId: String,
    var withdrawnAmount: Double,
    var date: LocalDateTime
)