package com.example.kreedapreranascout.data.repository

import com.example.kreedapreranascout.data.local.TeacherDao
import com.example.kreedapreranascout.data.model.Teacher
import kotlinx.coroutines.flow.Flow

class UserRepository(private val teacherDao: TeacherDao) {
    suspend fun registerTeacher(teacher: Teacher): Long {
        return teacherDao.insertTeacher(teacher)
    }

    suspend fun getTeacherByEmail(email: String): Teacher? {
        return teacherDao.getTeacherByEmail(email)
    }

    fun getTeacherById(id: Long): Flow<Teacher?> {
        return teacherDao.getTeacherById(id)
    }

    suspend fun updateTeacher(teacher: Teacher) {
        teacherDao.updateTeacher(teacher)
    }
}
