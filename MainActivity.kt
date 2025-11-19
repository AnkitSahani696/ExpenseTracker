package com.example.expensetracker

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker.ExpenseAdapter
import com.example.expensetracker.DatabaseHelper
import com.example.expensetracker.Expense
import com.example.expensetracker.SessionManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var expenseAdapter: ExpenseAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var fab: FloatingActionButton
    private lateinit var totalExpenseText: TextView
    private lateinit var filterSpinner: Spinner
    private lateinit var welcomeText: TextView
    private lateinit var sessionManager: SessionManager

    private val categories = arrayOf("All", "Food", "Transport", "Shopping", "Entertainment", "Bills", "Health", "Education", "Others")
    private val paymentTypes = arrayOf("Cash", "Card", "UPI", "Net Banking")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = DatabaseHelper(this)
        sessionManager = SessionManager(this)

        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        initViews()
        findViewById<Button>(R.id.logoutButton).setOnClickListener {
            showLogoutDialog()
        }


        displayUserInfo()
        setupRecyclerView()
        setupFilter()
        loadExpenses()
        updateTotalExpense()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                showLogoutDialog()
                true
            }
            R.id.action_profile -> {
                showProfileDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerView)
        fab = findViewById(R.id.fab)
        totalExpenseText = findViewById(R.id.totalExpenseText)
        filterSpinner = findViewById(R.id.filterSpinner)
        welcomeText = findViewById(R.id.welcomeText)

        fab.setOnClickListener { showAddExpenseDialog() }

        findViewById<Button>(R.id.exportButton).setOnClickListener { exportToCSV() }
        findViewById<Button>(R.id.summaryButton).setOnClickListener { showMonthlySummary() }
        findViewById<Button>(R.id.logoutButton).setOnClickListener { showLogoutDialog() }

    }


    private fun displayUserInfo() {
        val userDetails = sessionManager.getUserDetails()
        val fullName = userDetails["fullName"] ?: "User"
        welcomeText.text = "Hello, $fullName! 👋"
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                sessionManager.logout()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun showProfileDialog() {
        val userDetails = sessionManager.getUserDetails()
        val message = """
            Full Name: ${userDetails["fullName"]}
            Username: ${userDetails["username"]}
            Email: ${userDetails["email"]}
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("Profile")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun setupRecyclerView() {
        expenseAdapter = ExpenseAdapter(
            expenses = emptyList(),
            onEditClick = { expense -> showEditExpenseDialog(expense) },
            onDeleteClick = { expense -> deleteExpense(expense) }
        )

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = expenseAdapter
        }
    }

    private fun setupFilter() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        filterSpinner.adapter = adapter

        filterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                filterExpenses(categories[position])
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun loadExpenses() {
        val expenses = dbHelper.getAllExpenses()
        expenseAdapter.updateExpenses(expenses)
    }

    private fun filterExpenses(category: String) {
        val expenses = if (category == "All") {
            dbHelper.getAllExpenses()
        } else {
            dbHelper.getExpensesByCategory(category)
        }
        expenseAdapter.updateExpenses(expenses)
        updateTotalExpense(expenses)
    }

    private fun updateTotalExpense(expenses: List<Expense>? = null) {
        val expenseList = expenses ?: dbHelper.getAllExpenses()
        val total = expenseList.sumOf { it.amount }
        totalExpenseText.text = "Total: ₹${String.format("%.2f", total)}"
    }

    private fun showAddExpenseDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_expens, null)
        val dialog = AlertDialog.Builder(this)
            .setTitle("Add Expense")
            .setView(dialogView)
            .setPositiveButton("Add", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            setupExpenseDialog(dialogView, dialog, null)
        }
        dialog.show()
    }

    private fun showEditExpenseDialog(expense: Expense) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_expens, null)
        val dialog = AlertDialog.Builder(this)
            .setTitle("Edit Expense")
            .setView(dialogView)
            .setPositiveButton("Update", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            setupExpenseDialog(dialogView, dialog, expense)
        }
        dialog.show()
    }

    private fun setupExpenseDialog(dialogView: View, dialog: AlertDialog, expense: Expense?) {
        val amountEdit = dialogView.findViewById<EditText>(R.id.amountEdit)
        val categorySpinner = dialogView.findViewById<Spinner>(R.id.categorySpinner)
        val dateEdit = dialogView.findViewById<EditText>(R.id.dateEdit)
        val noteEdit = dialogView.findViewById<EditText>(R.id.noteEdit)
        val paymentSpinner = dialogView.findViewById<Spinner>(R.id.paymentSpinner)

        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories.drop(1))
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = categoryAdapter

        val paymentAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, paymentTypes)
        paymentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        paymentSpinner.adapter = paymentAdapter

        dateEdit.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                dateEdit.setText(String.format("%04d-%02d-%02d", year, month + 1, day))
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        expense?.let {
            amountEdit.setText(it.amount.toString())
            categorySpinner.setSelection(categories.indexOf(it.category) - 1)
            dateEdit.setText(it.date)
            noteEdit.setText(it.note)
            paymentSpinner.setSelection(paymentTypes.indexOf(it.paymentType))
        } ?: run {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            dateEdit.setText(today)
        }

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val amount = amountEdit.text.toString().toDoubleOrNull()
            val category = categorySpinner.selectedItem.toString()
            val date = dateEdit.text.toString()
            val note = noteEdit.text.toString()
            val paymentType = paymentSpinner.selectedItem.toString()

            if (amount == null || amount <= 0) {
                Toast.makeText(this, "Please enter valid amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (date.isEmpty()) {
                Toast.makeText(this, "Please select date", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newExpense = Expense(
                id = expense?.id ?: 0,
                amount = amount,
                category = category,
                date = date,
                note = note,
                paymentType = paymentType
            )

            if (expense == null) {
                dbHelper.addExpense(newExpense)
                Toast.makeText(this, "Expense added", Toast.LENGTH_SHORT).show()
            } else {
                dbHelper.updateExpense(newExpense)
                Toast.makeText(this, "Expense updated", Toast.LENGTH_SHORT).show()
            }

            loadExpenses()
            updateTotalExpense()
            dialog.dismiss()
        }
    }

    private fun deleteExpense(expense: Expense) {
        AlertDialog.Builder(this)
            .setTitle("Delete Expense")
            .setMessage("Are you sure you want to delete this expense?")
            .setPositiveButton("Delete") { _, _ ->
                dbHelper.deleteExpense(expense.id)
                loadExpenses()
                updateTotalExpense()
                Toast.makeText(this, "Expense deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun exportToCSV() {
        Toast.makeText(this, "CSV Export feature - Implement with file provider", Toast.LENGTH_LONG).show()
    }

    private fun showMonthlySummary() {
        val calendar = Calendar.getInstance()
        val month = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(calendar.time)
        val summary = dbHelper.getMonthlySummary(month)

        val summaryText = summary.entries.joinToString("\n") {
            "${it.key}: ₹${String.format("%.2f", it.value)}"
        }

        AlertDialog.Builder(this)
            .setTitle("Monthly Summary ($month)")
            .setMessage(summaryText.ifEmpty { "No expenses this month" })
            .setPositiveButton("OK", null)
            .show()
    }
}