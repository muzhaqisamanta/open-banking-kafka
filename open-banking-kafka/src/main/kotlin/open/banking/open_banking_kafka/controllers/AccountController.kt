package open.banking.open_banking_kafka.controllers

import open.banking.open_banking_kafka.entity.Account
import open.banking.open_banking_kafka.service.OpenBankingService
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/openbanking")

class AccountController(val openBankingService: OpenBankingService) {

    @GetMapping("/accounts")
    fun getAllAccounts(): ResponseEntity<List<Account>> {
        return ResponseEntity.ok(openBankingService.showAccounts())
    }

    @GetMapping("/accounts/{accountId}")
    fun getAccountById(@PathVariable accountId: String): ResponseEntity<Account> {
        return try {
            val account = openBankingService.getAccountById(accountId)
            if (account.isPresent) {
                ResponseEntity.ok(account.get()) // 200 OK
            } else {
                ResponseEntity.status(HttpStatus.NOT_FOUND).build() // 404 Not Found
            }
        } catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }
}