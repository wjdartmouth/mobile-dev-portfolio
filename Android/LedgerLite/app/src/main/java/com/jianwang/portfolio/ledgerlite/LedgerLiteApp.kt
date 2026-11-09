package com.jianwang.portfolio.ledgerlite
import android.app.Application
import com.jianwang.portfolio.ledgerlite.data.AppDatabase
import com.jianwang.portfolio.ledgerlite.data.SeedData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LedgerLiteApp : Application() {
    lateinit var database: AppDatabase private set
    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.get(this)
        CoroutineScope(Dispatchers.IO).launch { SeedData.seedIfEmpty(database) }
    }
}
