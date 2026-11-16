package com.jianwang.portfolio.ledgerlite.data

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.jianwang.portfolio.ledgerlite.domain.LedgerCsv
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvExporter {
    fun share(context: Context, expenses: List<Expense>) {
        val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
        val csv = LedgerCsv.render(expenses.map { it.toLedgerLine() }) { millis ->
            fmt.format(Date(millis))
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
