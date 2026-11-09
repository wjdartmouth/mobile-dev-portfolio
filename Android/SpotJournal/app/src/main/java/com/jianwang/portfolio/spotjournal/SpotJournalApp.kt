package com.jianwang.portfolio.spotjournal

import android.app.Application
import com.jianwang.portfolio.spotjournal.data.AppDatabase

class SpotJournalApp : Application() {
    lateinit var database: AppDatabase
        private set
    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.get(this)
    }
}
