package com.example.expensetracker

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "ExpenseTrackerSession"
        private const val KEY_IS_LOGGED_IN = "isLoggedIn"
        private const val KEY_USERNAME = "username"
        private const val KEY_FULL_NAME = "fullName"
        private const val KEY_EMAIL = "email"
    }

    // Save login session
    fun createLoginSession(username: String, fullName: String, email: String) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_USERNAME, username)
            putString(KEY_FULL_NAME, fullName)
            putString(KEY_EMAIL, email)
            apply()
        }
    }

    // Check if user is logged in
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    // Get user details
    fun getUserDetails(): HashMap<String, String?> {
        return hashMapOf(
            KEY_USERNAME to prefs.getString(KEY_USERNAME, null),
            KEY_FULL_NAME to prefs.getString(KEY_FULL_NAME, null),
            KEY_EMAIL to prefs.getString(KEY_EMAIL, null)
        )
    }

    // Logout user
    fun logout() {
        prefs.edit().clear().apply()
    }
}