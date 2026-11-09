package com.jianwang.portfolio.spotjournal.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalDao {
    @Query("SELECT * FROM journal_entries ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<JournalEntry>>

    @Query("""
        SELECT * FROM journal_entries
        WHERE title LIKE '%' || :q || '%' OR note LIKE '%' || :q || '%' OR placeLabel LIKE '%' || :q || '%'
        ORDER BY createdAt DESC
    """)
    fun search(q: String): Flow<List<JournalEntry>>

    @Query("SELECT * FROM journal_entries WHERE id = :id LIMIT 1")
    suspend fun get(id: String): JournalEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: JournalEntry)

    @Delete
    suspend fun delete(entry: JournalEntry)
}

@Database(entities = [JournalEntry::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun journal(): JournalDao
    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun get(ctx: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(ctx, AppDatabase::class.java, "spot_journal.db").build().also { INSTANCE = it }
        }
    }
}
