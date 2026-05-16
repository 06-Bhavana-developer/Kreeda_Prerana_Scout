package com.example.kreedapreranascout.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.kreedapreranascout.data.repository.StudentRepository
import java.util.Calendar

class DashboardViewModel(private val repository: StudentRepository) : ViewModel() {
    
    fun getStudentCount(teacherId: Long) = repository.getStudentCount(teacherId).asLiveData()

    fun getTodayAttendanceCount() = repository.getAttendanceByDate(getTodayStartTimestamp()).asLiveData()
    
    fun getTopPerformer(teacherId: Long) = repository.getTopPerformer(teacherId).asLiveData()

    private fun getTodayStartTimestamp(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}
