package com.example.kreedapreranascout.util

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.kreedapreranascout.data.repository.StudentRepository
import com.example.kreedapreranascout.data.repository.UserRepository
import com.example.kreedapreranascout.ui.auth.AuthViewModel
import com.example.kreedapreranascout.ui.dashboard.DashboardViewModel
import com.example.kreedapreranascout.ui.student.StudentViewModel

class ViewModelFactory(private val repository: Any) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(repository as UserRepository) as T
        }
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
             return DashboardViewModel(repository as StudentRepository) as T
        }
        if (modelClass.isAssignableFrom(StudentViewModel::class.java)) {
             return StudentViewModel(repository as StudentRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
