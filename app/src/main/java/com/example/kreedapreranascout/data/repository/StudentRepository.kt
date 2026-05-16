package com.example.kreedapreranascout.data.repository

import com.example.kreedapreranascout.data.local.*
import com.example.kreedapreranascout.data.model.*
import kotlinx.coroutines.flow.Flow

class StudentRepository(
    private val studentDao: StudentDao,
    private val performanceDao: PerformanceDao,
    private val attendanceDao: AttendanceDao,
    private val achievementDao: AchievementDao
) {
    suspend fun addStudent(student: Student): Long = studentDao.insertStudent(student)
    suspend fun addStudents(students: List<Student>): List<Long> = studentDao.insertStudents(students)
    suspend fun updateStudent(student: Student) = studentDao.updateStudent(student)
    suspend fun deleteStudent(student: Student) = studentDao.deleteStudent(student)
    fun getAllStudents(teacherId: Long): Flow<List<Student>> = studentDao.getAllStudents(teacherId)
    fun getStudentById(id: Long): Flow<Student?> = studentDao.getStudentById(id)
    fun getStudentCount(teacherId: Long): Flow<Int> = studentDao.getStudentCount(teacherId)
    fun searchStudents(teacherId: Long, query: String): Flow<List<Student>> = studentDao.searchStudents(teacherId, query)
    suspend fun getAllRollNumbers(teacherId: Long): List<String> = studentDao.getAllRollNumbers(teacherId)

    suspend fun addPerformance(performance: Performance) = performanceDao.insertPerformance(performance)
    fun getPerformanceForStudent(studentId: Long): Flow<List<Performance>> = performanceDao.getPerformanceForStudent(studentId)
    fun getLatestPerformanceForStudent(studentId: Long): Flow<Performance?> = performanceDao.getLatestPerformanceForStudent(studentId)
    fun getLatestTwoPerformances(studentId: Long, testType: String): Flow<List<Performance>> = performanceDao.getLatestTwoPerformances(studentId, testType)
    fun getLastFivePerformances(studentId: Long, testType: String): Flow<List<Performance>> = performanceDao.getLastFivePerformances(studentId, testType)
    fun getLeaderboard(testType: String): Flow<List<LeaderboardResult>> = performanceDao.getLeaderboard(testType)
    fun getTopPerformer(teacherId: Long): Flow<TopPerformerResult?> = performanceDao.getTopPerformer(teacherId)

    suspend fun markAttendance(attendance: Attendance) = attendanceDao.insertAttendance(attendance)
    suspend fun markBatchAttendance(attendanceList: List<Attendance>) = attendanceDao.insertAll(attendanceList)
    fun getAttendanceForStudent(studentId: Long) = attendanceDao.getAttendanceForStudent(studentId)
    fun getAttendanceByDate(date: Long) = attendanceDao.getAttendanceByDate(date)

    suspend fun addAchievement(achievement: Achievement) = achievementDao.insertAchievement(achievement)
    fun getAchievementsForStudent(studentId: Long) = achievementDao.getAchievementsForStudent(studentId)
    suspend fun hasAchievement(studentId: Long, title: String, level: String) = achievementDao.hasAchievement(studentId, title, level)
}
