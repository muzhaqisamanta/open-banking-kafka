package open.banking.open_banking_kafka.controllers

import open.banking.open_banking_kafka.entity.Transaction
import open.banking.open_banking_kafka.service.OpenBankingService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/v1/openbanking/transactions")
class TransactionController(val openBankingService: OpenBankingService) {
    @GetMapping("/{accountId}")
    fun getAllTransactions(@PathVariable accountId: String): ResponseEntity<Any> {
        return try {
            openBankingService.showTransactions(accountId)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

}