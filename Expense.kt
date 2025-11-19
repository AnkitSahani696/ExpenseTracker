package com.example.expensetracker

import java.io.Serializable

data class Expense(
    val id: Long = 0,
    val amount: Double,
    val category: String,
    val date: String,
    val note: String,
    val paymentType: String
) : Serializable
