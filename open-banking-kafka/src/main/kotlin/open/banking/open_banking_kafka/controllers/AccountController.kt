package open.banking.open_banking_kafka.controllers

import open.banking.open_banking_kafka.entity.Account
import open.banking.open_banking_kafka.service.OpenBankingService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/openbanking/accounts")

class AccountController(val openBankingService: OpenBankingService) {

    @GetMapping("")
    fun getAllAccounts(): ResponseEntity<List<Account>> {
        return ResponseEntity.ok(openBankingService.showAccounts())
    }

    @GetMapping("/{accountId}")
    fun getAccountById(@PathVariable accountId: String): ResponseEntity<Account> {
        return try {
            openBankingService.getAccountById(accountId)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    @PostMapping("/{accountNumber}")
    fun addAccount(@PathVariable accountNumber: String): ResponseEntity<Account> {
        return try {
            openBankingService.addAccount(accountNumber)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    @GetMapping("/{accountId}/balance")
    fun getBalanceByAccountId(@PathVariable accountId: String): ResponseEntity<Double> {
        return try {
            openBankingService.checkBalance(accountId)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)

        }
    }


}