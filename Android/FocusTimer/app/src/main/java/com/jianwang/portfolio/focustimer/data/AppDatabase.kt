package com.jianwang.portfolio.focustimer.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusSessionDao {
    @Query("SELECT * FROM focus_sessions ORDER BY completedAt DESC")
    fun observeAll(): Flow<List<FocusSession>>

    @Query("SELECT * FROM focus_sessions WHERE completedAt >= :since ORDER BY completedAt ASC")
    fun observeSince(since: Long): Flow<List<FocusSession>>

    @Query("SELECT COUNT(*) FROM focus_sessions WHERE phase = 'FOCUS' AND wasCompleted = 1 AND completedAt >= :startOfDay")
    fun observeFocusCountToday(startOfDay: Long): Flow<Int>

    @Insert
    suspend fun insert(session: FocusSession): Long

    @Query("DELETE FROM focus_sessions")
    suspend fun clearAll()
}

@Database(entities = [FocusSession::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sessions(): FocusSessionDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "focus_timer.db"
                ).build().also { INSTANCE = it }
            }
    }
}
