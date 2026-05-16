package com.example.kreedapreranascout.data.local

import androidx.room.*
import com.example.kreedapreranascout.data.model.Student
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<Student>): List<Long>

    @Update
    suspend fun updateStudent(student: Student)

    @Delete
    suspend fun deleteStudent(student: Student)

    @Query("SELECT * FROM students WHERE createdByTeacherId = :teacherId ORDER BY fullName ASC")
    fun getAllStudents(teacherId: Long): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE id = :studentId")
    fun getStudentById(studentId: Long): Flow<Student?>

    @Query("SELECT COUNT(*) FROM students WHERE createdByTeacherId = :teacherId")
    fun getStudentCount(teacherId: Long): Flow<Int>

    @Query("SELECT * FROM students WHERE createdByTeacherId = :teacherId AND (fullName LIKE '%' || :query || '%' OR rollNumber LIKE '%' || :query || '%')")
    fun searchStudents(teacherId: Long, query: String): Flow<List<Student>>

    @Query("SELECT rollNumber FROM students WHERE createdByTeacherId = :teacherId")
    suspend fun getAllRollNumbers(teacherId: Long): List<String>
}
