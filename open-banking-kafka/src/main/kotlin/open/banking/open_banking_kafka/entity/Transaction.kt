package open.banking.open_banking_kafka.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import open.banking.open_banking_kafka.enums.TransactionTypeEnum

@Entity
data class Transaction(
    @Id val accountNumber: String, val transactionType: TransactionTypeEnum, val message: String, val amount: Double
)
