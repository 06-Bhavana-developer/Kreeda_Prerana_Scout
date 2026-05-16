package com.example.kreedapreranascout.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.kreedapreranascout.data.model.*

@Database(
    entities = [
        Teacher::class,
        Student::class,
        Performance::class,
        Attendance::class,
        Achievement::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun teacherDao(): TeacherDao
    abstract fun studentDao(): StudentDao
    abstract fun performanceDao(): PerformanceDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun achievementDao(): AchievementDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kreeda_prerana_db"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
