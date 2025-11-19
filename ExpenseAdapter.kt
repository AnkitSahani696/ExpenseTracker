package com.example.expensetracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker.R
import com.example.expensetracker.Expense

class ExpenseAdapter(
    private var expenses: List<Expense>,
    private val onEditClick: (Expense) -> Unit,
    private val onDeleteClick: (Expense) -> Unit
) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    class ExpenseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val categoryIcon: TextView = view.findViewById(R.id.categoryIcon)
        val categoryText: TextView = view.findViewById(R.id.categoryText)
        val amountText: TextView = view.findViewById(R.id.amountText)
        val dateText: TextView = view.findViewById(R.id.dateText)
        val noteText: TextView = view.findViewById(R.id.noteText)
        val paymentTypeText: TextView = view.findViewById(R.id.paymentTypeText)
        val editButton: ImageView = view.findViewById(R.id.editButton)
        val deleteButton: ImageView = view.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenses[position]

        holder.categoryIcon.text = getCategoryIcon(expense.category)
        holder.categoryText.text = expense.category
        holder.amountText.text = "₹${String.format("%.2f", expense.amount)}"
        holder.dateText.text = expense.date
        holder.noteText.text = expense.note
        holder.paymentTypeText.text = expense.paymentType

        holder.editButton.setOnClickListener { onEditClick(expense) }
        holder.deleteButton.setOnClickListener { onDeleteClick(expense) }
    }

    override fun getItemCount() = expenses.size

    fun updateExpenses(newExpenses: List<Expense>) {
        expenses = newExpenses
        notifyDataSetChanged()
    }

    private fun getCategoryIcon(category: String): String {
        return when (category) {
            "Food" -> "🍔"
            "Transport" -> "🚗"
            "Shopping" -> "🛒"
            "Entertainment" -> "🎬"
            "Bills" -> "📄"
            "Health" -> "⚕️"
            "Education" -> "📚"
            "Others" -> "📦"
            else -> "💰"
        }
    }
}