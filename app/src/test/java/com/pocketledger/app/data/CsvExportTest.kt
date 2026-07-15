package com.pocketledger.app.data

import com.pocketledger.app.model.LedgerEntry
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CsvExportTest {
    @Test
    fun untrustedSpreadsheetFormulasAreNeutralizedAndQuotesAreEscaped() {
        val csv = exportLedgerCsv(
            listOf(
                LedgerEntry(
                    id = "entry",
                    amount = 12.0,
                    purpose = "=HYPERLINK(\"https://example.invalid\")",
                    category = "+SUM(A1:A2)",
                    accountName = " @command",
                    note = "-2+3",
                    rawText = "normal \"quoted\" text",
                    occurredAt = 0,
                )
            )
        )

        assertTrue(csv.contains("\"'=HYPERLINK(\"\"https://example.invalid\"\")\""))
        assertTrue(csv.contains("\"'+SUM(A1:A2)\""))
        assertTrue(csv.contains("\"' @command\""))
        assertTrue(csv.contains("\"'-2+3\""))
        assertTrue(csv.contains("\"normal \"\"quoted\"\" text\""))
        assertFalse(csv.contains(",\"=HYPERLINK"))
    }
}
