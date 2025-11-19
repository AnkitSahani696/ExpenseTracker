package com.example.expensetracker

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.expensetracker.UserDatabaseHelper
import com.example.expensetracker.User

class RegisterActivity : AppCompatActivity() {

    private lateinit var fullNameEdit: EditText
    private lateinit var emailEdit: EditText
    private lateinit var usernameEdit: EditText
    private lateinit var passwordEdit: EditText
    private lateinit var confirmPasswordEdit: EditText
    private lateinit var registerButton: Button
    private lateinit var loginLink: TextView

    private lateinit var userDbHelper: UserDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        userDbHelper = UserDatabaseHelper(this)

        initViews()
        setupListeners()
    }

    private fun initViews() {
        fullNameEdit = findViewById(R.id.fullNameEdit)
        emailEdit = findViewById(R.id.emailEdit)
        usernameEdit = findViewById(R.id.usernameEdit)
        passwordEdit = findViewById(R.id.passwordEdit)
        confirmPasswordEdit = findViewById(R.id.confirmPasswordEdit)
        registerButton = findViewById(R.id.registerButton)
        loginLink = findViewById(R.id.loginLink)
    }

    private fun setupListeners() {
        registerButton.setOnClickListener {
            val fullName = fullNameEdit.text.toString().trim()
            val email = emailEdit.text.toString().trim()
            val username = usernameEdit.text.toString().trim()
            val password = passwordEdit.text.toString().trim()
            val confirmPassword = confirmPasswordEdit.text.toString().trim()

            if (validateInput(fullName, email, username, password, confirmPassword)) {
                registerUser(fullName, email, username, password)
            }
        }

        loginLink.setOnClickListener {
            finish()
        }
    }

    private fun validateInput(
        fullName: String,
        email: String,
        username: String,
        password: String,
        confirmPassword: String
    ): Boolean {

        if (fullName.isEmpty()) {
            fullNameEdit.error = "Full name required"
            fullNameEdit.requestFocus()
            return false
        }

        if (email.isEmpty()) {
            emailEdit.error = "Email required"
            emailEdit.requestFocus()
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEdit.error = "Enter valid email"
            emailEdit.requestFocus()
            return false
        }

        if (username.isEmpty()) {
            usernameEdit.error = "Username required"
            usernameEdit.requestFocus()
            return false
        }

        if (username.length < 4) {
            usernameEdit.error = "Username must be at least 4 characters"
            usernameEdit.requestFocus()
            return false
        }

        if (password.isEmpty()) {
            passwordEdit.error = "Password required"
            passwordEdit.requestFocus()
            return false
        }

        if (password.length < 6) {
            passwordEdit.error = "Password must be at least 6 characters"
            passwordEdit.requestFocus()
            return false
        }

        if (password != confirmPassword) {
            confirmPasswordEdit.error = "Passwords do not match"
            confirmPasswordEdit.requestFocus()
            return false
        }

        if (userDbHelper.isUsernameExists(username)) {
            usernameEdit.error = "Username already exists"
            usernameEdit.requestFocus()
            return false
        }

        if (userDbHelper.isEmailExists(email)) {
            emailEdit.error = "Email already registered"
            emailEdit.requestFocus()
            return false
        }

        return true
    }

    private fun registerUser(fullName: String, email: String, username: String, password: String) {
        val user = User(
            fullName = fullName,
            email = email,
            username = username,
            password = password
        )

        val isRegistered = userDbHelper.registerUser(user)

        if (isRegistered) {
            Toast.makeText(this, "Registration successful! Please login.", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show()
        }
    }
}