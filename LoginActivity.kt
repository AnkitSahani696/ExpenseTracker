package com.example.expensetracker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.expensetracker.UserDatabaseHelper
import com.example.expensetracker.SessionManager // <-- use your app's SessionManager

class LoginActivity : AppCompatActivity() {

    private lateinit var usernameEdit: EditText
    private lateinit var passwordEdit: EditText
    private lateinit var loginButton: Button
    private lateinit var registerLink: TextView

    private lateinit var userDbHelper: UserDatabaseHelper
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        userDbHelper = UserDatabaseHelper(this)
        sessionManager = SessionManager(this)

        initViews()
        setupListeners()
    }

    private fun initViews() {
        usernameEdit = findViewById(R.id.usernameEdit)
        passwordEdit = findViewById(R.id.passwordEdit)
        loginButton = findViewById(R.id.loginButton)
        registerLink = findViewById(R.id.registerLink)
    }

    private fun setupListeners() {
        loginButton.setOnClickListener {
            val username = usernameEdit.text.toString().trim()
            val password = passwordEdit.text.toString().trim()

            if (validateInput(username, password)) {
                loginUser(username, password)
            }
        }

        registerLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun validateInput(username: String, password: String): Boolean {
        if (username.isEmpty()) {
            usernameEdit.error = "Username required"
            usernameEdit.requestFocus()
            return false
        }

        if (password.isEmpty()) {
            passwordEdit.error = "Password required"
            passwordEdit.requestFocus()
            return false
        }

        return true
    }

    private fun loginUser(username: String, password: String) {
        val user = userDbHelper.loginUser(username, password)

        if (user != null) {
            sessionManager.createLoginSession(user.username, user.fullName, user.email)
            Toast.makeText(this, "Welcome ${user.fullName}!", Toast.LENGTH_SHORT).show()

            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show()
        }
    }
}
