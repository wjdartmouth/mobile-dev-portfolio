package com.jianwang.portfolio.syncnotes.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NoteConflictPolicyTest {
    @Test
    fun missingLocalLosesToRemote() {
        val remote = NoteVersion(revision = 1, updatedAt = 10, isDeleted = false)
        assertTrue(NoteConflictPolicy.shouldPreferRemote(local = null, remote = remote))
    }

    @Test
    fun higherRemoteRevisionWinsEvenIfOlder() {
        val local = NoteVersion(revision = 2, updatedAt = 200, isDeleted = false)
        val remote = NoteVersion(revision = 3, updatedAt = 100, isDeleted = false)
        assertTrue(NoteConflictPolicy.shouldPreferRemote(local, remote))
    }

    @Test
    fun lowerRemoteRevisionLosesEvenIfNewer() {
        val local = NoteVersion(revision = 4, updatedAt = 100, isDeleted = false)
        val remote = NoteVersion(revision = 3, updatedAt = 500, isDeleted = false)
        assertFalse(NoteConflictPolicy.shouldPreferRemote(local, remote))
    }

    @Test
    fun equalRevisionNewerRemoteWins() {
        val local = NoteVersion(revision = 2, updatedAt = 100, isDeleted = false)
        val remote = NoteVersion(revision = 2, updatedAt = 101, isDeleted = false)
        assertTrue(NoteConflictPolicy.shouldPreferRemote(local, remote))
    }

    @Test
    fun equalRevisionOlderRemoteLoses() {
        val local = NoteVersion(revision = 2, updatedAt = 200, isDeleted = false)
        val remote = NoteVersion(revision = 2, updatedAt = 100, isDeleted = false)
        assertFalse(NoteConflictPolicy.shouldPreferRemote(local, remote))
    }

    @Test
    fun equalRevisionAndTimestampKeepsLocal() {
        val local = NoteVersion(revision = 2, updatedAt = 100, isDeleted = false)
        val remote = NoteVersion(revision = 2, updatedAt = 100, isDeleted = true)
        assertFalse(NoteConflictPolicy.shouldPreferRemote(local, remote))
    }

    @Test
    fun deletedRemoteIsPreferredWhenRevisionWins() {
        val local = NoteVersion(revision = 2, updatedAt = 500, isDeleted = false)
        val remote = NoteVersion(revision = 5, updatedAt = 100, isDeleted = true)
        assertTrue(NoteConflictPolicy.shouldPreferRemote(local, remote))
    }
}
