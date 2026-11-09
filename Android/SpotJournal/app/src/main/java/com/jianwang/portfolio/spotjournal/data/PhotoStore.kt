package com.jianwang.portfolio.spotjournal.data

import android.content.Context
import android.net.Uri
import java.io.File
import java.util.UUID

class PhotoStore(private val context: Context) {
    private val dir: File by lazy {
        File(context.filesDir, "photos").also { it.mkdirs() }
    }

    fun createCaptureUri(): Pair<File, Uri> {
        val file = File(dir, "cap_${UUID.randomUUID()}.jpg")
        val uri = androidx.core.content.FileProvider.getUriForFile(
            context, "${context.packageName}.fileprovider", file
        )
        return file to uri
    }

    fun persistFromUri(uri: Uri): String {
        val out = File(dir, "img_${UUID.randomUUID()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            out.outputStream().use { output -> input.copyTo(output) }
        }
        return out.absolutePath
    }

    fun delete(path: String?) {
        if (path.isNullOrBlank()) return
        runCatching { File(path).delete() }
    }
}
