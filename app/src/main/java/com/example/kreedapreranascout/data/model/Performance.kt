package com.example.kreedapreranascout.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "performance_records",
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
data class Performance(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val testType: String,
    val value: Double,
    val unit: String,
    val attemptNumber: Int,
    val date: Long,
    val remarks: String?
)
