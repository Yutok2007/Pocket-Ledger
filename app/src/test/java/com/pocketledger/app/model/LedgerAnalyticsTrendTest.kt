package com.pocketledger.app.model

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class LedgerAnalyticsTrendTest {
    private val zone = ZoneId.of("Asia/Shanghai")

    @Test
    fun monthChartUsesTheFourMostRecentIsoWeeks() {
        val entries = listOf(
            expenseOn(LocalDate.of(2026, 7, 5), 5.0),
            expenseOn(LocalDate.of(2026, 7, 6), 6.0),
            expenseOn(LocalDate.of(2026, 7, 13), 13.0),
            expenseOn(LocalDate.of(2026, 7, 20), 20.0),
            expenseOn(LocalDate.of(2026, 7, 31), 31.0),
        )

        val buckets = LedgerAnalytics.trendBuckets(entries, "month", "CNY", LocalDate.of(2026, 7, 15), zone)

        assertEquals(listOf("W28", "W29", "W30", "W31"), buckets.map { it.label })
        assertEquals(listOf(6.0, 13.0, 20.0, 31.0), buckets.map { it.expense })
    }

    @Test
    fun monthChartExcludesAdjacentMonthDaysFromOverlappingWeeks() {
        val entries = listOf(
            expenseOn(LocalDate.of(2026, 7, 31), 31.0),
            expenseOn(LocalDate.of(2026, 8, 1), 100.0),
        )

        val buckets = LedgerAnalytics.trendBuckets(entries, "month", "CNY", LocalDate.of(2026, 7, 15), zone)

        assertEquals(4, buckets.size)
        assertEquals(31.0, buckets.last().expense, 0.001)
    }

    @Test
    fun selectedWeekUsesMondayThroughSunday() {
        val period = LedgerAnalytics.periodContaining("week", LocalDate.of(2026, 7, 15))

        assertEquals(LocalDate.of(2026, 7, 13), period.start)
        assertEquals(LocalDate.of(2026, 7, 20), period.endExclusive)
    }

    @Test
    fun availableMonthRangesIncludeRecentMonthsAndMonthsWithTransactions() {
        val oldEntry = expenseOn(LocalDate.of(2024, 1, 20), 8.0)

        val periods = LedgerAnalytics.availablePeriods(listOf(oldEntry), "month", LocalDate.of(2026, 7, 15), zone)

        assertEquals(LocalDate.of(2024, 1, 1), periods.first().start)
        assertEquals(LocalDate.of(2026, 7, 1), periods.last().start)
        assertEquals(13, periods.size)
    }

    @Test
    fun entriesInPeriodIncludesStartAndExcludesNextPeriod() {
        val entries = listOf(
            expenseOn(LocalDate.of(2026, 7, 1), 1.0),
            expenseOn(LocalDate.of(2026, 7, 31), 2.0),
            expenseOn(LocalDate.of(2026, 8, 1), 3.0),
        )
        val july = LedgerAnalytics.periodContaining("month", LocalDate.of(2026, 7, 15))

        val selected = LedgerAnalytics.entriesInPeriod(entries, july, zone)

        assertEquals(listOf(1.0, 2.0), selected.map { it.amount })
    }

    @Test
    fun categoryTotalsChangeWithTheSelectedTransactionType() {
        val entries = listOf(
            expenseOn(LocalDate.of(2026, 7, 15), 12.0).copy(category = "Housing"),
            expenseOn(LocalDate.of(2026, 7, 15), 20.0).copy(type = EntryType.INCOME, category = "Salary"),
        )

        assertEquals(mapOf("Housing" to 12.0), LedgerAnalytics.categoryTotals(entries, EntryType.EXPENSE, "CNY"))
        assertEquals(mapOf("Salary" to 20.0), LedgerAnalytics.categoryTotals(entries, EntryType.INCOME, "CNY"))
    }

    private fun expenseOn(date: LocalDate, amount: Double) = LedgerEntry(
        type = EntryType.EXPENSE,
        amount = amount,
        currency = "CNY",
        occurredAt = ZonedDateTime.of(date, LocalTime.NOON, zone).toInstant().toEpochMilli(),
    )
}
