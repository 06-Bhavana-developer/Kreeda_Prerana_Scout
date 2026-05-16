package com.example.kreedapreranascout.data.model

data class LeaderboardEntry(
    val studentName: String,
    val college: String?,
    val value: Double,
    val unit: String,
    val rank: Int = 0
)
