package com.jianwang.portfolio.spotjournal.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "journal_entries")
data class JournalEntry(
    @PrimaryKey val id: String,
    val title: String,
    val note: String,
    val photoPath: String?,
    val latitude: Double?,
    val longitude: Double?,
    val placeLabel: String?,
    val createdAt: Long,
    val updatedAt: Long
)
