package open.banking.open_banking_kafka.service


import open.banking.open_banking_kafka.entity.*
import open.banking.open_banking_kafka.repository.AccountRepository
import open.banking.open_banking_kafka.repository.TransactionRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrNull

@Service
class OpenBankingService(val accountRepository: AccountRepository, val transactionRepository: TransactionRepository) {
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

//    // Transfer money between accounts
//    fun transferMoney(from: String, to: String, amount: Double, message: String) {
//        val fromAccount = accounts.find { it.accountNumber == from }
//        val toAccount = accounts.find { it.accountNumber == to }
//
//        if (fromAccount == null || toAccount == null) {
//            println("One of the accounts does not exist!")
//            return
//        }
//        if (fromAccount.balance < amount) {
//            println("Insufficient funds!")
//            return
//        }
//
//        fromAccount.balance -= amount
//        toAccount.balance += amount
//        transactions.add(Transaction(accountNumber = from, TransactionType.TRANSFER, message, amount))
//        println("Transfer successful: $amount transferred from $from to $to")
//    }
//
//    //Deposit money to an account
//    fun depositMoney(accountNumber: String, amount: Double) {
//        val accountToDeposit = accounts.find { it.accountNumber == accountNumber }
//        if (accountToDeposit == null) {
//            println("This account does not exist")
//            return
//        }
//        if (amount <= 0) {
//            println("Add a number greater than 0")
//            return
//        }
//        accountToDeposit.balance += amount
//        transactions.add(Transaction(accountNumber, TransactionType.DEPOSIT, message = "DEPOSIT", amount))
//        println("New balance for account $accountNumber is ${accountToDeposit.balance}")
//    }
//
//    //Withdraw money
//    fun withdrawMoney(withdrawnLimit: MutableList<DailyLimit>, accountNumber: String, amount: Double) {
//        val accountToWithdraw = accounts.find { it.accountNumber == accountNumber }
//        if (accountToWithdraw == null) {
//            println("This account does not exist")
//            return
//        }
//        if (amount > accountToWithdraw.balance) {
//            println("You do not have kaq shum lek")
//            return
//        }
//        if (amount > DAILY_LIMIT) {
//            println("You passed the limit")
//            return
//        }
//        checkDailyLimit(withdrawnLimit, accountNumber, amount, accountToWithdraw)
//        transactions.add(Transaction(accountNumber, TransactionType.WITHDRAWAL, message = "Withdraw", amount))
//
//        println("New balance for account $accountNumber is ${accountToWithdraw.balance}")
//    }
//
//    private fun checkDailyLimit(
//        withdrawnLimit: MutableList<DailyLimit>, accountNumber: String, amount: Double, currentAccount: Account
//    ) {
//        val withdrawAccount = withdrawnLimit.firstOrNull { it.accountId == accountNumber }
//        if (withdrawAccount == null) {
//            withdrawnLimit.add(DailyLimit(accountNumber, amount, LocalDateTime.now()))
//        } else {
//            //TODO: fix date check (chrono calendar)
//            if (withdrawAccount.withdrawnAmount + amount > DAILY_LIMIT && withdrawAccount.date.dayOfMonth == LocalDateTime.now().dayOfMonth) {
//                println("You have passed the limit")
//                return
//            } else {
//                withdrawAccount.withdrawnAmount += amount
//                withdrawAccount.date = LocalDateTime.now()
//                currentAccount.balance -= amount
//            }
//        }
//    }
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


