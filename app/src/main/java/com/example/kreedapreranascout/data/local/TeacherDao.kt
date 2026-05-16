package com.example.kreedapreranascout.data.local

import androidx.room.*
import com.example.kreedapreranascout.data.model.Teacher
import kotlinx.coroutines.flow.Flow

@Dao
interface TeacherDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertTeacher(teacher: Teacher): Long

    @Query("SELECT * FROM teachers WHERE email = :email LIMIT 1")
    suspend fun getTeacherByEmail(email: String): Teacher?

    @Query("SELECT * FROM teachers WHERE id = :id")
    fun getTeacherById(id: Long): Flow<Teacher?>

    @Update
    suspend fun updateTeacher(teacher: Teacher)
}
