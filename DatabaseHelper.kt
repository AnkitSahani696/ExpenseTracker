package com.example.expensetracker

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.expensetracker.Expense

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "ExpenseTracker.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_EXPENSES = "expenses"

        private const val COLUMN_ID = "id"
        private const val COLUMN_AMOUNT = "amount"
        private const val COLUMN_CATEGORY = "category"
        private const val COLUMN_DATE = "date"
        private const val COLUMN_NOTE = "note"
        private const val COLUMN_PAYMENT_TYPE = "payment_type"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = """
            CREATE TABLE $TABLE_EXPENSES (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_AMOUNT REAL NOT NULL,
                $COLUMN_CATEGORY TEXT NOT NULL,
                $COLUMN_DATE TEXT NOT NULL,
                $COLUMN_NOTE TEXT,
                $COLUMN_PAYMENT_TYPE TEXT NOT NULL
            )
        """.trimIndent()
        db?.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_EXPENSES")
        onCreate(db)
    }

    // Add Expense
    fun addExpense(expense: Expense): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_AMOUNT, expense.amount)
            put(COLUMN_CATEGORY, expense.category)
            put(COLUMN_DATE, expense.date)
            put(COLUMN_NOTE, expense.note)
            put(COLUMN_PAYMENT_TYPE, expense.paymentType)
        }
        return db.insert(TABLE_EXPENSES, null, values)
    }

    // Update Expense
    fun updateExpense(expense: Expense): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_AMOUNT, expense.amount)
            put(COLUMN_CATEGORY, expense.category)
            put(COLUMN_DATE, expense.date)
            put(COLUMN_NOTE, expense.note)
            put(COLUMN_PAYMENT_TYPE, expense.paymentType)
        }
        return db.update(TABLE_EXPENSES, values, "$COLUMN_ID = ?", arrayOf(expense.id.toString()))
    }

    // Delete Expense
    fun deleteExpense(id: Long): Int {
        val db = writableDatabase
        return db.delete(TABLE_EXPENSES, "$COLUMN_ID = ?", arrayOf(id.toString()))
    }

    // Get All Expenses
    fun getAllExpenses(): List<Expense> {
        val expenses = mutableListOf<Expense>()
        val db = readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM $TABLE_EXPENSES ORDER BY $COLUMN_DATE DESC", null)

        if (cursor.moveToFirst()) {
            do {
                expenses.add(cursorToExpense(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return expenses
    }

    // Filter by Category
    fun getExpensesByCategory(category: String): List<Expense> {
        val expenses = mutableListOf<Expense>()
        val db = readableDatabase
        val cursor: Cursor = db.rawQuery(
            "SELECT * FROM $TABLE_EXPENSES WHERE $COLUMN_CATEGORY = ? ORDER BY $COLUMN_DATE DESC",
            arrayOf(category)
        )

        if (cursor.moveToFirst()) {
            do {
                expenses.add(cursorToExpense(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return expenses
    }

    // Filter by Date Range
    fun getExpensesByDateRange(startDate: String, endDate: String): List<Expense> {
        val expenses = mutableListOf<Expense>()
        val db = readableDatabase
        val cursor: Cursor = db.rawQuery(
            "SELECT * FROM $TABLE_EXPENSES WHERE $COLUMN_DATE BETWEEN ? AND ? ORDER BY $COLUMN_DATE DESC",
            arrayOf(startDate, endDate)
        )

        if (cursor.moveToFirst()) {
            do {
                expenses.add(cursorToExpense(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return expenses
    }

    // Get Monthly Summary
    fun getMonthlySummary(month: String): Map<String, Double> {
        val summary = mutableMapOf<String, Double>()
        val db = readableDatabase
        val cursor: Cursor = db.rawQuery(
            "SELECT $COLUMN_CATEGORY, SUM($COLUMN_AMOUNT) as total FROM $TABLE_EXPENSES WHERE $COLUMN_DATE LIKE ? GROUP BY $COLUMN_CATEGORY",
            arrayOf("$month%")
        )

        if (cursor.moveToFirst()) {
            do {
                val category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY))
                val total = cursor.getDouble(cursor.getColumnIndexOrThrow("total"))
                summary[category] = total
            } while (cursor.moveToNext())
        }
        cursor.close()
        return summary
    }

    private fun cursorToExpense(cursor: Cursor): Expense {
        return Expense(
            id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
            amount = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT)),
            category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)),
            date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
            note = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTE)) ?: "",
            paymentType = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PAYMENT_TYPE))
        )
    }
}