package com.example.kreedapreranascout.data.local

import androidx.room.*
import com.example.kreedapreranascout.data.model.Performance
import kotlinx.coroutines.flow.Flow

@Dao
interface PerformanceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPerformance(performance: Performance): Long

    @Query("SELECT value FROM performance_records WHERE studentId = :studentId AND testType = :testType ORDER BY date DESC LIMIT 5")
    fun getTalentCurveScores(studentId: Long, testType: String): Flow<List<Double>>

    /**
     * Fetches the top 10 leaderboard results for a specific test event.
     * It handles different sorting based on units and event type (Sprints use ASC, others use DESC).
     */
    @Query("""
        SELECT studentId, studentName, college, value, unit FROM (
            -- Sprints: Best is the Minimum value (Time)
            SELECT s.id as studentId, s.fullName as studentName, s.college, MIN(p.value) as value, p.unit, p.testType
            FROM performance_records p
            INNER JOIN students s ON p.studentId = s.id
            WHERE (p.unit IN ('s', 'sec', 'seconds') OR p.testType LIKE '%Sprint%') 
            AND LOWER(TRIM(p.testType)) = LOWER(TRIM(:testType))
            GROUP BY s.id
            
            UNION ALL
            
            -- Field Events: Best is the Maximum value (Distance/Weight)
            SELECT s.id as studentId, s.fullName as studentName, s.college, MAX(p.value) as value, p.unit, p.testType
            FROM performance_records p
            INNER JOIN students s ON p.studentId = s.id
            WHERE (p.unit NOT IN ('s', 'sec', 'seconds') AND p.testType NOT LIKE '%Sprint%') 
            AND LOWER(TRIM(p.testType)) = LOWER(TRIM(:testType))
            GROUP BY s.id
        ) 
        ORDER BY 
            CASE WHEN unit IN ('s', 'sec', 'seconds') OR testType LIKE '%Sprint%' THEN value END ASC,
            CASE WHEN unit NOT IN ('s', 'sec', 'seconds') AND testType NOT LIKE '%Sprint%' THEN value END DESC
        LIMIT 10
    """)
    fun getLeaderboard(testType: String): Flow<List<LeaderboardResult>>

    @Query("SELECT * FROM performance_records WHERE studentId = :studentId ORDER BY date DESC")
    fun getPerformanceForStudent(studentId: Long): Flow<List<Performance>>

    @Query("SELECT * FROM performance_records WHERE studentId = :studentId AND testType = :testType ORDER BY date DESC LIMIT 5")
    fun getLastFivePerformances(studentId: Long, testType: String): Flow<List<Performance>>

    @Query("SELECT * FROM performance_records WHERE studentId = :studentId ORDER BY date DESC LIMIT 1")
    fun getLatestPerformanceForStudent(studentId: Long): Flow<Performance?>

    @Query("SELECT * FROM performance_records WHERE studentId = :studentId AND testType = :testType ORDER BY date DESC LIMIT 2")
    fun getLatestTwoPerformances(studentId: Long, testType: String): Flow<List<Performance>>

    /**
     * Finds the 'Best' performance across all events for a specific teacher's athletes.
     * It considers the record holders of each event and returns the most recent record-breaking performance.
     */
    @Query("""
        SELECT studentName, testType, value, unit FROM (
            -- Records for Sprints (Time)
            SELECT s.fullName as studentName, p.testType, p.value, p.unit, p.date
            FROM performance_records p
            INNER JOIN students s ON p.studentId = s.id
            WHERE s.createdByTeacherId = :teacherId AND (p.unit IN ('s', 'sec', 'seconds') OR p.testType LIKE '%Sprint%')
            AND p.value = (
                SELECT MIN(v.value) 
                FROM performance_records v 
                INNER JOIN students st ON v.studentId = st.id 
                WHERE st.createdByTeacherId = :teacherId AND v.testType = p.testType
            )
            
            UNION ALL
            
            -- Records for Field Events (Distance/Weight)
            SELECT s.fullName as studentName, p.testType, p.value, p.unit, p.date
            FROM performance_records p
            INNER JOIN students s ON p.studentId = s.id
            WHERE s.createdByTeacherId = :teacherId AND (p.unit NOT IN ('s', 'sec', 'seconds') AND p.testType NOT LIKE '%Sprint%')
            AND p.value = (
                SELECT MAX(v.value) 
                FROM performance_records v 
                INNER JOIN students st ON v.studentId = st.id 
                WHERE st.createdByTeacherId = :teacherId AND v.testType = p.testType
            )
        ) ORDER BY date DESC LIMIT 1
    """)
    fun getTopPerformer(teacherId: Long): Flow<TopPerformerResult?>
}

data class LeaderboardResult(
    val studentId: Long,
    val studentName: String,
    val college: String?,
    val value: Double,
    val unit: String
)

data class TopPerformerResult(
    val studentName: String,
    val testType: String,
    val value: Double,
    val unit: String
)
