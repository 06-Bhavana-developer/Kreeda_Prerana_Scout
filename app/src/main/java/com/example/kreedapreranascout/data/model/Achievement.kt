package com.example.kreedapreranascout.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "achievements",
    foreignKeys = [
        ForeignKey(
            entity = Student::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["studentId"])]
)
data class Achievement(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val title: String,
    val level: String, // School, District, State, National
    val date: Long,
    val description: String?
)
