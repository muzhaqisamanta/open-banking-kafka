package open.banking.open_banking_kafka.controllers

import open.banking.open_banking_kafka.service.OpenBankingService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.lang.IllegalArgumentException

@RestController
@RequestMapping("/api/v1/openbanking/loans")
class LoanController(private val openBankingService: OpenBankingService) {
    @PostMapping("/{accountId}/apply")
    fun applyForLoan(
        @PathVariable accountId: String,
        @RequestParam loanAmount: Double,
        @RequestParam fixedMonthlyPayment: Double
    ): ResponseEntity<Any> {
        return try {
            val loan = openBankingService.addLoan(accountId, loanAmount)
            ResponseEntity.status(HttpStatus.CREATED).body(loan)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("error" to e.message))

        }
    }

    @PostMapping("/{accountId}/pay")
    fun payLoan(@PathVariable accountId: String): ResponseEntity<Any> {
        return try {
            val repayment = openBankingService.repayLoan(accountId)
            ResponseEntity.status(HttpStatus.CREATED).body(repayment)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("error" to e.message))

        }

    }
}