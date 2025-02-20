package open.banking.open_banking_kafka.repository

import open.banking.open_banking_kafka.entity.LoanPayment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface LoanPaymentRepository : JpaRepository<LoanPayment, String> {
}