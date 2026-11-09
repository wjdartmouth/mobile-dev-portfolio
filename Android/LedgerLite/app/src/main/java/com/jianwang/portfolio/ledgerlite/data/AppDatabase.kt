package com.jianwang.portfolio.ledgerlite.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY spentAt DESC")
    fun observeAll(): Flow<List<Expense>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(expense: Expense)

    @Delete
    suspend fun delete(expense: Expense)

    @Query("SELECT COUNT(*) FROM expenses")
    suspend fun count(): Int
}

@Database(entities = [Expense::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expenses(): ExpenseDao
    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun get(ctx: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(ctx, AppDatabase::class.java, "ledger_lite.db").build().also { INSTANCE = it }
        }
    }
}
