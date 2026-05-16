package com.example.kreedapreranascout.data.local

import androidx.room.*
import com.example.kreedapreranascout.data.model.Attendance
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: Attendance)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(attendanceList: List<Attendance>)

    @Query("SELECT * FROM attendance_records WHERE studentId = :studentId ORDER BY date DESC")
    fun getAttendanceForStudent(studentId: Long): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance_records WHERE date = :date")
    fun getAttendanceByDate(date: Long): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance_records WHERE date >= :dateStart AND date <= :dateEnd")
    fun getAttendanceByDateRange(dateStart: Long, dateEnd: Long): Flow<List<Attendance>>

    @Query("SELECT COUNT(*) FROM attendance_records WHERE studentId = :studentId AND status = 'Present'")
    fun getPresentCountForStudent(studentId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM attendance_records WHERE studentId = :studentId")
    fun getTotalDaysForStudent(studentId: Long): Flow<Int>
}
