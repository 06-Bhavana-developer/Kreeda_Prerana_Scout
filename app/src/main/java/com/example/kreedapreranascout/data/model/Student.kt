package com.example.kreedapreranascout.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "students",
    foreignKeys = [
        ForeignKey(
            entity = Teacher::class,
            parentColumns = ["id"],
            childColumns = ["createdByTeacherId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["createdByTeacherId"])]
)
data class Student(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val age: Int,
    val gender: String,
    val classGrade: String,
    val semester: String? = null,
    val section: String,
    val rollNumber: String,
    val usn: String? = null,
    val college: String? = null,
    val primarySport: String,
    val secondarySport: String?,
    val height: Double, // in cm
    val weight: Double, // in kg
    val bmi: Double,
    val guardianName: String,
    val guardianContact: String,
    val address: String,
    val medicalNotes: String?,
    val profileImageUri: String? = null,
    val createdByTeacherId: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val isMilestoneAchieved: Boolean = false
)
