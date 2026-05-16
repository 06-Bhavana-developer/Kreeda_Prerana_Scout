package com.example.kreedapreranascout.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.kreedapreranascout.data.model.Teacher
import com.example.kreedapreranascout.data.repository.UserRepository
import com.example.kreedapreranascout.util.PasswordHasher
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: UserRepository) : ViewModel() {
    private val _loginStatus = MutableLiveData<Result<Long>>()
    val loginStatus: LiveData<Result<Long>> = _loginStatus

    private val _registrationStatus = MutableLiveData<Result<Long>>()
    val registrationStatus: LiveData<Result<Long>> = _registrationStatus

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                val teacher = repository.getTeacherByEmail(email)
                if (teacher != null && PasswordHasher.checkPassword(password, teacher.passwordHash)) {
                    _loginStatus.value = Result.success(teacher.id)
                } else {
                    _loginStatus.value = Result.failure(Exception("Invalid email or password"))
                }
            } catch (e: Exception) {
                _loginStatus.value = Result.failure(e)
            }
        }
    }

    fun register(teacher: Teacher, password: String) {
        viewModelScope.launch {
            try {
                val existing = repository.getTeacherByEmail(teacher.email)
                if (existing != null) {
                    _registrationStatus.value = Result.failure(Exception("Email already registered"))
                    return@launch
                }
                
                val hashedTeacher = teacher.copy(passwordHash = PasswordHasher.hashPassword(password))
                val id = repository.registerTeacher(hashedTeacher)
                _registrationStatus.value = Result.success(id)
            } catch (e: Exception) {
                _registrationStatus.value = Result.failure(e)
            }
        }
    }

    fun getTeacherById(id: Long): LiveData<Teacher?> {
        return repository.getTeacherById(id).asLiveData()
    }

    private val _updateStatus = MutableLiveData<Result<Unit>>()
    val updateStatus: LiveData<Result<Unit>> = _updateStatus

    fun updateProfile(teacher: Teacher) {
        viewModelScope.launch {
            try {
                repository.updateTeacher(teacher)
                _updateStatus.value = Result.success(Unit)
            } catch (e: Exception) {
                _updateStatus.value = Result.failure(e)
            }
        }
    }
}
