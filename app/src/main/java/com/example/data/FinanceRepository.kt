package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class FinanceRepository(private val database: AppDatabase) {
    private val accountDao = database.accountDao()
    private val transactionDao = database.transactionDao()
    private val categoryDao = database.categoryDao()
    private val targetDao = database.targetDao()
    private val operationalCostDao = database.operationalCostDao()

    val allAccounts: Flow<List<Account>> = accountDao.getAllAccounts()
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()
    val allCategories: Flow<List<Category>> = categoryDao.getAllCategories()
    val allOperationalCosts: Flow<List<OperationalCost>> = operationalCostDao.getAllOperationalCosts()
    val allTargets: Flow<List<Target>> = targetDao.getAllTargets()

    suspend fun prePopulateIfNeeded() = withContext(Dispatchers.IO) {
        // Check and pre-populate Accounts
        val accounts = allAccounts.first()
        if (accounts.isEmpty()) {
            accountDao.insertAccount(Account(name = "Cash"))
            accountDao.insertAccount(Account(name = "GoPay"))
            accountDao.insertAccount(Account(name = "BCA"))
        }

        // Check and pre-populate Categories
        val categories = allCategories.first()
        if (categories.isEmpty()) {
            val defaultIncomes = listOf(
                "Pendapatan Gojek",
                "Bonus Gojek",
                "Penjualan",
                "Transfer Masuk",
                "Gaji",
                "Lainnya"
            )
            for (income in defaultIncomes) {
                categoryDao.insertCategory(Category(name = income, type = "Income"))
            }

            val defaultExpenses = listOf(
                "BBM",
                "Servis Motor",
                "Ganti Oli",
                "Ban",
                "Makan",
                "Belanja",
                "Tagihan",
                "Pulsa",
                "Internet",
                "Pendidikan",
                "Kesehatan",
                "Lainnya"
            )
            for (expense in defaultExpenses) {
                categoryDao.insertCategory(Category(name = expense, type = "Expense"))
            }
        }
    }

    // Accounts
    suspend fun insertAccount(account: Account) = withContext(Dispatchers.IO) {
        accountDao.insertAccount(account)
    }

    suspend fun updateAccount(account: Account) = withContext(Dispatchers.IO) {
        accountDao.updateAccount(account)
    }

    suspend fun deleteAccount(account: Account) = withContext(Dispatchers.IO) {
        accountDao.deleteAccount(account)
    }

    suspend fun getAccountById(id: Int): Account? = withContext(Dispatchers.IO) {
        accountDao.getAccountById(id)
    }

    // Transactions
    suspend fun insertTransaction(transaction: Transaction) = withContext(Dispatchers.IO) {
        transactionDao.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: Transaction) = withContext(Dispatchers.IO) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: Transaction) = withContext(Dispatchers.IO) {
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun getTransactionById(id: Int): Transaction? = withContext(Dispatchers.IO) {
        transactionDao.getTransactionById(id)
    }

    // Categories
    suspend fun insertCategory(category: Category) = withContext(Dispatchers.IO) {
        categoryDao.insertCategory(category)
    }

    suspend fun updateCategory(category: Category) = withContext(Dispatchers.IO) {
        categoryDao.updateCategory(category)
    }

    suspend fun deleteCategory(category: Category) = withContext(Dispatchers.IO) {
        categoryDao.deleteCategory(category)
    }

    // Targets (Gojek daily targets)
    suspend fun getTargetByDate(date: String): Target? = withContext(Dispatchers.IO) {
        targetDao.getTargetByDate(date)
    }

    fun getTargetFlowByDate(date: String): Flow<Target?> {
        return targetDao.getTargetFlowByDate(date)
    }

    suspend fun saveTarget(target: Target) = withContext(Dispatchers.IO) {
        val existing = targetDao.getTargetByDate(target.date)
        if (existing != null) {
            targetDao.updateTarget(target.copy(id = existing.id))
        } else {
            targetDao.insertTarget(target)
        }
    }

    // Operational Costs
    suspend fun insertOperationalCost(cost: OperationalCost) = withContext(Dispatchers.IO) {
        operationalCostDao.insertOperationalCost(cost)
    }

    suspend fun updateOperationalCost(cost: OperationalCost) = withContext(Dispatchers.IO) {
        operationalCostDao.updateOperationalCost(cost)
    }

    suspend fun deleteOperationalCost(cost: OperationalCost) = withContext(Dispatchers.IO) {
        operationalCostDao.deleteOperationalCost(cost)
    }
}
