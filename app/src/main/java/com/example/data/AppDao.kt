package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts")
    fun getAllAccounts(): Flow<List<Account>>

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getAccountById(id: Int): Account?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: Account): Long

    @Update
    suspend fun updateAccount(account: Account)

    @Delete
    suspend fun deleteAccount(account: Account)
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY transaction_date DESC, created_at DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Int): Transaction?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction): Long

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories")
    fun getAllCategories(): Flow<List<Category>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category): Long

    @Update
    suspend fun updateCategory(category: Category)

    @Delete
    suspend fun deleteCategory(category: Category)
}

@Dao
interface TargetDao {
    @Query("SELECT * FROM targets")
    fun getAllTargets(): Flow<List<Target>>

    @Query("SELECT * FROM targets WHERE date = :date LIMIT 1")
    suspend fun getTargetByDate(date: String): Target?

    @Query("SELECT * FROM targets WHERE date = :date LIMIT 1")
    fun getTargetFlowByDate(date: String): Flow<Target?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTarget(target: Target): Long

    @Update
    suspend fun updateTarget(target: Target)
}

@Dao
interface OperationalCostDao {
    @Query("SELECT * FROM operational_costs")
    fun getAllOperationalCosts(): Flow<List<OperationalCost>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOperationalCost(cost: OperationalCost): Long

    @Update
    suspend fun updateOperationalCost(cost: OperationalCost)

    @Delete
    suspend fun deleteOperationalCost(cost: OperationalCost)
}
