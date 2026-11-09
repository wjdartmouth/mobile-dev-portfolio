package com.jianwang.portfolio.ledgerlite.data

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvExporter {
    fun share(context: Context, expenses: List<Expense>) {
        val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
        val csv = buildString {
            appendLine("id,title,amount,category,note,spent_at")
            expenses.forEach { e ->
                val amount = "%.2f".format(Locale.US, e.amountCents / 100.0)
                val title = e.title.replace(",", " ")
                val note = e.note.replace(",", " ")
                appendLine("${e.id},$title,$amount,${e.category},$note,${fmt.format(Date(e.spentAt))}")
            }
        }
        val dir = File(context.cacheDir, "exports").also { it.mkdirs() }
        val file = File(dir, "ledger_export.csv")
        file.writeText(csv)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Ledger Lite export")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share CSV"))
    }
}
