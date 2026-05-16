package com.example.kreedapreranascout.ui.student

import android.util.Log
import androidx.lifecycle.*
import com.example.kreedapreranascout.data.model.*
import com.example.kreedapreranascout.data.repository.StudentRepository
import com.example.kreedapreranascout.util.SportsEventUtils
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class StudentViewModel(private val repository: StudentRepository) : ViewModel() {
    private val _addStudentStatus = MutableLiveData<Result<Long>?>()
    val addStudentStatus: LiveData<Result<Long>?> = _addStudentStatus

    private val _batchAddStatus = MutableLiveData<Result<List<Long>>?>()
    val batchAddStatus: LiveData<Result<List<Long>>?> = _batchAddStatus

    private val _deleteStudentStatus = MutableLiveData<Result<Unit>>()
    val deleteStudentStatus: LiveData<Result<Unit>> = _deleteStudentStatus

    private val _addPerformanceStatus = MutableLiveData<Result<Long>>()
    val addPerformanceStatus: LiveData<Result<Long>> = _addPerformanceStatus

    private val _newBadgeEarned = MutableSharedFlow<Achievement>()
    val newBadgeEarned: SharedFlow<Achievement> = _newBadgeEarned

    fun addStudent(student: Student) {
        viewModelScope.launch {
            try {
                val id = repository.addStudent(student)
                _addStudentStatus.postValue(Result.success(id))
            } catch (e: Exception) {
                _addStudentStatus.postValue(Result.failure(e))
            }
        }
    }

    fun addBatchStudents(students: List<Student>) {
        viewModelScope.launch {
            try {
                val ids = repository.addStudents(students)
                _batchAddStatus.postValue(Result.success(ids))
            } catch (e: Exception) {
                _batchAddStatus.postValue(Result.failure(e))
            }
        }
    }

    fun clearBatchAddStatus() {
        _batchAddStatus.value = null
    }

    suspend fun getAllRollNumbers(teacherId: Long): List<String> {
        return repository.getAllRollNumbers(teacherId)
    }

    fun updateStudent(student: Student) {
        viewModelScope.launch {
            try {
                repository.updateStudent(student)
                _addStudentStatus.postValue(Result.success(student.id))
            } catch (e: Exception) {
                _addStudentStatus.postValue(Result.failure(e))
            }
        }
    }

    fun clearAddStudentStatus() {
        _addStudentStatus.value = null
    }

    fun deleteStudent(student: Student) {
        viewModelScope.launch {
            try {
                repository.deleteStudent(student)
                _deleteStudentStatus.value = Result.success(Unit)
            } catch (e: Exception) {
                _deleteStudentStatus.value = Result.failure(e)
            }
        }
    }

    fun addPerformance(performance: Performance) {
        viewModelScope.launch {
            try {
                repository.addPerformance(performance)
                _addPerformanceStatus.value = Result.success(0L) 
                evaluateMilestones(performance)
            } catch (e: Exception) {
                _addPerformanceStatus.value = Result.failure(e)
            }
        }
    }

    private suspend fun evaluateMilestones(performance: Performance) {
        val testType = performance.testType
        val value = performance.value
        val eventType = SportsEventUtils.getEventType(testType)
        
        val levelsToAward = mutableListOf<String>()

        when (eventType) {
            SportsEventUtils.EventType.SPRINT -> {
                if (value < 0.50) levelsToAward.add("National")
                if (value < 0.70) levelsToAward.add("State")
                if (value < 1.00) levelsToAward.add("District")
            }
            SportsEventUtils.EventType.JUMP -> {
                if (value > 7.0) levelsToAward.add("National")
                if (value > 5.0) levelsToAward.add("State")
                if (value > 3.0) levelsToAward.add("District")
            }
            SportsEventUtils.EventType.THROW -> {
                if (value > 30.0) levelsToAward.add("National")
                if (value > 20.0) levelsToAward.add("State")
                if (value > 10.0) levelsToAward.add("District")
            }
            else -> {}
        }

        levelsToAward.forEach { level ->
            awardMilestoneBadge(performance.studentId, level, testType)
        }
    }

    private suspend fun awardMilestoneBadge(studentId: Long, level: String, testType: String) {
        val title = when (level) {
            "District" -> "District Level Ready"
            "State" -> "State Level Ready"
            "National" -> "National Level Potential"
            else -> return
        }

        if (!repository.hasAchievement(studentId, title, level)) {
            val achievement = Achievement(
                studentId = studentId,
                title = title,
                level = level,
                date = System.currentTimeMillis(),
                description = "Achieved in $testType"
            )
            repository.addAchievement(achievement)
            _newBadgeEarned.emit(achievement)
        }
    }

    fun getAllStudents(teacherId: Long) = repository.getAllStudents(teacherId).asLiveData()
    fun getStudentById(id: Long) = repository.getStudentById(id).asLiveData()
    fun getPerformance(studentId: Long) = repository.getPerformanceForStudent(studentId).asLiveData()
    fun getLeaderboard(testType: String) = repository.getLeaderboard(testType).asLiveData()
    fun getLastFivePerformances(studentId: Long, testType: String) = repository.getLastFivePerformances(studentId, testType).asLiveData()
    fun getLatestPerformance(studentId: Long) = repository.getLatestPerformanceForStudent(studentId).asLiveData()
    fun getAchievements(studentId: Long) = repository.getAchievementsForStudent(studentId).asLiveData()

    fun getPerformanceInsight(studentId: Long, testType: String): LiveData<String> {
        return repository.getLatestTwoPerformances(studentId, testType).map { performances ->
            if (performances.size < 2) {
                "Keep practicing to see insights!"
            } else {
                SportsEventUtils.getInsightText(performances[0].value, performances[1].value, testType)
            }
        }.asLiveData()
    }
}
