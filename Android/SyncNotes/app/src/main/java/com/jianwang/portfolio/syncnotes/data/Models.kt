package com.jianwang.portfolio.syncnotes.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val id: String,
    val title: String,
    val body: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isPinned: Boolean,
    val revision: Int,
    val isDeleted: Boolean,
    val remoteId: String?
)
