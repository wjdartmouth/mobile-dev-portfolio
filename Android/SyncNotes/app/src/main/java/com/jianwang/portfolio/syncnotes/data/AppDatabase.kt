package com.jianwang.portfolio.syncnotes.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE isDeleted = 0 ORDER BY isPinned DESC, updatedAt DESC")
    fun observeActive(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    suspend fun get(id: String): NoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(note: NoteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(notes: List<NoteEntity>)

    @Query("SELECT * FROM notes")
    suspend fun all(): List<NoteEntity>
}

@Database(entities = [NoteEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun notes(): NoteDao
    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun get(ctx: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(ctx, AppDatabase::class.java, "sync_notes.db").build().also { INSTANCE = it }
        }
    }
}
