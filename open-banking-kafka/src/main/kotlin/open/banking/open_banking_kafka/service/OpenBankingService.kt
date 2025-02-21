package open.banking.open_banking_kafka.service


import open.banking.open_banking_kafka.entity.*
import open.banking.open_banking_kafka.enums.TransactionTypeEnum
import open.banking.open_banking_kafka.repository.AccountRepository
import open.banking.open_banking_kafka.repository.TransactionRepository
import open.banking.open_banking_kafka.repository.WithdrawDailyLimitRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

@Service
class OpenBankingService(
    val accountRepository: AccountRepository,
    val transactionRepository: TransactionRepository,
    val withdrawDailyLimitRepository: WithdrawDailyLimitRepository
) {
//    private val transactions = mutableListOf<Transaction>()

    private val DAILY_LIMIT = 500

    // Show account details
    fun showAccounts(): List<Account> {
        val accounts = accountRepository.findAll()
        println("Your Accounts:")
        accounts.forEach { println("Account Number: ${it.accountNumber}, Balance: ${it.balance}") }
        return accounts
    }

    fun getAccountById(accountId: String): ResponseEntity<Account> {
        val accountById = accountRepository.findById(accountId)
        return if (accountById.isPresent) {
            ResponseEntity.ok(accountById.get())
        } else {
            ResponseEntity.notFound().build()
        }
    }


    //Add new account
    fun addAccount(accountNumber: String): ResponseEntity<Account> {
        val existingAccount = accountRepository.findById(accountNumber).getOrNull()
        return if (existingAccount == null) {
            val newAccount = Account(accountNumber, 0.00)
            accountRepository.save(newAccount)
            ResponseEntity.status(HttpStatus.CREATED).body(newAccount)
        } else {
            ResponseEntity.status(HttpStatus.CONFLICT).body(null)
        }
    }

    // Check balance for an account
    fun checkBalance(accountId: String): ResponseEntity<Double> {
        val account = accountRepository.findById(accountId).getOrNull()
        return if (account != null) {
            val balance = account.balance
            ResponseEntity.ok(balance)
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }
    }

    // Show all transactions for an account
    fun showTransactions(accountId: String): ResponseEntity<Any> {
        val account = accountRepository.findById(accountId).getOrNull()
        val transactions = transactionRepository.findAllByAccountId(accountId)
        return if (account == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("status" to 404, "message" to "Account Id not found"))
        } else if (transactions != null) {
            ResponseEntity.ok(transactions)
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("status" to 404, "message" to "No Transaction found for this account"))
        }
    }

    // Transfer money between accounts
    fun transferMoney(fromAccountId: String, toAccountId: String, amount: Double): Transaction {
        val fromAccount = accountRepository.findById(fromAccountId).orElseThrow {
            IllegalArgumentException("Sender account not found")
        }
        val toAccount = accountRepository.findById(toAccountId).orElseThrow {
            IllegalArgumentException("Receiver account not found")
        }
        if (fromAccount.balance < amount) {
            throw IllegalArgumentException("Insufficient funds")
        }

        fromAccount.balance -= amount
        toAccount.balance += amount
        accountRepository.save(fromAccount)
        accountRepository.save(toAccount)

        val transaction = Transaction(
            transactionId = UUID.randomUUID().toString(),
            accountId = fromAccountId,
            transactionType = TransactionTypeEnum.TRANSFER,
            message = "Transferred $amount to account $toAccountId",
            amount = amount
        )
        return transactionRepository.save(transaction)
    }

    //Deposit money to an account
    fun depositMoney(accountId: String, amount: Double): Transaction {
        val accountToDeposit = accountRepository.findById(accountId).orElseThrow {
            IllegalArgumentException("Account $accountId not found")
        }
        if (amount <= 0) {
            throw IllegalArgumentException("Add an amount greater than 0.00")
        }

        accountToDeposit.balance += amount
        accountRepository.save(accountToDeposit)
        val transaction = Transaction(
            transactionId = UUID.randomUUID().toString(),
            accountId = accountId,
            transactionType = TransactionTypeEnum.DEPOSIT,
            message = "Deposited $amount to account $accountId",
            amount = amount
        )
        return transactionRepository.save(transaction)
    }

    //Withdraw money
    fun withdrawMoney(accountId: String, amount: Double): Transaction {
        val accountToWithdraw = accountRepository.findById(accountId).orElseThrow {
            IllegalArgumentException("Account $accountId not found")
        }
        if (amount <= 0) {
            throw IllegalArgumentException("This account does not have that amount")
        }
        if (amount > DAILY_LIMIT) {
            throw IllegalArgumentException("Daily withdrawal limit exceeded")
        }
        checkDailyLimit(accountId, amount, accountToWithdraw)
        accountToWithdraw.balance -= amount
        accountRepository.save(accountToWithdraw)

        val transaction = Transaction(
            transactionId = UUID.randomUUID().toString(),
            accountId = accountId,
            transactionType = TransactionTypeEnum.WITHDRAWAL,
            message = "Withdrew $amount from account $accountId",
            amount = amount
        )
        return transactionRepository.save(transaction)
    }

    private fun checkDailyLimit(
        accountId: String, amount: Double, currentAccount: Account
    ) {
        val withdrawnList = withdrawDailyLimitRepository.findAllByAccountId(accountId)

        val withdrawAccount = withdrawnList.filter { it.date.toLocalDate() == LocalDate.now() }.maxByOrNull { it.date }

        if (withdrawAccount == null) {
            withdrawDailyLimitRepository.save(
                WithdrawDailyLimit(
                    UUID.randomUUID().toString(), accountId, amount, LocalDateTime.now()
                )
            )
        } else {
            if (withdrawAccount.withdrawnAmount + amount > DAILY_LIMIT) {
                throw IllegalArgumentException("❌ Daily withdrawal limit exceeded")
            }
            withdrawAccount.withdrawnAmount += amount
            withdrawAccount.date = LocalDateTime.now()
            withdrawDailyLimitRepository.save(withdrawAccount)
        }
    }

//
//    fun payMonthlyDebt(
//        loans: MutableMap<String, LoanInformation>,
//        monthlyLoanPayments: MutableMap<String, MutableList<LoanPayment>>,
//        accountNumber: String
//    ) {
//        val account = accounts.find { it.accountNumber == accountNumber }
//        if (account == null) {
//            println("This account does not exist.")
//            return
//        }
//
//        val loanForAccount = loans[accountNumber]
//        if (loanForAccount == null) {
//            println("This account does not have an active loan.")
//            return
//        }
//        if (account.balance < loanForAccount.fixedMonthlyPayment) {
//            println("Not enough balance in account $accountNumber to pay ${loanForAccount.fixedMonthlyPayment}.")
//        }
//        val loanPayments = monthlyLoanPayments[accountNumber] ?: mutableListOf() // ✅ Ensure non-null list
//        val currentMonth = LocalDateTime.now().monthValue
//
//        if (loanPayments.any { it.paymentDate.monthValue == currentMonth }) {
//            println("⚠️ Loan for account $accountNumber has already been paid this month.")
//            return
//        }
//
//        // Process loan payment
//        val newLoanPayment = LoanPayment(
//            paymentDate = LocalDateTime.now(),
//            paymentAmount = loanForAccount.fixedMonthlyPayment,
//            accountNumber = accountNumber
//        )
//
//        // Add the new payment to the list for the account
//        loanPayments.add(newLoanPayment)
//        loanForAccount.remainingLoanAmount -= loanForAccount.fixedMonthlyPayment
//        account.balance -= loanForAccount.fixedMonthlyPayment
//
//        println("✅ Payment of ${loanForAccount.fixedMonthlyPayment} made for account $accountNumber.")
//        println("💰 New balance: ${account.balance}, Remaining Loan: ${loanForAccount.remainingLoanAmount}")
//
//        transactions.add(
//            Transaction(
//                accountNumber,
//                TransactionType.LOAN_REPAYMENT,
//                message = "Loan Repayment for account $accountNumber",
//                amount = loanForAccount.fixedMonthlyPayment
//            )
//        )
//
//        // ✅ Remove loan if fully paid
//        if (loanForAccount.remainingLoanAmount <= 0) {
//            loanForAccount.isLoanActive = false
//            println("🎉 Loan for account $accountNumber fully paid! Removing from records.")
//            loans.remove(accountNumber)
//            monthlyLoanPayments.remove(accountNumber)
//        }
//    }
//
//
//    fun addLoan(
//        loans: MutableMap<String, LoanInformation>,
//        accountNumber: String,
//        loanAmount: Double,
//        fixedMonthlyPayment: Double
//    ) {
//        //TODO: Add this check as a function to call everywhere
//        val account = accounts.find { it.accountNumber == accountNumber }
//        if (account == null) {
//            println("This account does not exist")
//            return
//        }
//        //TODO: add a status isLoanActive,
//        // if loan has been paid off user can apply for another
//        //Check if user has already an active loan
//        val existingLoan = loans[accountNumber]
//        if (existingLoan != null && existingLoan.isLoanActive) {
//            println("This account already has an active loan.")
//            return
//        }
//        val newLoan = LoanInformation(
//            accountNumber,
//            originalLoanAmount = loanAmount,
//            remainingLoanAmount = loanAmount,
//            fixedMonthlyPayment = fixedMonthlyPayment,
//            LocalDateTime.now()
//        )
//        transactions.add(
//            Transaction(
//                accountNumber, TransactionType.LOAN_APPLICATION, message = "Loan Repayment", amount = loanAmount
//            )
//        )
//        loans[accountNumber] = newLoan
//        account.balance += loanAmount
//        println("Loan of $loanAmount granted to account $accountNumber. New balance is ${account.balance}.")
//    }

}


