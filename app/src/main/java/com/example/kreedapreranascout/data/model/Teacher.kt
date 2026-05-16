package com.example.kreedapreranascout.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "teachers")
data class Teacher(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val email: String,
    val phone: String,
    val schoolName: String,
    val schoolAddress: String,
    val passwordHash: String,
    val profileImageUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
