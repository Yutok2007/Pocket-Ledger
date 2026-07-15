package com.pocketledger.app.data

import com.pocketledger.app.model.LedgerEntry
import java.time.Instant
import java.util.Locale

internal fun exportLedgerCsv(entries: List<LedgerEntry>): String = buildString {
    appendLine("id,type,amount,currency,purpose,category,account,date_time,note,original_text")
    entries.sortedByDescending { it.occurredAt }.forEach { entry ->
        val values = listOf(
            entry.id,
            entry.type.name,
            cleanCsvNumber(entry.amount),
            entry.currency,
            entry.purpose,
            entry.category,
            entry.accountName,
            Instant.ofEpochMilli(entry.occurredAt).toString(),
            entry.note,
            entry.rawText,
        )
        appendLine(values.joinToString(",", transform = ::escapeCsvCell))
    }
}

private fun escapeCsvCell(value: String): String {
    val firstMeaningful = value.dropWhile { it == ' ' || it == '\t' || it == '\r' || it == '\n' }.firstOrNull()
    val safe = if (firstMeaningful in setOf('=', '+', '-', '@')) "'$value" else value
    return "\"${safe.replace("\"", "\"\"")}\""
}

private fun cleanCsvNumber(value: Double): String =
    if (value % 1.0 == 0.0) value.toLong().toString() else "%.2f".format(Locale.ROOT, value)
