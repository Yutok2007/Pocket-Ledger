package com.pocketledger.app.model

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.WeekFields

data class TrendBucket(
    val label: String,
    val income: Double,
    val expense: Double,
)

data class StatisticsPeriod(
    val start: LocalDate,
    val endExclusive: LocalDate,
)

object LedgerAnalytics {
    fun categoryTotals(entries: List<LedgerEntry>, type: EntryType, currency: String): Map<String, Double> =
        entries.asSequence()
            .filter { it.type == type && it.currency == currency }
            .groupBy { it.category.ifBlank { "Uncategorized" } }
            .mapValues { (_, values) -> Money.fromMinor(values.sumOf { Money.toMinor(it.amount, it.currency) }, currency) }

    fun totals(entries: List<LedgerEntry>, currency: String): Triple<Double, Double, Double> {
        val matching = entries.filter { it.currency == currency }
        val incomeMinor = matching.filter { it.type == EntryType.INCOME }.sumOf { Money.toMinor(it.amount, currency) }
        val expenseMinor = matching.filter { it.type == EntryType.EXPENSE }.sumOf { Money.toMinor(it.amount, currency) }
        return Triple(Money.fromMinor(incomeMinor, currency), Money.fromMinor(expenseMinor, currency), Money.fromMinor(incomeMinor - expenseMinor, currency))
    }

    fun periodContaining(range: String, date: LocalDate): StatisticsPeriod {
        val start = when (range) {
            "week" -> date.minusDays((date.dayOfWeek.value - 1).toLong())
            "year" -> date.withDayOfYear(1)
            else -> date.withDayOfMonth(1)
        }
        val endExclusive = when (range) {
            "week" -> start.plusWeeks(1)
            "year" -> start.plusYears(1)
            else -> start.plusMonths(1)
        }
        return StatisticsPeriod(start, endExclusive)
    }

    fun availablePeriods(
        entries: List<LedgerEntry>,
        range: String,
        now: LocalDate = LocalDate.now(),
        zone: ZoneId = ZoneId.systemDefault(),
    ): List<StatisticsPeriod> {
        val recentCount = when (range) {
            "year" -> 5
            else -> 12
        }
        val starts = buildSet {
            repeat(recentCount) { offset ->
                val date = when (range) {
                    "week" -> now.minusWeeks(offset.toLong())
                    "year" -> now.minusYears(offset.toLong())
                    else -> now.minusMonths(offset.toLong())
                }
                add(periodContaining(range, date).start)
            }
            entries.forEach { entry ->
                val date = Instant.ofEpochMilli(entry.occurredAt).atZone(zone).toLocalDate()
                add(periodContaining(range, date).start)
            }
        }
        return starts.sorted().map { start -> periodContaining(range, start) }
    }

    fun entriesInPeriod(
        entries: List<LedgerEntry>,
        period: StatisticsPeriod,
        zone: ZoneId = ZoneId.systemDefault(),
    ): List<LedgerEntry> = entries.filter { entry ->
        val date = Instant.ofEpochMilli(entry.occurredAt).atZone(zone).toLocalDate()
        !date.isBefore(period.start) && date.isBefore(period.endExclusive)
    }

    fun trendBuckets(
        entries: List<LedgerEntry>,
        range: String,
        currency: String,
        now: LocalDate = LocalDate.now(),
        zone: ZoneId = ZoneId.systemDefault(),
    ): List<TrendBucket> {
        fun bucket(label: String, matches: (LocalDate) -> Boolean): TrendBucket {
            val values = entries.filter { entry ->
                entry.currency == currency && matches(Instant.ofEpochMilli(entry.occurredAt).atZone(zone).toLocalDate())
            }
            val (income, expense) = totals(values, currency)
            return TrendBucket(label, income, expense)
        }

        val selectedPeriod = periodContaining(range, now)
        return when (range) {
            "week" -> {
                val start = selectedPeriod.start
                (0..6).map { offset ->
                    val date = start.plusDays(offset.toLong())
                    bucket(date.dayOfWeek.name.take(1)) { it == date }
                }
            }
            "year" -> (1..12).map { month ->
                bucket(month.toString()) { it.year == selectedPeriod.start.year && it.monthValue == month }
            }
            else -> {
                val lastDate = selectedPeriod.endExclusive.minusDays(1)
                val lastWeekStart = lastDate.minusDays((lastDate.dayOfWeek.value - 1).toLong())
                (3 downTo 0).map { weeksAgo ->
                    val weekStart = lastWeekStart.minusWeeks(weeksAgo.toLong())
                    val weekEnd = weekStart.plusWeeks(1)
                    val weekNumber = weekStart.get(WeekFields.ISO.weekOfWeekBasedYear())
                    bucket("W$weekNumber") { date ->
                        !date.isBefore(selectedPeriod.start) &&
                            date.isBefore(selectedPeriod.endExclusive) &&
                            !date.isBefore(weekStart) &&
                            date.isBefore(weekEnd)
                    }
                }
            }
        }
    }
}
