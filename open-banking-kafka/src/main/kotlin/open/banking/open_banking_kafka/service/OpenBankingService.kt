package open.banking.open_banking_kafka.service


import open.banking.open_banking_kafka.entity.*
import open.banking.open_banking_kafka.enums.TransactionTypeEnum
import open.banking.open_banking_kafka.repository.*
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
    val withdrawDailyLimitRepository: WithdrawDailyLimitRepository,
    val loanRepository: LoanRepository,
    val loanRepaymentRepository: LoanRepaymentRepository
) {
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
    fun addAccount(accountNumber: String): ResponseEntity<Any> {
        val existingAccount = accountRepository.findById(accountNumber).getOrNull()
        return if (existingAccount == null) {
            val newAccount = Account(accountNumber, 0.00)
            accountRepository.save(newAccount)
            ResponseEntity.status(HttpStatus.CREATED).body(newAccount)
        } else {
            ResponseEntity.status(HttpStatus.CONFLICT).body("Account with id $accountNumber exists.")
        }
    }

    // Check balance for an account
    fun checkBalance(accountId: String): ResponseEntity<Any> {
        val account = accountRepository.findById(accountId).orElse(null)
        return if (account == null) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body("Account ID not found")
        } else {
            ResponseEntity.ok(account.balance)
        }
    }

    // Show all transactions for an account
    fun showTransactions(accountId: String): ResponseEntity<Any> {
        val account = accountRepository.findById(accountId).orElse(null)
        val transactions = transactionRepository.findAllByAccountId(accountId)
        if (account == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Account Id not found")
        }
        return if (transactions.isNotEmpty()) {
            ResponseEntity.ok(transactions)
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body("No Transaction found for this account")
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

    //ADD loan
    fun addLoan(
        accountId: String, loanAmount: Double
    ): Loan {
        val account = accountRepository.findById(accountId).orElseThrow {
            IllegalArgumentException("Account $accountId not found")
        }

        //Check if user has already an active loan
        val existingLoan = loanRepository.findByAccountId(accountId).firstOrNull { it.isActive }
        if (existingLoan != null) {
            throw IllegalArgumentException("This account already has an active loan.")
        }

        val newLoan = Loan(
            loanId = UUID.randomUUID().toString(),
            accountId = accountId,
            principalAmount = loanAmount,
            outstandingBalance = loanAmount,
            interestRate = 5.0,
            termMonths = 12,
            lastPaymentDate = LocalDateTime.now(),
            isActive = true
        )

        val savedLoan = loanRepository.save(newLoan)

        val transaction = Transaction(
            transactionId = UUID.randomUUID().toString(),
            accountId = accountId,
            transactionType = TransactionTypeEnum.LOAN_APPLICATION,
            message = "Loan of $loanAmount granted",
            amount = loanAmount
        )
        transactionRepository.save(transaction)

        account.balance += loanAmount
        accountRepository.save(account)

        return savedLoan
    }


    //
    fun repayLoan(accountId: String): ResponseEntity<Any> {
        // Fetch account
        val account = accountRepository.findById(accountId).orElseThrow {
            IllegalArgumentException("❌ Account $accountId does not exist.")
        }

        // Fetch active loan
        val loanForAccount = loanRepository.findByAccountIdAndIsActive(accountId, true)
            ?: return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                mapOf("status" to 400, "message" to "No active loan for this account.")
            )

        if (!loanForAccount.isActive) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                mapOf("status" to 400, "message" to "❌ This loan has already been paid off.")
            )
        }

        // Get last repayment
        val lastRepayment =
            loanRepaymentRepository.findTopByLoanIdOrderByPaymentDateDesc(loanForAccount.loanId).firstOrNull()
        val currentMonth = LocalDateTime.now().monthValue

        if (lastRepayment != null && lastRepayment.paymentDate.monthValue == currentMonth) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                mapOf("status" to 400, "message" to "❌ Loan payment for this month has already been made.")
            )
        }

        // Calculate monthly payment
        val monthlyPayment = loanForAccount.calculateMonthlyPayment()

        // Check account balance
        if (account.balance < monthlyPayment) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                mapOf("status" to 400, "message" to "❌ Insufficient funds to pay the monthly installment.")
            )
        }

        // Deduct payment from account balance
        account.balance -= monthlyPayment
        accountRepository.save(account)

        // Deduct payment from loan balance
        loanForAccount.outstandingBalance -= monthlyPayment
        if (loanForAccount.outstandingBalance <= 0) {
            loanForAccount.isActive = false
        }
        loanRepository.save(loanForAccount)

        // Save repayment record
        val repayment = LoanRepayment(
            repaymentId = UUID.randomUUID().toString(),
            loanId = loanForAccount.loanId,
            accountId = accountId,
            amountPaid = monthlyPayment,
            paymentDate = LocalDateTime.now()
        )
        loanRepaymentRepository.save(repayment)

        // Save transaction
        val transaction = Transaction(
            transactionId = UUID.randomUUID().toString(),
            accountId = accountId,
            transactionType = TransactionTypeEnum.LOAN_REPAYMENT,
            message = "Monthly loan repayment of $monthlyPayment",
            amount = monthlyPayment
        )
        transactionRepository.save(transaction)

        return ResponseEntity.ok(
            mapOf(
                "status" to 200,
                "message" to "✅ Loan repayment successful.",
                "remainingBalance" to loanForAccount.outstandingBalance
            )
        )
    }

}


