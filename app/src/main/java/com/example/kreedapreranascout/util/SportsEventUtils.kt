package com.example.kreedapreranascout.util

import com.example.kreedapreranascout.data.model.Performance
import java.util.Locale

object SportsEventUtils {
    
    enum class EventType {
        SPRINT, JUMP, THROW, OTHER
    }

    fun getEventType(testType: String): EventType {
        val type = testType.lowercase()
        return when {
            type.contains("sprint") || type.contains("run") || type.contains("100m") || 
            type.contains("200m") || type.contains("400m") -> EventType.SPRINT
            type.contains("jump") -> EventType.JUMP
            type.contains("throw") || type.contains("put") -> EventType.THROW
            else -> EventType.OTHER
        }
    }

    fun isLowerBetter(testType: String): Boolean {
        return getEventType(testType) == EventType.SPRINT
    }

    fun getUnit(testType: String): String {
        return if (isLowerBetter(testType)) "seconds" else "meters"
    }

    fun formatValue(value: Double, testType: String): String {
        val unit = getUnit(testType)
        return String.format(Locale.US, "%.2f %s", value, unit)
    }

    fun getImprovementPercentage(first: Double, latest: Double, testType: String): Double {
        if (first == 0.0) return 0.0
        val lowerBetter = isLowerBetter(testType)
        val diff = if (lowerBetter) first - latest else latest - first
        return (diff / first) * 100
    }

    fun getInsightText(latest: Double, previous: Double, testType: String): String {
        val lowerBetter = isLowerBetter(testType)
        val improved = if (lowerBetter) latest < previous else latest > previous
        val difference = Math.abs(latest - previous)
        val formattedDiff = String.format(Locale.US, "%.2f", difference)
        val unit = getUnit(testType)
        
        return when {
            improved -> "Performance Improved by $formattedDiff $unit!"
            latest == previous -> "Performance maintained at $formattedDiff $unit."
            else -> "Needs more practice. Dropped by $formattedDiff $unit."
        }
    }

    fun getTrendStatus(performances: List<Performance>, testType: String): String {
        if (performances.size < 2) return "Performance data developing"
        
        val latest = performances.last().value
        val first = performances.first().value
        val secondLast = performances[performances.size - 2].value
        
        val lowerBetter = isLowerBetter(testType)
        val improvedRecent = if (lowerBetter) latest < secondLast else latest > secondLast
        val totalImproved = if (lowerBetter) latest < first else latest > first

        return when {
            improvedRecent && totalImproved -> "Performance improving steadily"
            !improvedRecent && totalImproved -> "Performance showing a slight plateau"
            improvedRecent && !totalImproved -> "Recent trials show a recovery trend"
            else -> "Performance currently declining"
        }
    }
}
