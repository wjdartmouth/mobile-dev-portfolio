package com.jianwang.portfolio.syncnotes.domain

data class NoteVersion(
    val revision: Int,
    val updatedAt: Long,
    val isDeleted: Boolean
)

object NoteConflictPolicy {
    /**
     * Last-writer-wins. A missing local note loses. Otherwise the higher
     * revision wins, then the later `updatedAt`. Equal timestamps keep local.
     *
     * `isDeleted` does not override that order. Callers apply a winning remote
     * even when it is a deletion.
     */
    fun shouldPreferRemote(local: NoteVersion?, remote: NoteVersion): Boolean {
        val current = local ?: return true
        if (remote.revision != current.revision) return remote.revision > current.revision
        return remote.updatedAt > current.updatedAt
    }
}
