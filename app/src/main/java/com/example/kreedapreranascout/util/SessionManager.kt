package com.example.kreedapreranascout.util

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_TEACHER_ID = "teacher_id"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }

    fun saveSession(teacherId: Long) {
        prefs.edit().apply {
            putLong(KEY_TEACHER_ID, teacherId)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }

    fun getTeacherId(): Long {
        return prefs.getLong(KEY_TEACHER_ID, -1L)
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun logout() {
        prefs.edit().clear().apply()
    }
}
