package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class TransactionFilterState(
    val filterType: String = "All",
    val categoryFilter: String = "All",
    val accountFilter: String = "All",
    val searchQuery: String = "",
    val sortType: String = "Terbaru"
)

class FinanceViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    val repository = FinanceRepository(database)

    // Raw sources from Room
    val accounts: StateFlow<List<Account>> = repository.allAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<Category>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<Transaction>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val operationalCosts: StateFlow<List<OperationalCost>> = repository.allOperationalCosts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val targets: StateFlow<List<com.example.data.Target>> = repository.allTargets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Unified Filter, Search, and Sort state
    private val _filterState = MutableStateFlow(TransactionFilterState())
    val filterState = _filterState.asStateFlow()

    // Separate backing getters for individual states required by screens
    val selectedFilterType = _filterState.map { it.filterType }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "All")
    val selectedCategoryFilter = _filterState.map { it.categoryFilter }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "All")
    val selectedAccountFilter = _filterState.map { it.accountFilter }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "All")
    val searchQuery = _filterState.map { it.searchQuery }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    val sortType = _filterState.map { it.sortType }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Terbaru")

    // Notification settings Saved in SharedPreferences
    private val sharedPrefs = application.getSharedPreferences("FinanceSettings", Context.MODE_PRIVATE)

    private val _notifEnabled = MutableStateFlow(sharedPrefs.getBoolean("notif_enabled", true))
    val notifEnabled = _notifEnabled.asStateFlow()

    private val _notifTime1 = MutableStateFlow(sharedPrefs.getString("notif_time_1", "21:00") ?: "21:00")
    val notifTime1 = _notifTime1.asStateFlow()

    private val _notifTime2 = MutableStateFlow(sharedPrefs.getString("notif_time_2", "23:00") ?: "23:00")
    val notifTime2 = _notifTime2.asStateFlow()

    // Target Gojek State (current setting target harian)
    private val _targetHarian = MutableStateFlow(sharedPrefs.getFloat("target_harian", 250000f).toDouble())
    val targetHarian = _targetHarian.asStateFlow()

    // Backup State Setup Mock/Control
    private val _isBackupEnabled = MutableStateFlow(sharedPrefs.getBoolean("auto_backup", false))
    val isBackupEnabled = _isBackupEnabled.asStateFlow()

    private val _isGoogleLoggedIn = MutableStateFlow(sharedPrefs.getBoolean("gdrive_logged_in", false))
    val isGoogleLoggedIn = _isGoogleLoggedIn.asStateFlow()

    private val _backupStatus = MutableStateFlow("Belum ada backup")
    val backupStatus = _backupStatus.asStateFlow()

    init {
        viewModelScope.launch {
            repository.prePopulateIfNeeded()
        }
    }

    // Account Balances calculated reactively based on transactions list
    val accountBalances: StateFlow<Map<Int, Double>> = transactions.map { txList ->
        val balances = mutableMapOf<Int, Double>()
        txList.forEach { tx ->
            val factor = if (tx.type == "Income") 1.0 else -1.0
            balances[tx.account_id] = (balances[tx.account_id] ?: 0.0) + (tx.amount * factor)
        }
        balances
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val totalBalance: StateFlow<Double> = accountBalances.map { balances ->
        balances.values.sum()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Current formatted month and year (e.g. "2026-06")
    private fun getCurrentMonth(): String {
        return SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
    }

    private fun getCurrentYear(): String {
        return SimpleDateFormat("yyyy", Locale.getDefault()).format(Date())
    }

    val statsThisMonth: StateFlow<MonthStats> = transactions.map { txList ->
        val currentMonth = getCurrentMonth()
        val mTransactions = txList.filter { it.transaction_date.startsWith(currentMonth) }
        val income = mTransactions.filter { it.type == "Income" }.sumOf { it.amount }
        val expense = mTransactions.filter { it.type == "Expense" }.sumOf { it.amount }
        MonthStats(income = income, expense = expense, net = income - expense)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MonthStats())

    // Gojek Targets calculation (Today's income categorised as "Pendapatan Gojek" or "Bonus Gojek" or contains Gojek)
    val todayGojekIncome: StateFlow<Double> = transactions.map { txList ->
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        txList.filter {
            it.transaction_date == todayStr &&
                    it.type == "Income" &&
                    (it.category == "Pendapatan Gojek" || it.category == "Bonus Gojek" || it.category.contains("Gojek", ignoreCase = true))
        }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Total Operational Costs monthly
    val totalMonthlyOperationalCost: StateFlow<Double> = operationalCosts.map { list ->
        list.filter { it.is_monthly == 1 }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Calculated daily burden: totalMonthlyOperational / daysInMonth
    val dailyOperationalBurden: StateFlow<Double> = totalMonthlyOperationalCost.map { total ->
        val calendar = Calendar.getInstance()
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        if (daysInMonth > 0) total / daysInMonth else 0.0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Filtered transaction flows
    val filteredTransactions: StateFlow<List<Transaction>> = combine(
        transactions,
        _filterState
    ) { txList, filter ->
        var result = txList

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = Date()
        val todayStr = sdf.format(today)

        // 1. Time Filters
        result = when (filter.filterType) {
            "Day" -> result.filter { it.transaction_date == todayStr }
            "Week" -> {
                val cal = Calendar.getInstance()
                cal.time = today
                cal.add(Calendar.DAY_OF_YEAR, -7)
                val oneWeekAgo = sdf.format(cal.time)
                result.filter { it.transaction_date >= oneWeekAgo && it.transaction_date <= todayStr }
            }
            "Month" -> {
                val currentMonth = getCurrentMonth()
                result.filter { it.transaction_date.startsWith(currentMonth) }
            }
            "Year" -> {
                val currentYear = getCurrentYear()
                result.filter { it.transaction_date.startsWith(currentYear) }
            }
            else -> result // All
        }

        // 2. Category Filter
        if (filter.categoryFilter != "All") {
            result = result.filter { it.category == filter.categoryFilter }
        }

        // 3. Account Filter
        if (filter.accountFilter != "All") {
            val accId = filter.accountFilter.toIntOrNull()
            if (accId != null) {
                result = result.filter { it.account_id == accId }
            }
        }

        // 4. Search Filter (Category or custom user note)
        if (filter.searchQuery.isNotBlank()) {
            result = result.filter {
                it.category.contains(filter.searchQuery, ignoreCase = true) ||
                        (it.note ?: "").contains(filter.searchQuery, ignoreCase = true)
            }
        }

        // 5. Sorting
        result = when (filter.sortType) {
            "Terlama" -> result.sortedWith(compareBy<Transaction> { it.transaction_date }.thenBy { it.created_at })
            "Nominal Terbesar" -> result.sortedByDescending { it.amount }
            else -> result.sortedWith(compareByDescending<Transaction> { it.transaction_date }.thenByDescending { it.created_at }) // Terbaru
        }

        result
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Actions
    fun setTimeFilter(type: String) {
        _filterState.value = _filterState.value.copy(filterType = type)
    }
    fun setCategoryFilter(category: String) {
        _filterState.value = _filterState.value.copy(categoryFilter = category)
    }
    fun setAccountFilter(accountId: String) {
        _filterState.value = _filterState.value.copy(accountFilter = accountId)
    }
    fun setSearchQuery(query: String) {
        _filterState.value = _filterState.value.copy(searchQuery = query)
    }
    fun setSortType(type: String) {
        _filterState.value = _filterState.value.copy(sortType = type)
    }

    fun addTransaction(type: String, category: String, amount: Double, accountId: Int, note: String, receiptImage: String? = null) {
        viewModelScope.launch {
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            val tx = Transaction(
                type = type,
                category = category,
                amount = amount,
                account_id = accountId,
                note = if (note.isBlank()) null else note,
                receipt_image = receiptImage,
                transaction_date = todayStr,
                created_at = timeStr
            )
            repository.insertTransaction(tx)
        }
    }

    fun addCustomTransactionWithDate(type: String, category: String, amount: Double, accountId: Int, note: String, date: String, receiptImage: String? = null) {
        viewModelScope.launch {
            val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            val tx = Transaction(
                type = type,
                category = category,
                amount = amount,
                account_id = accountId,
                note = if (note.isBlank()) null else note,
                receipt_image = receiptImage,
                transaction_date = date,
                created_at = timeStr
            )
            repository.insertTransaction(tx)
        }
    }

    fun updateTransaction(tx: Transaction) {
        viewModelScope.launch {
            repository.updateTransaction(tx)
        }
    }

    fun deleteTransaction(tx: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(tx)
        }
    }

    // Quick Action Shortcuts (Fast BBM Expense, Motor Service, Gojek Income)
    fun quickBBMExpense(amount: Double = 20000.0) {
        viewModelScope.launch {
            val accountsList = accounts.value
            val cashAcc = accountsList.find { it.name.equals("Cash", ignoreCase = true) } ?: accountsList.firstOrNull()
            val cashId = cashAcc?.id ?: 1

            addTransaction(
                type = "Expense",
                category = "BBM",
                amount = amount,
                accountId = cashId,
                note = "BBM Cepat"
            )
        }
    }

    fun quickGojekIncome(amount: Double = 150000.0) {
        viewModelScope.launch {
            val accountsList = accounts.value
            val gopayAcc = accountsList.find { it.name.equals("GoPay", ignoreCase = true) } ?: accountsList.firstOrNull()
            val gopayId = gopayAcc?.id ?: 2

            addTransaction(
                type = "Income",
                category = "Pendapatan Gojek",
                amount = amount,
                accountId = gopayId,
                note = "Pendapatan Gojek Cepat"
            )
        }
    }

    // Accounts management
    fun insertAccount(name: String) {
        viewModelScope.launch {
            repository.insertAccount(Account(name = name))
        }
    }

    fun updateAccount(account: Account) {
        viewModelScope.launch {
            repository.updateAccount(account)
        }
    }

    fun deleteAccount(account: Account) {
        viewModelScope.launch {
            repository.deleteAccount(account)
        }
    }

    // Category additions
    fun insertCategory(name: String, type: String) {
        viewModelScope.launch {
            repository.insertCategory(Category(name = name, type = type))
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }

    // Target change
    fun updateTargetHarian(amount: Double) {
        _targetHarian.value = amount
        sharedPrefs.edit().putFloat("target_harian", amount.toFloat()).apply()
    }

    // Operational costs
    fun insertOperationalCost(name: String, amount: Double, isMonthly: Boolean) {
        viewModelScope.launch {
            repository.insertOperationalCost(
                OperationalCost(name = name, amount = amount, is_monthly = if (isMonthly) 1 else 0)
            )
        }
    }

    fun deleteOperationalCost(cost: OperationalCost) {
        viewModelScope.launch {
            repository.deleteOperationalCost(cost)
        }
    }

    // Notifications control
    fun toggleNotifications(enabled: Boolean) {
        _notifEnabled.value = enabled
        sharedPrefs.edit().putBoolean("notif_enabled", enabled).apply()
    }

    fun updateNotificationTime1(time: String) {
        _notifTime1.value = time
        sharedPrefs.edit().putString("notif_time_1", time).apply()
    }

    fun updateNotificationTime2(time: String) {
        _notifTime2.value = time
        sharedPrefs.edit().putString("notif_time_2", time).apply()
    }

    // Mock Backup control
    fun toggleAutoBackup(enabled: Boolean) {
        _isBackupEnabled.value = enabled
        sharedPrefs.edit().putBoolean("auto_backup", enabled).apply()
        if (enabled) {
            _backupStatus.value = "Backup otomatis aktif harian"
        } else {
            _backupStatus.value = "Backup otomatis dinonaktifkan"
        }
    }

    fun loginGoogleDrive(loggedIn: Boolean) {
        _isGoogleLoggedIn.value = loggedIn
        sharedPrefs.edit().putBoolean("gdrive_logged_in", loggedIn).apply()
    }

    fun backupDatabaseNow() {
        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        _backupStatus.value = "Sukses dicadangkan ke Google Drive pukul $dateStr"
    }

    fun restoreDatabaseNow() {
        _backupStatus.value = "Sukses memulihkan database lokal dari cadangan Google Drive!"
    }
}

data class MonthStats(
    val income: Double = 0.0,
    val expense: Double = 0.0,
    val net: Double = 0.0
)
