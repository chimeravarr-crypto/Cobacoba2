package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String
)

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "Income" or "Expense"
    val category: String,
    val amount: Double,
    val account_id: Int,
    val note: String?,
    val receipt_image: String?,
    val transaction_date: String, // "YYYY-MM-DD"
    val created_at: String
)

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val type: String // "Income" or "Expense"
)

@Entity(tableName = "targets")
data class Target(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val target_amount: Double,
    val date: String // "YYYY-MM-DD"
)

@Entity(tableName = "operational_costs")
data class OperationalCost(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val amount: Double,
    val is_monthly: Int // 1 for monthly, 0 otherwise
)
