@file:OptIn(ExperimentalMaterial3Api::class)

package com.pocketledger.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pocketledger.app.i18n.categoryLabel
import com.pocketledger.app.i18n.languages
import com.pocketledger.app.i18n.tr
import com.pocketledger.app.data.BackupCrypto
import com.pocketledger.app.data.exportLedgerCsv
import com.pocketledger.app.model.*
import com.pocketledger.app.viewmodel.LedgerUiState
import com.pocketledger.app.viewmodel.LedgerViewModel
import java.text.NumberFormat
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.WeekFields
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToInt

private val LightColors = lightColorScheme(
    primary = Color(0xFF087A58), onPrimary = Color.White, primaryContainer = Color(0xFFD7F3E6), onPrimaryContainer = Color(0xFF0B3B2D),
    secondary = Color(0xFF526A60), onSecondary = Color.White, secondaryContainer = Color(0xFFE0EEE7), onSecondaryContainer = Color(0xFF243A31),
    tertiary = Color(0xFF456B78), background = Color(0xFFF5F7F5), surface = Color(0xFFFCFDFC),
    surfaceVariant = Color(0xFFE7ECE8), outline = Color(0xFF74827B), outlineVariant = Color(0xFFD0D8D3), error = Color(0xFFBA1A1A)
)
private val DarkColors = darkColorScheme(
    primary = Color(0xFF72DBAF), onPrimary = Color(0xFF003827), primaryContainer = Color(0xFF174C3B), onPrimaryContainer = Color(0xFFC8F6E1),
    secondary = Color(0xFFB4CCBF), onSecondary = Color(0xFF20352C), secondaryContainer = Color(0xFF30473D), onSecondaryContainer = Color(0xFFD0E8DB),
    tertiary = Color(0xFFA7CDDA), background = Color(0xFF101412), surface = Color(0xFF151A17),
    surfaceVariant = Color(0xFF29312D), outline = Color(0xFF8A9991), outlineVariant = Color(0xFF3E4943), error = Color(0xFFFFB4AB)
)

private val LedgerCardShape = RoundedCornerShape(24.dp)
private val LedgerHeaderShape = RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp)
private val LedgerShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(30.dp),
)

private enum class AppGlyph { HOME, CHART, REPORT, PROFILE, SEARCH, CALENDAR, MENU, ADD }

@Composable
private fun LedgerIcon(glyph: AppGlyph, modifier: Modifier = Modifier, color: Color = LocalContentColor.current) {
    Canvas(modifier.size(24.dp)) {
        val strokeWidth = size.minDimension * .075f
        val stroke = Stroke(strokeWidth, cap = StrokeCap.Round)
        when (glyph) {
            AppGlyph.HOME -> {
                val path = Path().apply {
                    moveTo(size.width * .14f, size.height * .47f)
                    lineTo(size.width * .5f, size.height * .17f)
                    lineTo(size.width * .86f, size.height * .47f)
                    moveTo(size.width * .22f, size.height * .42f)
                    lineTo(size.width * .22f, size.height * .84f)
                    lineTo(size.width * .78f, size.height * .84f)
                    lineTo(size.width * .78f, size.height * .42f)
                }
                drawPath(path, color, style = stroke)
            }
            AppGlyph.CHART -> {
                drawArc(color, -90f, 272f, false, topLeft = Offset(size.width * .12f, size.height * .12f), size = Size(size.width * .76f, size.height * .76f), style = stroke)
                drawLine(color, Offset(size.width * .5f, size.height * .5f), Offset(size.width * .5f, size.height * .12f), strokeWidth, StrokeCap.Round)
                drawLine(color, Offset(size.width * .5f, size.height * .5f), Offset(size.width * .86f, size.height * .5f), strokeWidth, StrokeCap.Round)
            }
            AppGlyph.REPORT -> {
                listOf(.27f, .5f, .73f).forEach { y ->
                    drawCircle(color, radius = strokeWidth * .75f, center = Offset(size.width * .16f, size.height * y))
                    drawLine(color, Offset(size.width * .3f, size.height * y), Offset(size.width * .84f, size.height * y), strokeWidth, StrokeCap.Round)
                }
            }
            AppGlyph.PROFILE -> {
                drawCircle(color, radius = size.minDimension * .16f, center = Offset(size.width * .5f, size.height * .31f), style = stroke)
                drawArc(color, 195f, 150f, false, topLeft = Offset(size.width * .18f, size.height * .48f), size = Size(size.width * .64f, size.height * .42f), style = stroke)
            }
            AppGlyph.SEARCH -> {
                drawCircle(color, radius = size.minDimension * .25f, center = Offset(size.width * .41f, size.height * .41f), style = stroke)
                drawLine(color, Offset(size.width * .59f, size.height * .59f), Offset(size.width * .84f, size.height * .84f), strokeWidth, StrokeCap.Round)
            }
            AppGlyph.CALENDAR -> {
                drawRoundRect(color, topLeft = Offset(size.width * .12f, size.height * .2f), size = Size(size.width * .76f, size.height * .68f), cornerRadius = CornerRadius(size.width * .1f), style = stroke)
                drawLine(color, Offset(size.width * .12f, size.height * .4f), Offset(size.width * .88f, size.height * .4f), strokeWidth)
                drawLine(color, Offset(size.width * .32f, size.height * .12f), Offset(size.width * .32f, size.height * .28f), strokeWidth, StrokeCap.Round)
                drawLine(color, Offset(size.width * .68f, size.height * .12f), Offset(size.width * .68f, size.height * .28f), strokeWidth, StrokeCap.Round)
            }
            AppGlyph.MENU -> {
                listOf(.28f, .5f, .72f).forEachIndexed { index, y ->
                    val end = if (index == 1) .72f else .84f
                    drawLine(color, Offset(size.width * .16f, size.height * y), Offset(size.width * end, size.height * y), strokeWidth, StrokeCap.Round)
                }
            }
            AppGlyph.ADD -> {
                drawLine(color, Offset(size.width * .5f, size.height * .18f), Offset(size.width * .5f, size.height * .82f), strokeWidth, StrokeCap.Round)
                drawLine(color, Offset(size.width * .18f, size.height * .5f), Offset(size.width * .82f, size.height * .5f), strokeWidth, StrokeCap.Round)
            }
        }
    }
}

@Composable
private fun ModernHeader(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val colors = listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.secondaryContainer)
    Box(
        modifier.fillMaxWidth().clip(LedgerHeaderShape).background(Brush.linearGradient(colors)),
        content = content,
    )
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { LedgerRoot() }
    }
}

private enum class MainPage { HOME, CHART, REPORT, SETTINGS }

@Composable
private fun LedgerRoot(viewModel: LedgerViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val settings = state.data.settings
    val systemDark = isSystemInDarkTheme()
    val dark = settings.theme == ThemeMode.DARK || (settings.theme == ThemeMode.SYSTEM && systemDark)
    val baseDensity = LocalDensity.current
    val direction = if (settings.language == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr
    CompositionLocalProvider(
        LocalDensity provides Density(baseDensity.density, baseDensity.fontScale * settings.textSize.scale),
        androidx.compose.ui.platform.LocalLayoutDirection provides direction
    ) {
        MaterialTheme(colorScheme = if (dark) DarkColors else LightColors, shapes = LedgerShapes) {
            var page by rememberSaveable { mutableStateOf(MainPage.HOME) }
            var adding by rememberSaveable { mutableStateOf(false) }
            var editing by remember { mutableStateOf<LedgerEntry?>(null) }
            Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                if (adding || editing != null) {
                    AddEntryScreen(viewModel, editing, onClose = { adding = false; editing = null })
                } else {
                    Scaffold(
                        containerColor = MaterialTheme.colorScheme.background,
                        bottomBar = {
                            MainNavigation(page, settings.language, onPage = { page = it }, onAdd = { adding = true })
                        }
                    ) { padding ->
                        Box(Modifier.padding(padding).fillMaxSize()) {
                            when (page) {
                                MainPage.HOME -> HomeScreen(viewModel, state, onEdit = { editing = it })
                                MainPage.CHART -> ChartScreen(viewModel, state)
                                MainPage.REPORT -> ReportScreen(viewModel, state)
                                MainPage.SETTINGS -> SettingsScreen(viewModel, state)
                            }
                        }
                    }
                }
            }
            state.userMessage?.let { message ->
                AlertDialog(
                    onDismissRequest = viewModel::clearMessage,
                    title = { Text("Pocket Ledger") },
                    text = { Text(message) },
                    confirmButton = { TextButton(onClick = viewModel::clearMessage) { Text("OK") } },
                )
            }
        }
    }
}

@Composable
private fun MainNavigation(page: MainPage, language: String, onPage: (MainPage) -> Unit, onAdd: () -> Unit) {
    NavigationBar(
        modifier = Modifier.heightIn(min = 80.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
    ) {
        NavItem(page == MainPage.HOME, AppGlyph.HOME, tr(language, "home"), "nav_home") { onPage(MainPage.HOME) }
        NavItem(page == MainPage.CHART, AppGlyph.CHART, tr(language, "chart"), "nav_chart") { onPage(MainPage.CHART) }
        NavigationBarItem(
            selected = false, onClick = onAdd, modifier = Modifier.testTag("nav_add"),
            icon = {
                Box(
                    Modifier.size(58.dp).shadow(8.dp, RoundedCornerShape(20.dp)).clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    LedgerIcon(AppGlyph.ADD, Modifier.size(27.dp), MaterialTheme.colorScheme.onPrimary)
                }
            }
        )
        NavItem(page == MainPage.REPORT, AppGlyph.REPORT, tr(language, "report"), "nav_report") { onPage(MainPage.REPORT) }
        NavItem(page == MainPage.SETTINGS, AppGlyph.PROFILE, tr(language, "profile"), "nav_profile") { onPage(MainPage.SETTINGS) }
    }
}

@Composable
private fun RowScope.NavItem(selected: Boolean, glyph: AppGlyph, label: String, testTag: String, onClick: () -> Unit) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        modifier = Modifier.testTag(testTag),
        icon = { LedgerIcon(glyph) },
        label = { Text(label, style = MaterialTheme.typography.labelMedium, maxLines = 1) },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
    )
}

@Composable
private fun HomeScreen(viewModel: LedgerViewModel, state: LedgerUiState, onEdit: (LedgerEntry) -> Unit) {
    val language = state.data.settings.language
    var selectedMonthText by rememberSaveable { mutableStateOf(YearMonth.now().toString()) }
    val selectedMonth = YearMonth.parse(selectedMonthText)
    val (income, expense, balance) = viewModel.monthTotals(selectedMonth.atDay(1))
    val entries = viewModel.filteredEntries()
        .filter { YearMonth.from(Instant.ofEpochMilli(it.occurredAt).atZone(ZoneId.systemDefault())) == selectedMonth }
        .sortedByDescending { it.occurredAt }
    var selected by remember { mutableStateOf<LedgerEntry?>(null) }
    var showSearch by rememberSaveable { mutableStateOf(false) }
    var showFilters by rememberSaveable { mutableStateOf(false) }
    var showMonthPicker by remember { mutableStateOf(false) }
    val filtersActive = state.categoryFilter != null || state.accountFilter != null || state.dateFilter != null
    Column(Modifier.fillMaxSize()) {
        HomeSummaryHeader(
            language = language,
            month = selectedMonth,
            currency = state.data.settings.currency,
            income = income,
            expense = expense,
            balance = balance,
            filtersActive = filtersActive,
            searchActive = showSearch || state.search.isNotBlank(),
            onToggleFilters = { showFilters = !showFilters },
            onToggleSearch = {
                if (showSearch && state.search.isNotBlank()) viewModel.setSearch("")
                showSearch = !showSearch
            },
            onSelectMonth = { showMonthPicker = true },
        )
        if (showSearch) HomeSearchField(state.search, language, viewModel::setSearch)
        if (showFilters) FilterStrip(viewModel, state)
        if (entries.isEmpty()) {
            EmptyState(tr(language, "no_entries"), tr(language, "add_first"), Modifier.weight(1f))
        } else {
            val grouped = entries.groupBy { Instant.ofEpochMilli(it.occurredAt).atZone(ZoneId.systemDefault()).toLocalDate() }
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
                grouped.toSortedMap(compareByDescending { it }).forEach { (date, dateEntries) ->
                    item { DateHeader(date, dateEntries, language, state.data.settings.currency) }
                    items(dateEntries, key = { it.id }) { entry ->
                        HomeEntryRow(entry, language, onClick = { selected = entry }, category = state.data.categories.firstOrNull { it.id == entry.categoryId })
                    }
                }
            }
        }
    }
    selected?.let { entry ->
        EntryDetailDialog(entry, language, onDismiss = { selected = null }, onEdit = { selected = null; onEdit(entry) }, onDelete = { viewModel.deleteEntry(entry); selected = null })
    }
    if (showMonthPicker) {
        MonthPickerDialog(
            selectedMonth = selectedMonth,
            language = language,
            onDismiss = { showMonthPicker = false },
            onSelected = { month -> selectedMonthText = month.toString(); showMonthPicker = false },
        )
    }
}

@Composable
private fun HomeSummaryHeader(
    language: String,
    month: YearMonth,
    currency: String,
    income: Double,
    expense: Double,
    balance: Double,
    filtersActive: Boolean,
    searchActive: Boolean,
    onToggleFilters: () -> Unit,
    onToggleSearch: () -> Unit,
    onSelectMonth: () -> Unit,
) {
    ModernHeader {
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.width(88.dp), contentAlignment = Alignment.CenterStart) {
                    HomeHeaderAction(AppGlyph.MENU, filtersActive, "home_filters", onToggleFilters)
                }
                Text(tr(language, "app"), modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(Modifier.width(88.dp), horizontalArrangement = Arrangement.End) {
                    HomeHeaderAction(AppGlyph.SEARCH, searchActive, "home_search", onToggleSearch)
                    HomeHeaderAction(AppGlyph.CALENDAR, false, "home_calendar", onSelectMonth)
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Bottom) {
                Column(Modifier.weight(1.15f).clip(RoundedCornerShape(14.dp)).clickable(onClick = onSelectMonth).testTag("home_month").padding(vertical = 4.dp)) {
                    Text(month.year.toString(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(month.format(DateTimeFormatter.ofPattern("MMM", statisticsLocale(language))), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, maxLines = 1)
                        Text("⌄", modifier = Modifier.padding(start = 4.dp), color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
                HomeMetric(tr(language, "expense"), money(expense, currency), MaterialTheme.colorScheme.error, Modifier.weight(1.25f))
                HomeMetric(tr(language, "income"), money(income, currency), Color(0xFF16865B), Modifier.weight(1f))
                HomeMetric(tr(language, "balance"), money(balance, currency), MaterialTheme.colorScheme.onPrimaryContainer, Modifier.weight(1.25f))
            }
        }
    }
}

@Composable
private fun HomeMetric(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.Start) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color, maxLines = 1)
    }
}

@Composable
private fun HomeHeaderAction(glyph: AppGlyph, active: Boolean, tag: String, onClick: () -> Unit) {
    Box(
        Modifier.size(44.dp).clip(CircleShape).background(if (active) MaterialTheme.colorScheme.primary.copy(alpha = .14f) else Color.Transparent).clickable(onClick = onClick).testTag(tag),
        contentAlignment = Alignment.Center,
    ) {
        LedgerIcon(glyph, Modifier.size(23.dp), if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimaryContainer)
    }
}

@Composable
private fun HomeSearchField(value: String, language: String, onValueChange: (String) -> Unit) {
    TextField(
        value = value,
        onValueChange = { onValueChange(it.take(LedgerConstraints.MAX_PURPOSE_LENGTH)) },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp).testTag("home_search_field"),
        placeholder = { Text(tr(language, "search")) },
        singleLine = true,
        leadingIcon = { LedgerIcon(AppGlyph.SEARCH, Modifier.size(21.dp), MaterialTheme.colorScheme.onSurfaceVariant) },
        trailingIcon = if (value.isNotBlank()) ({ Box(Modifier.size(40.dp).clip(CircleShape).clickable { onValueChange("") }, contentAlignment = Alignment.Center) { Text("×") } }) else null,
        shape = RoundedCornerShape(20.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
        ),
    )
}

@Composable
private fun MonthPickerDialog(
    selectedMonth: YearMonth,
    language: String,
    onDismiss: () -> Unit,
    onSelected: (YearMonth) -> Unit,
) {
    var displayedYear by rememberSaveable(selectedMonth) { mutableIntStateOf(selectedMonth.year) }
    val locale = statisticsLocale(language)
    AlertDialog(
        modifier = Modifier.testTag("month_picker"),
        onDismissRequest = onDismiss,
        title = { Text(tr(language, "select_month"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { displayedYear -= 1 }) { Text("‹", style = MaterialTheme.typography.titleLarge) }
                    Text(displayedYear.toString(), modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    IconButton(onClick = { displayedYear += 1 }) { Text("›", style = MaterialTheme.typography.titleLarge) }
                }
                (1..12).chunked(3).forEach { months ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        months.forEach { monthNumber ->
                            val month = YearMonth.of(displayedYear, monthNumber)
                            val selected = month == selectedMonth
                            Box(
                                Modifier.weight(1f).height(46.dp).clip(RoundedCornerShape(14.dp))
                                    .background(if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .45f))
                                    .clickable { onSelected(month) },
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    month.format(DateTimeFormatter.ofPattern("MMM", locale)),
                                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(tr(language, "close")) } },
    )
}

@Composable
private fun SummaryCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(modifier, shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .55f))) {
        Column(Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(6.dp))
            Text(value, fontWeight = FontWeight.Bold, color = color, maxLines = 1)
        }
    }
}

@Composable
private fun FilterStrip(viewModel: LedgerViewModel, state: LedgerUiState) {
    val language = state.data.settings.language
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 2.dp).testTag("home_filter_strip"), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        LedgerFilterChip(selected = state.categoryFilter == null && state.accountFilter == null && state.dateFilter == null, onClick = viewModel::clearFilters, label = tr(language, "all"))
        LedgerFilterChip(selected = state.dateFilter == LocalDate.now(), onClick = { viewModel.setDateFilter(if (state.dateFilter == LocalDate.now()) null else LocalDate.now()) }, label = tr(language, "today"))
        LedgerFilterChip(selected = state.dateFilter == LocalDate.now().minusDays(1), onClick = { viewModel.setDateFilter(if (state.dateFilter == LocalDate.now().minusDays(1)) null else LocalDate.now().minusDays(1)) }, label = tr(language, "yesterday"))
        state.data.categories.filterNot { it.archived }.forEach { category ->
            LedgerFilterChip(selected = state.categoryFilter == category.name, onClick = { viewModel.setCategoryFilter(if (state.categoryFilter == category.name) null else category.name) }, label = categoryLabel(language, category.name))
        }
        viewModel.activeAccounts().forEach { account ->
            LedgerFilterChip(selected = state.accountFilter == account.id, onClick = { viewModel.setAccountFilter(if (state.accountFilter == account.id) null else account.id) }, label = account.name)
        }
    }
}

@Composable
private fun LedgerFilterChip(selected: Boolean, onClick: () -> Unit, label: String) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        shape = RoundedCornerShape(14.dp),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surface,
            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ),
    )
}

@Composable
private fun DateHeader(date: LocalDate, entries: List<LedgerEntry>, language: String, currency: String) {
    val dailyExpense = entries.filter { it.type == EntryType.EXPENSE && it.currency == currency }.sumOf { it.amount }
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            date.format(DateTimeFormatter.ofPattern("MMM d  EEEE", statisticsLocale(language))),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
        )
        Text(
            "${tr(language, "expense")}: ${money(dailyExpense, currency)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun HomeEntryRow(entry: LedgerEntry, language: String, onClick: () -> Unit, category: LedgerCategory? = null) {
    val sign = if (entry.type == EntryType.INCOME) "+" else "−"
    val amountColor = if (entry.type == EntryType.INCOME) Color(0xFF16865B) else MaterialTheme.colorScheme.onSurface
    val accent = when {
        category == null || category.color == DEFAULT_CATEGORY_COLOR -> categoryColor(entry.category)
        else -> storedCategoryColor(category.color)
    }
    val time = Instant.ofEpochMilli(entry.occurredAt).atZone(ZoneId.systemDefault()).toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"))
    Column(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).background(accent, CircleShape), contentAlignment = Alignment.Center) {
                Text(category?.icon ?: categorySymbol(entry.category), color = Color.White, fontWeight = FontWeight.Bold)
            }
            Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
                Text(entry.purpose, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(listOf(categoryLabel(language, entry.category), entry.accountName, time).filter { it.isNotBlank() }.joinToString(" · "), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text("$sign${money(entry.amount, entry.currency)}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = amountColor, maxLines = 1)
        }
        HorizontalDivider(Modifier.padding(start = 76.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = .55f))
    }
}

@Composable
private fun EntryRow(entry: LedgerEntry, language: String, onClick: (() -> Unit)? = null, category: LedgerCategory? = null) {
    val sign = if (entry.type == EntryType.INCOME) "+" else "−"
    val color = if (entry.type == EntryType.INCOME) Color(0xFF16865B) else MaterialTheme.colorScheme.error
    val time = Instant.ofEpochMilli(entry.occurredAt).atZone(ZoneId.systemDefault()).toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"))
    val surfaceModifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
    Surface(surfaceModifier, shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 1.dp) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).background((category?.let { storedCategoryColor(it.color) } ?: categoryColor(entry.category)).copy(alpha = .16f), RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                Text(category?.icon ?: categorySymbol(entry.category))
            }
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(entry.purpose, fontWeight = FontWeight.SemiBold)
                Text(listOf(categoryLabel(language, entry.category), entry.accountName, time).filter { it.isNotBlank() }.joinToString(" · "), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
            Text("$sign${money(entry.amount, entry.currency)}", fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
private fun EntryDetailDialog(entry: LedgerEntry, language: String, onDismiss: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(entry.purpose) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DetailLine(tr(language, "type"), tr(language, if (entry.type == EntryType.INCOME) "income" else "expense"))
                DetailLine(tr(language, "amount"), money(entry.amount, entry.currency))
                DetailLine(tr(language, "category"), categoryLabel(language, entry.category))
                DetailLine(tr(language, "account"), entry.accountName.ifBlank { "—" })
                DetailLine(tr(language, "date_time"), formatDateTime(entry.occurredAt))
                if (entry.note.isNotBlank()) DetailLine(tr(language, "note"), entry.note)
            }
        },
        confirmButton = { TextButton(onClick = onEdit) { Text(tr(language, "edit")) } },
        dismissButton = { Row { TextButton(onClick = onDelete) { Text(tr(language, "delete"), color = MaterialTheme.colorScheme.error) }; TextButton(onClick = onDismiss) { Text(tr(language, "close")) } } }
    )
}

@Composable
private fun DetailLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth()) { Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(104.dp)); Text(value, Modifier.weight(1f)) }
}

@Composable
private fun AddEntryScreen(viewModel: LedgerViewModel, editing: LedgerEntry?, onClose: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val language = state.data.settings.language
    var mode by rememberSaveable(editing?.id) { mutableStateOf(if (editing == null) "sentence" else "manual") }
    var sentence by rememberSaveable(editing?.id) { mutableStateOf(editing?.rawText.orEmpty()) }
    var reviewing by rememberSaveable(editing?.id) { mutableStateOf(editing != null) }
    var missing by remember { mutableStateOf(emptySet<String>()) }
    var draft by remember(editing?.id) {
        mutableStateOf(editing ?: LedgerEntry(currency = state.data.settings.currency))
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (reviewing) tr(language, "confirm") else tr(language, "add_entry"), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onClose) { Text("‹", style = MaterialTheme.typography.headlineMedium) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            if (!reviewing) {
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp)) {
                    listOf("sentence", "manual").forEachIndexed { index, item ->
                        SegmentedButton(
                            selected = mode == item,
                            onClick = { mode = item },
                            shape = SegmentedButtonDefaults.itemShape(index, 2),
                        ) { Text(tr(language, item)) }
                    }
                }
            }
            if (mode == "sentence" && !reviewing) {
                SentenceInput(
                    text = sentence, onTextChange = { sentence = it }, language = language,
                    onParse = {
                        val result = viewModel.parse(sentence)
                        draft = result.entry
                        missing = result.missing
                        reviewing = true
                    }
                )
            } else {
                EntryForm(
                    entry = draft,
                    language = language,
                    accounts = viewModel.activeAccounts(),
                    categories = viewModel.activeCategories(draft.type),
                    missing = missing,
                    onChange = { draft = it },
                    onSave = {
                        if (viewModel.saveEntry(draft)) onClose()
                    },
                    onCancel = if (reviewing && editing == null && mode == "sentence") ({ reviewing = false }) else onClose
                )
            }
        }
    }
}

@Composable
private fun SentenceInput(text: String, onTextChange: (String) -> Unit, language: String, onParse: () -> Unit) {
    val speakLabel = tr(language, "speak")
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    var showImeHint by remember { mutableStateOf(false) }
    var imeRequest by remember { mutableIntStateOf(0) }
    LaunchedEffect(imeRequest) {
        if (imeRequest > 0) {
            focusRequester.requestFocus()
            withFrameNanos { }
            keyboardController?.show()
        }
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(shape = LedgerCardShape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = .65f))) {
            Text(tr(language, "sentence_hint"), Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        OutlinedTextField(
            value = text,
            onValueChange = { onTextChange(it.take(LedgerConstraints.MAX_SENTENCE_LENGTH)) },
            modifier = Modifier.fillMaxWidth().heightIn(min = 150.dp).focusRequester(focusRequester),
            label = { Text(tr(language, "sentence")) }, placeholder = { Text(tr(language, "sentence_hint")) }, shape = RoundedCornerShape(20.dp)
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = {
                    imeRequest += 1
                    showImeHint = true
                }, modifier = Modifier.weight(1f)
            ) { Text("⌨  $speakLabel") }
            Button(onClick = onParse, enabled = text.isNotBlank(), modifier = Modifier.weight(1f)) { Text(tr(language, "parse")) }
        }
        if (showImeHint) {
            Text(tr(language, "ime_voice_hint"), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
        }
        Text("中文 · English · 日本語 · 한국어 · Español · Français · Deutsch · Português · العربية", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun EntryForm(
    entry: LedgerEntry,
    language: String,
    accounts: List<LedgerAccount>,
    categories: List<LedgerCategory>,
    missing: Set<String>,
    onChange: (LedgerEntry) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    var saveAttempted by remember(entry.id) { mutableStateOf(false) }
    var amountText by remember(entry.id) { mutableStateOf(if (entry.amount > 0) cleanNumber(entry.amount) else "") }
    var dateText by remember(entry.id) { mutableStateOf(Instant.ofEpochMilli(entry.occurredAt).atZone(ZoneId.systemDefault()).toLocalDate().toString()) }
    var timeText by remember(entry.id) { mutableStateOf(Instant.ofEpochMilli(entry.occurredAt).atZone(ZoneId.systemDefault()).toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"))) }
    val compatibleAccounts = accounts.filter { it.currency == entry.currency }
    val dateTimeValid = runCatching {
        LocalDate.parse(dateText)
        LocalTime.parse(timeText, DateTimeFormatter.ofPattern("H:mm"))
    }.isSuccess
    val valid = LedgerConstraints.isValidTransactionAmount(entry.amount) &&
        entry.purpose.isNotBlank() && entry.purpose.length <= LedgerConstraints.MAX_PURPOSE_LENGTH &&
        dateTimeValid

    fun updateDateTime(dateValue: String = dateText, timeValue: String = timeText) {
        runCatching {
            val date = LocalDate.parse(dateValue)
            val time = LocalTime.parse(timeValue, DateTimeFormatter.ofPattern("H:mm"))
            onChange(entry.copy(occurredAt = ZonedDateTime.of(date, time, ZoneId.systemDefault()).toInstant().toEpochMilli()))
        }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FilterChip(selected = entry.type == EntryType.EXPENSE, onClick = { onChange(entry.copy(type = EntryType.EXPENSE, categoryId = null, category = "")) }, label = { Text(tr(language, "expense")) })
            FilterChip(selected = entry.type == EntryType.INCOME, onClick = { onChange(entry.copy(type = EntryType.INCOME, categoryId = null, category = "")) }, label = { Text(tr(language, "income")) })
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = amountText, onValueChange = { value ->
                    amountText = value.take(32)
                    val amount = amountText.replace(",", ".").toDoubleOrNull()?.takeIf(LedgerConstraints::isValidTransactionAmount) ?: 0.0
                    onChange(entry.copy(amount = amount))
                },
                modifier = Modifier.weight(1.3f), label = { Text(tr(language, "amount")) }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), isError = saveAttempted && !LedgerConstraints.isValidTransactionAmount(entry.amount)
            )
            DropdownField(entry.currency, currencies, tr(language, "currency"), Modifier.weight(1f)) { currency ->
                val selected = accounts.firstOrNull { it.id == entry.accountId }
                onChange(entry.copy(currency = currency, accountId = if (selected?.currency == currency) entry.accountId else null, accountName = if (selected?.currency == currency) entry.accountName else ""))
            }
        }
        OutlinedTextField(
            value = entry.purpose, onValueChange = { onChange(entry.copy(purpose = it.take(LedgerConstraints.MAX_PURPOSE_LENGTH))) }, modifier = Modifier.fillMaxWidth(),
            label = { Text(tr(language, "purpose")) }, singleLine = true, isError = saveAttempted && entry.purpose.isBlank(),
            supportingText = if (saveAttempted && entry.purpose.isBlank()) ({ Text("Purpose could not be identified. Please enter it manually.") }) else null
        )
        DropdownField(
            value = entry.categoryId.orEmpty(), options = listOf("") + categories.map { it.id }, label = tr(language, "category"), modifier = Modifier.fillMaxWidth(),
            display = { id -> categories.firstOrNull { it.id == id }?.let { categoryLabel(language, it.name) } ?: if (id == entry.categoryId) categoryLabel(language, entry.category) else "—" }, onSelected = { id ->
                val category = categories.firstOrNull { it.id == id }
                onChange(entry.copy(categoryId = category?.id, category = category?.name.orEmpty()))
            }
        )
        DropdownField(
            value = entry.accountId.orEmpty(), options = listOf("") + compatibleAccounts.map { it.id }, label = tr(language, "account"), modifier = Modifier.fillMaxWidth(),
            display = { id -> compatibleAccounts.firstOrNull { it.id == id }?.name ?: if (id == entry.accountId) entry.accountName else "—" }, onSelected = { id ->
                val account = compatibleAccounts.firstOrNull { it.id == id }
                onChange(entry.copy(accountId = account?.id, accountName = account?.name.orEmpty()))
            }
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(value = dateText, onValueChange = { dateText = it.take(10); updateDateTime(dateValue = dateText) }, label = { Text("YYYY-MM-DD") }, singleLine = true, isError = saveAttempted && !dateTimeValid, modifier = Modifier.weight(1.4f))
            OutlinedTextField(value = timeText, onValueChange = { timeText = it.take(5); updateDateTime(timeValue = timeText) }, label = { Text("HH:MM") }, singleLine = true, isError = saveAttempted && !dateTimeValid, modifier = Modifier.weight(1f))
        }
        if (saveAttempted && !dateTimeValid) Text("Enter a valid date and time.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        OutlinedTextField(value = entry.note, onValueChange = { onChange(entry.copy(note = it.take(LedgerConstraints.MAX_NOTE_LENGTH))) }, modifier = Modifier.fillMaxWidth().heightIn(min = 90.dp), label = { Text(tr(language, "note")) })
        if (saveAttempted && !valid) Text(tr(language, "required"), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) { Text(tr(language, "cancel")) }
            Button(onClick = { saveAttempted = true; if (valid) onSave() }, modifier = Modifier.weight(1f)) { Text(tr(language, "save")) }
        }
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun DropdownField(
    value: String,
    options: List<String>,
    label: String,
    modifier: Modifier = Modifier,
    display: (String) -> String = { it },
    onSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth().height(56.dp), contentPadding = PaddingValues(horizontal = 12.dp)) {
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(display(value).ifBlank { "—" }, maxLines = 1)
            }
            Text("▾")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.distinct().forEach { option ->
                DropdownMenuItem(text = { Text(display(option)) }, onClick = { onSelected(option); expanded = false })
            }
        }
    }
}

@Composable
private fun ChartScreen(viewModel: LedgerViewModel, state: LedgerUiState) {
    val language = state.data.settings.language
    val currency = state.data.settings.currency
    val now = remember { LocalDate.now() }
    var selectedTypeName by rememberSaveable { mutableStateOf(EntryType.EXPENSE.name) }
    val selectedType = EntryType.valueOf(selectedTypeName)
    var range by rememberSaveable { mutableStateOf("month") }
    val sourceEntries = remember(state.data.entries, currency) { viewModel.entriesInCurrency(currency) }
    val periods = remember(sourceEntries, range, now) { LedgerAnalytics.availablePeriods(sourceEntries, range, now) }
    var selectedPeriodStart by rememberSaveable(range) {
        mutableLongStateOf(LedgerAnalytics.periodContaining(range, now).start.toEpochDay())
    }
    val selectedPeriod = periods.firstOrNull { it.start.toEpochDay() == selectedPeriodStart }
        ?: LedgerAnalytics.periodContaining(range, now)
    val entries = remember(sourceEntries, selectedPeriod) { LedgerAnalytics.entriesInPeriod(sourceEntries, selectedPeriod) }
    val selectedEntries = remember(entries, selectedType) { entries.filter { it.type == selectedType } }
    val categoryTotals = remember(selectedEntries, selectedType, currency) {
        LedgerAnalytics.categoryTotals(selectedEntries, selectedType, currency).toList().sortedByDescending { it.second }
    }
    val total = categoryTotals.sumOf { it.second }
    val segments = remember(categoryTotals, state.data.categories) { categoryChartSegments(categoryTotals, state.data.categories) }
    val buckets = remember(entries, range, currency, selectedPeriod) {
        LedgerAnalytics.trendBuckets(entries, range, currency, selectedPeriod.start)
    }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            StatisticsHeader(
                selectedType = selectedType,
                range = range,
                language = language,
                onTypeSelected = { selectedTypeName = it.name },
                onRangeSelected = { range = it },
                onCurrentPeriod = {
                    selectedPeriodStart = LedgerAnalytics.periodContaining(range, now).start.toEpochDay()
                },
            )
        }
        item { Spacer(Modifier.height(12.dp)) }
        item {
            StatisticsPeriodSelector(
                periods = periods,
                selectedPeriod = selectedPeriod,
                range = range,
                language = language,
                now = now,
                onSelected = { selectedPeriodStart = it.start.toEpochDay() },
            )
        }
        item { Spacer(Modifier.height(8.dp)) }
        if (selectedEntries.isEmpty()) {
            item { EmptyState(tr(language, "no_data"), tr(language, "add_first"), Modifier.height(260.dp)) }
        } else {
            item {
                CategoryDonutChart(segments, total, currency, language)
            }
            item {
                Text(tr(language, "category_details"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(20.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            item {
                CategoryDetailList(segments, total, currency, language, state.data.categories)
            }
            item { Spacer(Modifier.height(20.dp)) }
            item {
                ElevatedCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp).testTag("statistics_trend"), shape = LedgerCardShape) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            tr(language, "trend"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.height(20.dp))
                        TrendChart(buckets, selectedType, language)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatisticsHeader(
    selectedType: EntryType,
    range: String,
    language: String,
    onTypeSelected: (EntryType) -> Unit,
    onRangeSelected: (String) -> Unit,
    onCurrentPeriod: () -> Unit,
) {
    ModernHeader {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                StatisticsTypeSelector(
                    selectedType = selectedType,
                    language = language,
                    onSelected = onTypeSelected,
                    modifier = Modifier.align(Alignment.Center),
                )
                IconButton(onClick = onCurrentPeriod, modifier = Modifier.align(Alignment.CenterEnd).testTag("statistics_current_period")) {
                    LedgerIcon(AppGlyph.CALENDAR, Modifier.size(25.dp), MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                listOf("week", "month", "year").forEachIndexed { index, item ->
                    SegmentedButton(
                        selected = range == item,
                        onClick = { onRangeSelected(item) },
                        shape = SegmentedButtonDefaults.itemShape(index, 3),
                        modifier = Modifier.testTag("statistics_range_$item"),
                    ) { Text(tr(language, item)) }
                }
            }
        }
    }
}

@Composable
private fun StatisticsTypeSelector(
    selectedType: EntryType,
    language: String,
    onSelected: (EntryType) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        TextButton(
            onClick = { expanded = true },
            modifier = Modifier.widthIn(min = 164.dp).heightIn(min = 52.dp).testTag("statistics_type_selector"),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
        ) {
            Text(
                tr(language, if (selectedType == EntryType.EXPENSE) "expense" else "income"),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Spacer(Modifier.width(12.dp))
            Text("⌄", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            listOf(EntryType.EXPENSE, EntryType.INCOME).forEach { type ->
                DropdownMenuItem(
                    text = { Text(tr(language, if (type == EntryType.EXPENSE) "expense" else "income")) },
                    leadingIcon = { if (type == selectedType) Text("✓", color = MaterialTheme.colorScheme.primary) },
                    onClick = { onSelected(type); expanded = false },
                    modifier = Modifier.testTag("statistics_type_${type.name}"),
                )
            }
        }
    }
}

@Composable
private fun StatisticsPeriodSelector(
    periods: List<StatisticsPeriod>,
    selectedPeriod: StatisticsPeriod,
    range: String,
    language: String,
    now: LocalDate,
    onSelected: (StatisticsPeriod) -> Unit,
) {
    val listState = rememberLazyListState()
    val selectedIndex = periods.indexOfFirst { it.start == selectedPeriod.start }.coerceAtLeast(0)
    LaunchedEffect(selectedIndex, periods.size, range) {
        if (periods.isNotEmpty()) listState.animateScrollToItem((selectedIndex - 1).coerceAtLeast(0))
    }
    LazyRow(
        state = listState,
        modifier = Modifier.fillMaxWidth().testTag("statistics_period_selector"),
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        itemsIndexed(periods, key = { _, period -> period.start.toEpochDay() }) { _, period ->
            val selected = period.start == selectedPeriod.start
            Column(
                Modifier.widthIn(min = 112.dp).clickable { onSelected(period) }.padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    statisticsPeriodTabLabel(language, range, period, now),
                    color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                )
                if (selected && range == "week") {
                    Text(
                        formatStatisticsPeriod(language, range, period),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                    )
                } else {
                    Spacer(Modifier.height(16.dp))
                }
                Spacer(Modifier.height(5.dp))
                Box(
                    Modifier.fillMaxWidth().height(3.dp).background(
                        if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        RoundedCornerShape(2.dp),
                    )
                )
            }
        }
    }
}

private data class CategoryChartSegment(val category: String, val amount: Double, val color: Color)

private fun categoryChartSegments(
    categoryTotals: List<Pair<String, Double>>,
    categories: List<LedgerCategory>,
): List<CategoryChartSegment> = categoryTotals.map { (category, amount) ->
    val storedCategory = categories.firstOrNull { it.name == category }
    CategoryChartSegment(
        category = category,
        amount = amount,
        color = if (storedCategory == null || storedCategory.color == DEFAULT_CATEGORY_COLOR) {
            categoryColor(category)
        } else {
            storedCategoryColor(storedCategory.color)
        },
    )
}

@Composable
private fun CategoryDonutChart(
    segments: List<CategoryChartSegment>,
    total: Double,
    currency: String,
    language: String,
) {
    if (total <= 0.0) return
    val ringTrackColor = MaterialTheme.colorScheme.surfaceVariant
    ElevatedCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp).testTag("statistics_donut"), shape = LedgerCardShape) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(Modifier.size(152.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) {
                    val strokeWidth = 26.dp.toPx()
                    drawArc(
                        color = ringTrackColor,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(strokeWidth, cap = StrokeCap.Round),
                    )
                    var startAngle = -90f
                    segments.forEach { segment ->
                        val sweepAngle = (segment.amount / total * 360.0).toFloat()
                        val gap = if (segments.size > 1) minOf(1.4f, sweepAngle / 3f) else 0f
                        drawArc(
                            color = segment.color,
                            startAngle = startAngle + gap / 2f,
                            sweepAngle = (sweepAngle - gap).coerceAtLeast(0f),
                            useCenter = false,
                            style = Stroke(strokeWidth, cap = StrokeCap.Round),
                        )
                        startAngle += sweepAngle
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(money(total, currency), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, maxLines = 1)
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                segments.take(5).forEach { segment ->
                    CategoryLegendItem(segment, total, language)
                }
            }
        }
    }
}

@Composable
private fun CategoryLegendItem(segment: CategoryChartSegment, total: Double, language: String) {
    val percent = (segment.amount / total * 100.0).roundToInt()
    val centerColor = MaterialTheme.colorScheme.surface
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(14.dp).background(segment.color, CircleShape), contentAlignment = Alignment.Center) {
            Box(Modifier.size(7.dp).background(centerColor, CircleShape))
        }
        Text(categoryLabel(language, segment.category), Modifier.weight(1f).padding(horizontal = 8.dp), fontWeight = FontWeight.Medium, maxLines = 1)
        Text("$percent%", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun CategoryDetailList(
    segments: List<CategoryChartSegment>,
    total: Double,
    currency: String,
    language: String,
    categories: List<LedgerCategory>,
) {
    ElevatedCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp).testTag("statistics_category_details"), shape = LedgerCardShape) {
        Column(Modifier.fillMaxWidth()) {
            segments.forEachIndexed { index, segment ->
                val category = categories.firstOrNull { it.name == segment.category }
                val fraction = (segment.amount / total).toFloat().coerceIn(0f, 1f)
                if (index > 0) HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(46.dp).background(segment.color.copy(alpha = .16f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(category?.icon ?: categorySymbol(segment.category), fontWeight = FontWeight.Bold)
                    }
                    Column(Modifier.weight(1f).padding(start = 14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(categoryLabel(language, segment.category), fontWeight = FontWeight.SemiBold)
                            Text("${(fraction * 100).roundToInt()}%", modifier = Modifier.padding(start = 10.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.weight(1f))
                            Text(money(segment.amount, currency), fontWeight = FontWeight.Medium)
                        }
                        LinearProgressIndicator(
                            progress = { fraction },
                            modifier = Modifier.fillMaxWidth().height(7.dp),
                            color = segment.color,
                            trackColor = segment.color.copy(alpha = .14f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TrendChart(buckets: List<TrendBucket>, selectedType: EntryType, language: String) {
    val maxValue = max(1.0, buckets.maxOfOrNull { max(it.income, it.expense) } ?: 1.0)
    val incomeColor = Color(0xFF35A875).copy(alpha = if (selectedType == EntryType.INCOME) 1f else .24f)
    val expenseColor = Color(0xFFE56C65).copy(alpha = if (selectedType == EntryType.EXPENSE) 1f else .24f)
    Column {
        Canvas(Modifier.fillMaxWidth().height(190.dp)) {
            val groupWidth = size.width / max(1, buckets.size)
            val barWidth = groupWidth * .28f
            val chartHeight = size.height - 8.dp.toPx()
            buckets.forEachIndexed { index, bucket ->
                val incomeHeight = (bucket.income / maxValue * chartHeight).toFloat()
                val expenseHeight = (bucket.expense / maxValue * chartHeight).toFloat()
                val groupStart = index * groupWidth
                drawRoundRect(incomeColor, Offset(groupStart + groupWidth * .18f, chartHeight - incomeHeight), Size(barWidth, incomeHeight), CornerRadius(barWidth * .35f))
                drawRoundRect(expenseColor, Offset(groupStart + groupWidth * .54f, chartHeight - expenseHeight), Size(barWidth, expenseHeight), CornerRadius(barWidth * .35f))
            }
            drawLine(Color.Gray.copy(alpha = .4f), Offset(0f, chartHeight), Offset(size.width, chartHeight), strokeWidth = 1.dp.toPx())
        }
        Row(Modifier.fillMaxWidth()) {
            buckets.forEach { bucket -> Text(localizedTrendLabel(language, bucket.label), style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center, modifier = Modifier.weight(1f), maxLines = 1) }
        }
        Row(Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            LegendDot(incomeColor, tr(language, "income"), selectedType == EntryType.INCOME)
            LegendDot(expenseColor, tr(language, "expense"), selectedType == EntryType.EXPENSE)
        }
    }
}

@Composable
private fun LegendDot(color: Color, text: String, emphasized: Boolean = true) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(9.dp).background(color, CircleShape))
        Spacer(Modifier.width(5.dp))
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (emphasized) FontWeight.Bold else FontWeight.Normal,
            color = if (emphasized) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CategoryBar(label: String, amount: Double, total: Double, currency: String, color: Color, changePercent: Double? = null) {
    val fraction = if (total <= 0) 0f else (amount / total).toFloat().coerceIn(0f, 1f)
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
        Row {
            Text(label, Modifier.weight(1f), fontWeight = FontWeight.Medium)
            Text("${money(amount, currency)} · ${(fraction * 100).toInt()}%" + (changePercent?.let { " · ${if (it >= 0) "+" else ""}${it.toInt()}%" } ?: ""))
        }
        Spacer(Modifier.height(7.dp))
        LinearProgressIndicator(progress = { fraction }, modifier = Modifier.fillMaxWidth().height(8.dp), color = color, trackColor = color.copy(alpha = .14f))
    }
}

@Composable
private fun ReportScreen(viewModel: LedgerViewModel, state: LedgerUiState) {
    val language = state.data.settings.language
    var tab by rememberSaveable { mutableIntStateOf(0) }
    Column(Modifier.fillMaxSize()) {
        ModernHeader {
            Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(tr(language, "report"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    listOf("analytics", "accounts").forEachIndexed { index, key ->
                        SegmentedButton(
                            selected = tab == index,
                            onClick = { tab = index },
                            shape = SegmentedButtonDefaults.itemShape(index, 2),
                        ) { Text(tr(language, key)) }
                    }
                }
            }
        }
        if (tab == 0) AnalyticsScreen(viewModel, state, Modifier.weight(1f)) else AccountsScreen(viewModel, state, Modifier.weight(1f))
    }
}

@Composable
private fun AnalyticsScreen(viewModel: LedgerViewModel, state: LedgerUiState, modifier: Modifier = Modifier) {
    val language = state.data.settings.language
    val currency = state.data.settings.currency
    val now = LocalDate.now()
    val weekStart = now.minusDays((now.dayOfWeek.value - 1).toLong())
    val week = viewModel.spendingSince(weekStart, weekStart.plusWeeks(1))
    val month = viewModel.spendingSince(now.withDayOfMonth(1), now.withDayOfMonth(1).plusMonths(1))
    val year = viewModel.spendingSince(now.withDayOfYear(1), now.withDayOfYear(1).plusYears(1))
    val totals = viewModel.monthTotals()
    val budget = if (state.data.budget.currency == currency) state.data.budget.total else 0.0
    val progress = if (budget <= 0) 0f else (month / budget).toFloat()
    val monthCategorySpending = LedgerAnalytics.categoryTotals(viewModel.entriesInRange("month"), EntryType.EXPENSE, currency).toList().sortedByDescending { it.second }
    var budgetDialog by remember { mutableStateOf(false) }
    var categoryBudgetDialog by remember { mutableStateOf(false) }

    LazyColumn(modifier, contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { MonthlyStatisticsCard(language, currency, now, totals) }
        item {
            MonthlyBudgetCard(
                language = language,
                currency = currency,
                budget = budget,
                spent = month,
                progress = progress,
                onSetBudget = { budgetDialog = true },
                onSetCategoryBudget = { categoryBudgetDialog = true },
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SummaryCard(tr(language, "week_spend"), money(week, currency), MaterialTheme.colorScheme.error, Modifier.weight(1f))
                SummaryCard(tr(language, "year_spend"), money(year, currency), MaterialTheme.colorScheme.error, Modifier.weight(1f))
            }
        }
        (if (state.data.budget.currency == currency) state.data.budget.categoryAmounts else emptyMap()).forEach { (category, limit) ->
            val spent = viewModel.entriesInRange("month").filter { it.type == EntryType.EXPENSE && it.category == category }.sumOf { it.amount }
            item { CategoryBudgetRow(categoryLabel(language, category), spent, limit, currency) }
        }
        if (monthCategorySpending.isNotEmpty()) {
            item { Text(tr(language, "categories"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp), maxLines = 1, overflow = TextOverflow.Ellipsis) }
            items(monthCategorySpending, key = { it.first }) { (category, spent) ->
                CategoryBar(categoryLabel(language, category), spent, month, currency, state.data.categories.firstOrNull { it.name == category }?.let { storedCategoryColor(it.color) } ?: categoryColor(category))
            }
        }
    }
    if (budgetDialog) BudgetDialog(budget, language, currency, onDismiss = { budgetDialog = false }) { viewModel.setBudget(it); budgetDialog = false }
    if (categoryBudgetDialog) CategoryBudgetDialog(
        categories = viewModel.activeCategories(EntryType.EXPENSE),
        currency = currency,
        onDismiss = { categoryBudgetDialog = false },
        onSave = { name, amount -> viewModel.setCategoryBudget(name, amount); categoryBudgetDialog = false },
    )
}

@Composable
private fun MonthlyStatisticsCard(language: String, currency: String, now: LocalDate, totals: Triple<Double, Double, Double>) {
    ElevatedCard(Modifier.fillMaxWidth(), shape = LedgerCardShape) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(tr(language, "this_month"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(now.format(DateTimeFormatter.ofPattern("MMM", statisticsLocale(language))), style = MaterialTheme.typography.titleLarge, modifier = Modifier.width(56.dp), maxLines = 1)
                ReportMetric(tr(language, "expense"), money(totals.second, currency), MaterialTheme.colorScheme.error, Modifier.weight(1f))
                ReportMetric(tr(language, "income"), money(totals.first, currency), Color(0xFF16865B), Modifier.weight(1f))
                ReportMetric(tr(language, "balance"), money(totals.third, currency), MaterialTheme.colorScheme.primary, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ReportMetric(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.End) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = color, maxLines = 1)
    }
}

@Composable
private fun MonthlyBudgetCard(
    language: String,
    currency: String,
    budget: Double,
    spent: Double,
    progress: Float,
    onSetBudget: () -> Unit,
    onSetCategoryBudget: () -> Unit,
) {
    val indicatorColor = if (progress > 1f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    ElevatedCard(Modifier.fillMaxWidth(), shape = LedgerCardShape) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(tr(language, "budget"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                TextButton(onClick = onSetBudget) { Text(tr(language, "set_budget")) }
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                Box(Modifier.size(112.dp), contentAlignment = Alignment.Center) {
                    Canvas(Modifier.fillMaxSize()) {
                        val stroke = 13.dp.toPx()
                        drawArc(trackColor, -90f, 360f, false, style = Stroke(stroke, cap = StrokeCap.Round))
                        if (budget > 0) drawArc(indicatorColor, -90f, progress.coerceIn(0f, 1f) * 360f, false, style = Stroke(stroke, cap = StrokeCap.Round))
                    }
                    Text(if (budget <= 0) "--" else "${(progress * 100).roundToInt()}%", fontWeight = FontWeight.Bold, color = indicatorColor)
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    DetailLine(tr(language, "remaining"), money(budget - spent, currency))
                    HorizontalDivider()
                    DetailLine(tr(language, "budget"), money(budget, currency))
                    DetailLine(tr(language, "expense"), money(spent, currency))
                }
            }
            if (progress > 1) Text("⚠ ${tr(language, "over_budget")}: ${money(spent - budget, currency)}", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
            TextButton(onClick = onSetCategoryBudget, modifier = Modifier.align(Alignment.End)) { Text(tr(language, "category_budgets")) }
        }
    }
}

@Composable
private fun CategoryBudgetRow(label: String, spent: Double, limit: Double, currency: String) {
    val fraction = if (limit <= 0) 0f else (spent / limit).toFloat()
    Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row { Text(label, Modifier.weight(1f)); Text("${money(spent, currency)} / ${money(limit, currency)}", color = if (fraction > 1) MaterialTheme.colorScheme.error else LocalContentColor.current) }
        LinearProgressIndicator(progress = { fraction.coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().padding(top = 6.dp), color = if (fraction > 1) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun BudgetDialog(current: Double, language: String, currency: String, onDismiss: () -> Unit, onSave: (Double) -> Unit) {
    var value by remember { mutableStateOf(cleanNumber(current)) }
    val parsed = value.replace(",", ".").toDoubleOrNull()
    val valid = parsed != null && LedgerConstraints.isValidBudget(parsed)
    AlertDialog(onDismissRequest = onDismiss, title = { Text(tr(language, "set_budget")) }, text = {
        OutlinedTextField(value = value, onValueChange = { value = it.take(32) }, label = { Text("${tr(language, "amount")} ($currency)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, isError = value.isNotBlank() && !valid)
    }, confirmButton = { Button(onClick = { parsed?.let(onSave) }, enabled = valid) { Text(tr(language, "save")) } }, dismissButton = { TextButton(onClick = onDismiss) { Text(tr(language, "cancel")) } })
}

@Composable
private fun CategoryBudgetDialog(categories: List<LedgerCategory>, currency: String, onDismiss: () -> Unit, onSave: (String, Double) -> Unit) {
    var categoryName by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    val parsedAmount = amount.replace(",", ".").toDoubleOrNull()
    val valid = parsedAmount != null && parsedAmount > 0.0 && LedgerConstraints.isValidBudget(parsedAmount)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set category budget") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DropdownField(categoryName, listOf("") + categories.map { it.name }, "Category", Modifier.fillMaxWidth()) { categoryName = it }
                OutlinedTextField(amount, { amount = it.take(32) }, label = { Text("Amount ($currency)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, isError = amount.isNotBlank() && !valid)
            }
        },
        confirmButton = { Button(onClick = { parsedAmount?.let { onSave(categoryName, it) } }, enabled = categoryName.isNotBlank() && valid) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun AccountsScreen(viewModel: LedgerViewModel, state: LedgerUiState, modifier: Modifier = Modifier) {
    val language = state.data.settings.language
    val currency = state.data.settings.currency
    var editing by remember { mutableStateOf<LedgerAccount?>(null) }
    var creating by remember { mutableStateOf(false) }
    var historyAccount by remember { mutableStateOf<LedgerAccount?>(null) }
    var deleting by remember { mutableStateOf<LedgerAccount?>(null) }
    var managing by rememberSaveable { mutableStateOf(false) }
    val assetTotals = state.data.accounts.filter { it.includeInAssets && !it.archived }.groupBy { it.currency }.mapValues { (_, accounts) -> accounts.sumOf { it.balance } }
    val currentCurrencyAccounts = state.data.accounts.filter { it.includeInAssets && !it.archived && it.currency == currency }
    val assets = currentCurrencyAccounts.filter { it.balance >= 0 }.sumOf { it.balance }
    val liabilities = currentCurrencyAccounts.filter { it.balance < 0 }.sumOf { -it.balance }
    val netWorth = assets - liabilities
    val groupedAccounts = state.data.accounts.sortedWith(compareBy<LedgerAccount> { it.archived }.thenBy { it.name }).groupBy {
        if (it.archived) "${it.type} · ${tr(language, "archive")}" else it.type
    }
    LazyColumn(modifier, contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), shape = LedgerCardShape) {
                Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.Top) {
                        Column(Modifier.weight(1f)) {
                            Text(tr(language, "net_worth"), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(money(netWorth, currency), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                        Text("●", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.primary.copy(alpha = .22f))
                    }
                    Row(Modifier.fillMaxWidth()) {
                        AccountTotalMetric(tr(language, "total_assets"), money(assets, currency), Modifier.weight(1f))
                        AccountTotalMetric(tr(language, "liabilities"), money(liabilities, currency), Modifier.weight(1f))
                    }
                    assetTotals.filterKeys { it != currency }.toSortedMap().forEach { (assetCurrency, total) ->
                        Text("$assetCurrency ${tr(language, "total_assets")}: ${money(total, assetCurrency)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { creating = true }, modifier = Modifier.weight(1f)) { Text("+ ${tr(language, "add_account")}") }
                OutlinedButton(onClick = { managing = !managing }, modifier = Modifier.weight(1f)) { Text(if (managing) tr(language, "close") else tr(language, "manage_accounts")) }
            }
        }
        groupedAccounts.forEach { (type, accounts) ->
            item(key = "heading-$type") {
                Row(Modifier.fillMaxWidth().padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(type, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                    val currenciesInGroup = accounts.map { it.currency }.distinct()
                    if (currenciesInGroup.size == 1) Text(money(accounts.sumOf { it.balance }, currenciesInGroup.first()), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            items(accounts, key = { it.id }) { account ->
                val recent = state.data.entries.filter { it.accountId == account.id }.maxByOrNull { it.occurredAt }
                AccountListRow(
                    account = account,
                    recent = recent,
                    language = language,
                    managing = managing,
                    onClick = { if (managing) editing = account else historyAccount = account },
                )
            }
        }
    }
    if (creating) AccountDialog(
        account = null,
        language = language,
        defaultCurrency = currency,
        existingAccounts = state.data.accounts,
        currencyEditable = true,
        onDismiss = { creating = false },
        onSave = { if (viewModel.addAccount(it)) creating = false },
    )
    editing?.let { account ->
        AccountDialog(
            account = account,
            language = language,
            defaultCurrency = currency,
            existingAccounts = state.data.accounts,
            currencyEditable = LedgerRules.canChangeAccountCurrency(account.id, state.data.entries),
            onDismiss = { editing = null },
            onSave = { if (viewModel.addAccount(it)) editing = null },
            onArchive = { viewModel.addAccount(account.copy(archived = !account.archived)); editing = null },
            onDelete = {
                if (!viewModel.deleteUnusedAccount(account)) deleting = account
                editing = null
            },
        )
    }
    historyAccount?.let { account ->
        AccountHistoryDialog(
            account = account,
            entries = state.data.entries.filter { it.accountId == account.id },
            language = language,
            onDismiss = { historyAccount = null },
            onEdit = { historyAccount = null; editing = account },
        )
    }
    deleting?.let { account ->
        AccountDeleteDialog(
            account = account,
            targets = state.data.accounts.filter { it.id != account.id && !it.archived && it.currency == account.currency },
            usageCount = viewModel.accountUsageCount(account.id),
            onDismiss = { deleting = null },
            onArchive = { viewModel.addAccount(account.copy(archived = true)); deleting = null },
            onKeepHistory = { viewModel.deleteAccountKeepingHistory(account); deleting = null },
            onMove = { target -> viewModel.moveAccountTransactions(account, target); deleting = null },
        )
    }
}

@Composable
private fun AccountDialog(
    account: LedgerAccount?,
    language: String,
    defaultCurrency: String,
    existingAccounts: List<LedgerAccount>,
    currencyEditable: Boolean,
    onDismiss: () -> Unit,
    onSave: (LedgerAccount) -> Unit,
    onArchive: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
) {
    var draft by remember(account?.id) { mutableStateOf(account ?: LedgerAccount(currency = defaultCurrency)) }
    var initialBalance by remember(account?.id) { mutableStateOf(if (account == null) "" else cleanNumber(account.initialBalance)) }
    var currentBalance by remember(account?.id) { mutableStateOf(if (account == null) "" else cleanNumber(account.balance)) }
    val initialValue = initialBalance.replace(",", ".").toDoubleOrNull()
    val currentValue = currentBalance.replace(",", ".").toDoubleOrNull()
    val duplicateName = LedgerRules.hasDuplicateAccountName(draft, existingAccounts)
    val valid = draft.name.isNotBlank() && draft.name.length <= LedgerConstraints.MAX_NAME_LENGTH && !duplicateName &&
        initialValue != null && LedgerConstraints.isValidBalance(initialValue) &&
        (account == null || (currentValue != null && LedgerConstraints.isValidBalance(currentValue)))
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(Modifier.fillMaxWidth().padding(20.dp).heightIn(max = 700.dp), shape = RoundedCornerShape(28.dp)) {
            Column(Modifier.verticalScroll(rememberScrollState()).padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(if (account == null) tr(language, "add_account") else tr(language, "edit"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                OutlinedTextField(
                    value = draft.name,
                    onValueChange = { draft = draft.copy(name = it.take(LedgerConstraints.MAX_NAME_LENGTH)) },
                    label = { Text(tr(language, "account_name")) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = duplicateName,
                    supportingText = if (duplicateName) ({ Text(tr(language, "duplicate_name")) }) else null,
                )
                DropdownField(draft.type, accountTypes, tr(language, "account_type"), Modifier.fillMaxWidth()) { draft = draft.copy(type = it) }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = initialBalance, onValueChange = {
                        initialBalance = it.take(32)
                        val value = initialBalance.replace(",", ".").toDoubleOrNull()?.takeIf(LedgerConstraints::isValidBalance) ?: 0.0
                        draft = if (account == null) draft.copy(initialBalance = value, balance = value) else draft.copy(initialBalance = value)
                        if (account == null) currentBalance = initialBalance
                    }, label = { Text("Initial balance") }, modifier = Modifier.weight(1.4f), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), isError = initialBalance.isNotBlank() && (initialValue == null || !LedgerConstraints.isValidBalance(initialValue)))
                    if (currencyEditable) {
                        DropdownField(draft.currency, currencies, tr(language, "currency"), Modifier.weight(1f)) { draft = draft.copy(currency = it) }
                    } else {
                        Column(Modifier.weight(1f)) {
                            Text(tr(language, "currency"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(draft.currency, fontWeight = FontWeight.Bold)
                            Text(tr(language, "account_currency_locked"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                if (account != null) {
                    OutlinedTextField(value = currentBalance, onValueChange = {
                        currentBalance = it.take(32)
                        draft = draft.copy(balance = currentBalance.replace(",", ".").toDoubleOrNull()?.takeIf(LedgerConstraints::isValidBalance) ?: 0.0)
                    }, label = { Text("Current balance") }, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), isError = currentBalance.isNotBlank() && (currentValue == null || !LedgerConstraints.isValidBalance(currentValue)))
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(tr(language, "include_assets"), Modifier.weight(1f))
                    Switch(checked = draft.includeInAssets, onCheckedChange = { draft = draft.copy(includeInAssets = it) })
                }
                if (account != null) {
                    HorizontalDivider()
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        onDelete?.let { TextButton(onClick = it) { Text(tr(language, "delete"), color = MaterialTheme.colorScheme.error) } }
                        onArchive?.let { TextButton(onClick = it) { Text(if (account.archived) "Unarchive" else tr(language, "archive")) } }
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onDismiss, Modifier.weight(1f)) { Text(tr(language, "cancel")) }
                    Button(onClick = { onSave(draft) }, enabled = valid, modifier = Modifier.weight(1f)) { Text(tr(language, "save")) }
                }
            }
        }
    }
}

@Composable
private fun AccountHistoryDialog(
    account: LedgerAccount,
    entries: List<LedgerEntry>,
    language: String,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(Modifier.fillMaxWidth().fillMaxHeight(.9f).padding(18.dp), shape = RoundedCornerShape(28.dp)) {
            Column(Modifier.fillMaxSize()) {
                Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(account.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("${account.type} · ${money(account.balance, account.currency)}${if (account.archived) " · Archived" else ""}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    TextButton(onClick = onEdit) { Text(tr(language, "edit")) }
                    TextButton(onClick = onDismiss) { Text("×") }
                }
                HorizontalDivider()
                if (entries.isEmpty()) {
                    EmptyState("No transactions", "Transactions using this account will appear here.", Modifier.weight(1f))
                } else {
                    LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(vertical = 10.dp)) {
                        items(entries.sortedByDescending { it.occurredAt }, key = { it.id }) { EntryRow(it, language, null) }
                    }
                }
            }
        }
    }
}

@Composable
private fun AccountDeleteDialog(
    account: LedgerAccount,
    targets: List<LedgerAccount>,
    usageCount: Int,
    onDismiss: () -> Unit,
    onArchive: () -> Unit,
    onKeepHistory: () -> Unit,
    onMove: (LedgerAccount) -> Unit,
) {
    var targetId by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Account is in use") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("${account.name} is used by $usageCount transaction(s). Existing transactions will never be deleted.")
                OutlinedButton(onClick = onArchive, Modifier.fillMaxWidth()) { Text("Archive account") }
                if (targets.isNotEmpty()) {
                    DropdownField(targetId, listOf("") + targets.map { it.id }, "Move transactions to", Modifier.fillMaxWidth(), display = { id -> targets.firstOrNull { it.id == id }?.name ?: "—" }) { targetId = it }
                    Button(onClick = { targets.firstOrNull { it.id == targetId }?.let(onMove) }, enabled = targetId.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text("Move transactions and delete") }
                }
                TextButton(onClick = onKeepHistory, Modifier.fillMaxWidth()) { Text("Keep transactions as Deleted Account", color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun SettingsScreen(viewModel: LedgerViewModel, state: LedgerUiState) {
    val language = state.data.settings.language
    LazyColumn(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .18f)), contentPadding = PaddingValues(bottom = 28.dp)) {
        item {
            ModernHeader {
                Row(Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 24.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(68.dp).shadow(5.dp, RoundedCornerShape(22.dp)).clip(RoundedCornerShape(22.dp)).background(MaterialTheme.colorScheme.primary), contentAlignment = Alignment.Center) {
                        LedgerIcon(AppGlyph.PROFILE, Modifier.size(32.dp), MaterialTheme.colorScheme.onPrimary)
                    }
                    Column(Modifier.padding(start = 18.dp)) {
                        Text(tr(language, "app"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(tr(language, "local_data_note"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                    }
                }
            }
        }
        item { SettingsSection(viewModel, state) }
    }
}

@Composable
private fun SettingsCard(title: String, symbol: String, content: @Composable ColumnScope.() -> Unit) {
    ElevatedCard(Modifier.fillMaxWidth(), shape = LedgerCardShape) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(38.dp).background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                    Text(symbol, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
                Text(title, modifier = Modifier.padding(start = 12.dp), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            content()
        }
    }
}

@Composable
private fun SettingsSection(viewModel: LedgerViewModel, state: LedgerUiState) {
    val settings = state.data.settings
    val language = settings.language
    val context = LocalContext.current
    var manageCategories by remember { mutableStateOf(false) }
    var confirmImport by remember { mutableStateOf(false) }
    var confirmCsvExport by remember { mutableStateOf(false) }
    var showExportPassphrase by remember { mutableStateOf(false) }
    var showImportPassphrase by remember { mutableStateOf(false) }
    var exportPassphrase by remember { mutableStateOf("") }
    var exportPassphraseConfirmation by remember { mutableStateOf("") }
    var importPassphrase by remember { mutableStateOf("") }
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }
    val exportBackupLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
        if (uri != null) viewModel.exportBackup(uri, exportPassphrase)
        exportPassphrase = ""
        exportPassphraseConfirmation = ""
    }
    val importBackupLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        pendingImportUri = uri
        showImportPassphrase = uri != null
        importPassphrase = ""
    }

    fun shareCsv() {
        val share = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_SUBJECT, "Pocket Ledger export")
            putExtra(Intent.EXTRA_TEXT, exportLedgerCsv(state.data.entries))
        }
        runCatching { context.startActivity(Intent.createChooser(share, "Export Pocket Ledger data")) }
    }
    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        SettingsCard(tr(language, "appearance"), "Aa") {
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                ThemeMode.entries.forEachIndexed { index, mode ->
                    val key = when (mode) { ThemeMode.SYSTEM -> "system"; ThemeMode.LIGHT -> "light"; ThemeMode.DARK -> "dark" }
                    SegmentedButton(selected = settings.theme == mode, onClick = { viewModel.updateSettings { it.copy(theme = mode) } }, shape = SegmentedButtonDefaults.itemShape(index, ThemeMode.entries.size)) { Text(tr(language, key), maxLines = 1) }
                }
            }
            Text(tr(language, "text_size"), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextSize.entries.forEach { size ->
                    val key = when (size) { TextSize.SMALL -> "small"; TextSize.STANDARD -> "standard"; TextSize.LARGE -> "large"; TextSize.EXTRA_LARGE -> "extra_large" }
                    FilterChip(selected = settings.textSize == size, onClick = { viewModel.updateSettings { it.copy(textSize = size) } }, label = { Text(tr(language, key)) })
                }
            }
        }
        SettingsCard(tr(language, "settings"), "◎") {
            DropdownField(settings.currency, currencies, tr(language, "currency"), Modifier.fillMaxWidth()) { currency -> viewModel.updateSettings { it.copy(currency = currency) } }
            DropdownField(
                value = settings.language, options = languages.map { it.code }, label = tr(language, "language"), modifier = Modifier.fillMaxWidth(),
                display = { code -> languages.firstOrNull { it.code == code }?.label ?: code },
                onSelected = { code -> viewModel.updateSettings { it.copy(language = code) } }
            )
        }
        SettingsCard(tr(language, "categories"), "#") {
            FilledTonalButton(onClick = { manageCategories = true }, modifier = Modifier.fillMaxWidth()) { Text(tr(language, "manage_categories")) }
            Text("${tr(language, "accounts")}: ${tr(language, "report")} → ${tr(language, "accounts")}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        SettingsCard(tr(language, "backup_restore"), "⇅") {
            Button(onClick = { showExportPassphrase = true }, modifier = Modifier.fillMaxWidth()) { Text(tr(language, "export_backup")) }
            OutlinedButton(onClick = { confirmImport = true }, modifier = Modifier.fillMaxWidth()) { Text(tr(language, "import_backup")) }
            OutlinedButton(onClick = { confirmCsvExport = true }, modifier = Modifier.fillMaxWidth()) { Text(tr(language, "export_csv")) }
            Text(tr(language, "backup_note"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        SettingsCard(tr(language, "privacy_data"), "i") {
            Text(tr(language, "local_data_note"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Pocket Ledger ${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.labelSmall)
        }
    }
    if (confirmImport) {
        AlertDialog(
            onDismissRequest = { confirmImport = false },
            title = { Text(tr(language, "import_backup")) },
            text = { Text(tr(language, "import_warning")) },
            confirmButton = {
                Button(onClick = {
                    confirmImport = false
                    importBackupLauncher.launch(arrayOf("application/json", "text/plain", "application/octet-stream"))
                }) { Text(tr(language, "continue_import")) }
            },
            dismissButton = { TextButton(onClick = { confirmImport = false }) { Text(tr(language, "cancel")) } },
        )
    }
    if (showExportPassphrase) {
        AlertDialog(
            onDismissRequest = { showExportPassphrase = false; exportPassphrase = ""; exportPassphraseConfirmation = "" },
            title = { Text(tr(language, "backup_password_title")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(tr(language, "backup_password_remember"))
                    OutlinedTextField(
                        value = exportPassphrase,
                        onValueChange = { exportPassphrase = it.take(256) },
                        label = { Text(tr(language, "backup_password_hint")) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = exportPassphraseConfirmation,
                        onValueChange = { exportPassphraseConfirmation = it.take(256) },
                        label = { Text(tr(language, "backup_password_confirm")) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        isError = exportPassphraseConfirmation.isNotEmpty() && exportPassphrase != exportPassphraseConfirmation,
                    )
                    if (exportPassphraseConfirmation.isNotEmpty() && exportPassphrase != exportPassphraseConfirmation) {
                        Text(tr(language, "backup_password_mismatch"), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                    Text(tr(language, "backup_password_requirement"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                Button(
                    enabled = exportPassphrase.length >= BackupCrypto.MIN_PASSPHRASE_LENGTH && exportPassphrase == exportPassphraseConfirmation,
                    onClick = {
                        showExportPassphrase = false
                        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmm"))
                        exportBackupLauncher.launch("PocketLedger-backup-$timestamp.plbackup")
                    },
                ) { Text(tr(language, "continue_export")) }
            },
            dismissButton = { TextButton(onClick = { showExportPassphrase = false; exportPassphrase = ""; exportPassphraseConfirmation = "" }) { Text(tr(language, "cancel")) } },
        )
    }
    if (showImportPassphrase) {
        AlertDialog(
            onDismissRequest = { showImportPassphrase = false; pendingImportUri = null; importPassphrase = "" },
            title = { Text(tr(language, "decrypt_backup")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(tr(language, "legacy_backup_hint"))
                    OutlinedTextField(
                        value = importPassphrase,
                        onValueChange = { importPassphrase = it.take(256) },
                        label = { Text(tr(language, "backup_password_hint")) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    pendingImportUri?.let { viewModel.importBackup(it, importPassphrase) }
                    showImportPassphrase = false
                    pendingImportUri = null
                    importPassphrase = ""
                }) { Text(tr(language, "continue_import")) }
            },
            dismissButton = { TextButton(onClick = { showImportPassphrase = false; pendingImportUri = null; importPassphrase = "" }) { Text(tr(language, "cancel")) } },
        )
    }
    if (confirmCsvExport) {
        AlertDialog(
            onDismissRequest = { confirmCsvExport = false },
            title = { Text(tr(language, "export_csv")) },
            text = { Text(tr(language, "csv_warning")) },
            confirmButton = { Button(onClick = { confirmCsvExport = false; shareCsv() }) { Text(tr(language, "continue_export")) } },
            dismissButton = { TextButton(onClick = { confirmCsvExport = false }) { Text(tr(language, "cancel")) } },
        )
    }
    if (manageCategories) CategoryManagerDialog(viewModel, state, onDismiss = { manageCategories = false })
}

@Composable
private fun AccountTotalMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun AccountListRow(account: LedgerAccount, recent: LedgerEntry?, language: String, managing: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        color = if (account.archived) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .45f) else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(18.dp),
        tonalElevation = 1.dp,
    ) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(46.dp).background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) { Text(accountSymbol(account.type)) }
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(account.name, fontWeight = FontWeight.Bold)
                Text(
                    listOfNotNull(recent?.purpose, if (!account.includeInAssets) tr(language, "excluded_assets") else null).joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${account.currency} ${cleanNumber(account.balance)}", fontWeight = FontWeight.Bold, color = if (account.balance < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                Text(if (managing) tr(language, "edit") else "›", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun CategoryManagerDialog(viewModel: LedgerViewModel, state: LedgerUiState, onDismiss: () -> Unit) {
    val language = state.data.settings.language
    var editing by remember { mutableStateOf<LedgerCategory?>(null) }
    var creating by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<LedgerCategory?>(null) }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(Modifier.fillMaxWidth().fillMaxHeight(.92f).padding(16.dp), shape = RoundedCornerShape(28.dp)) {
            Column(Modifier.fillMaxSize()) {
                Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(tr(language, "manage_categories"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    FilledTonalButton(onClick = { creating = true }) { Text("+ Add") }
                    TextButton(onClick = onDismiss) { Text("×") }
                }
                LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
                    listOf(EntryType.EXPENSE to "Expense Categories", EntryType.INCOME to "Income Categories").forEach { (type, title) ->
                        item { Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 12.dp)) }
                        items(state.data.categories.filter { it.type == type }.sortedWith(compareBy<LedgerCategory> { it.archived }.thenBy { it.name }), key = { it.id }) { category ->
                            ListItem(
                                headlineContent = { Text(category.name, fontWeight = FontWeight.SemiBold) },
                                supportingContent = { Text(if (category.archived) "Archived" else "Available for new transactions") },
                                leadingContent = { Box(Modifier.size(38.dp).background(storedCategoryColor(category.color).copy(alpha = .16f), CircleShape), contentAlignment = Alignment.Center) { Text(category.icon) } },
                                trailingContent = { TextButton(onClick = { editing = category }) { Text("Edit") } },
                                modifier = Modifier.fillMaxWidth(),
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
    if (creating) CategoryEditDialog(
        category = null,
        existingCategories = state.data.categories,
        typeEditable = true,
        onDismiss = { creating = false },
        onSave = { if (viewModel.addCategory(it)) creating = false },
    )
    editing?.let { category ->
        CategoryEditDialog(
            category,
            existingCategories = state.data.categories,
            typeEditable = LedgerRules.canChangeCategoryType(category.id, state.data.entries),
            onDismiss = { editing = null },
            onSave = { if (viewModel.addCategory(it)) editing = null },
            onArchive = { viewModel.archiveCategory(category); editing = null },
            onDelete = {
                if (!viewModel.deleteUnusedCategory(category)) deleting = category
                editing = null
            },
        )
    }
    deleting?.let { category ->
        CategoryDeleteDialog(
            category = category,
            targets = state.data.categories.filter { it.id != category.id && it.type == category.type && !it.archived },
            usageCount = viewModel.categoryUsageCount(category.id),
            onDismiss = { deleting = null },
            onArchive = { viewModel.archiveCategory(category); deleting = null },
            onKeepHistory = { viewModel.deleteCategoryKeepingHistory(category); deleting = null },
            onMove = { target -> viewModel.moveCategoryTransactions(category, target); deleting = null },
        )
    }
}

@Composable
private fun CategoryEditDialog(
    category: LedgerCategory?,
    existingCategories: List<LedgerCategory>,
    typeEditable: Boolean,
    onDismiss: () -> Unit,
    onSave: (LedgerCategory) -> Unit,
    onArchive: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
) {
    var draft by remember(category?.id) { mutableStateOf(category ?: LedgerCategory()) }
    val duplicateName = LedgerRules.hasDuplicateCategoryName(draft, existingCategories)
    val valid = draft.name.isNotBlank() && draft.name.length <= LedgerConstraints.MAX_NAME_LENGTH && !duplicateName
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (category == null) "Add Category" else "Edit Category") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = draft.name,
                    onValueChange = { draft = draft.copy(name = it.take(LedgerConstraints.MAX_NAME_LENGTH)) },
                    label = { Text("Category name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = duplicateName,
                    supportingText = if (duplicateName) ({ Text("Category names must be unique.") }) else null,
                )
                if (typeEditable) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = draft.type == EntryType.EXPENSE, onClick = { draft = draft.copy(type = EntryType.EXPENSE) }, label = { Text("Expense") })
                        FilterChip(selected = draft.type == EntryType.INCOME, onClick = { draft = draft.copy(type = EntryType.INCOME) }, label = { Text("Income") })
                    }
                } else {
                    Text(if (draft.type == EntryType.EXPENSE) "Expense · locked after the first transaction" else "Income · locked after the first transaction", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                OutlinedTextField(value = draft.icon, onValueChange = { draft = draft.copy(icon = it.take(3)) }, label = { Text("Icon") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Text("Color", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf(0xFFE99542, 0xFFE26A78, 0xFF6B7FD7, 0xFF3B9BA5, 0xFFAC6AC7, 0xFF16865B).forEach { colorValue ->
                        Surface(
                            modifier = Modifier.size(if (draft.color == colorValue) 38.dp else 32.dp).clickable { draft = draft.copy(color = colorValue) },
                            shape = CircleShape,
                            color = storedCategoryColor(colorValue),
                            tonalElevation = if (draft.color == colorValue) 6.dp else 0.dp,
                        ) {}
                    }
                }
                if (category != null) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        onDelete?.let { TextButton(onClick = it) { Text("Delete", color = MaterialTheme.colorScheme.error) } }
                        onArchive?.let { TextButton(onClick = it) { Text(if (category.archived) "Unarchive" else "Archive") } }
                    }
                }
            }
        },
        confirmButton = { Button(onClick = { onSave(draft) }, enabled = valid) { Text(if (category == null) "Add" else "Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun CategoryDeleteDialog(
    category: LedgerCategory,
    targets: List<LedgerCategory>,
    usageCount: Int,
    onDismiss: () -> Unit,
    onArchive: () -> Unit,
    onKeepHistory: () -> Unit,
    onMove: (LedgerCategory) -> Unit,
) {
    var targetId by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Category is in use") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("${category.name} is used by $usageCount transaction(s). Existing transactions will be preserved.")
                OutlinedButton(onClick = onArchive, Modifier.fillMaxWidth()) { Text("Archive category") }
                if (targets.isNotEmpty()) {
                    DropdownField(targetId, listOf("") + targets.map { it.id }, "Move transactions to", Modifier.fillMaxWidth(), display = { id -> targets.firstOrNull { it.id == id }?.name ?: "—" }) { targetId = it }
                    Button(onClick = { targets.firstOrNull { it.id == targetId }?.let(onMove) }, enabled = targetId.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text("Move transactions and delete") }
                }
                TextButton(onClick = onKeepHistory, Modifier.fillMaxWidth()) { Text("Keep historical category name and delete", color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun EmptyState(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("◌", style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(8.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(8.dp), textAlign = TextAlign.Center)
    }
}

private fun money(amount: Double, currency: String): String {
    val symbols = mapOf("CNY" to "¥", "HKD" to "HK$", "USD" to "$", "EUR" to "€", "GBP" to "£", "JPY" to "¥", "KRW" to "₩", "SGD" to "S$", "AUD" to "A$", "CAD" to "C$")
    val formatter = NumberFormat.getNumberInstance().apply { minimumFractionDigits = if (amount % 1.0 == 0.0) 0 else 2; maximumFractionDigits = 2 }
    val sign = if (amount < 0) "−" else ""
    return "$sign${symbols[currency] ?: "$currency "}${formatter.format(kotlin.math.abs(amount))}"
}

private fun cleanNumber(value: Double): String = if (value % 1.0 == 0.0) value.toLong().toString() else "%.2f".format(Locale.ROOT, value)

private fun formatDateTime(epoch: Long): String = Instant.ofEpochMilli(epoch).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT))

@Composable
private fun statisticsPeriodTabLabel(language: String, range: String, period: StatisticsPeriod, now: LocalDate): String {
    val current = LedgerAnalytics.periodContaining(range, now)
    val previousStart = when (range) {
        "week" -> current.start.minusWeeks(1)
        "year" -> current.start.minusYears(1)
        else -> current.start.minusMonths(1)
    }
    if (period.start == current.start) {
        return tr(language, when (range) { "week" -> "this_week"; "year" -> "this_year"; else -> "this_month" })
    }
    if (period.start == previousStart) {
        return tr(language, when (range) { "week" -> "last_week"; "year" -> "last_year"; else -> "last_month" })
    }
    val locale = statisticsLocale(language)
    return when (range) {
        "week" -> {
            val week = period.start.get(WeekFields.ISO.weekOfWeekBasedYear())
            val weekYear = period.start.get(WeekFields.ISO.weekBasedYear())
            when (language) {
                "zh-TW" -> if (weekYear == now.year) "第${week}週" else "${weekYear}年第${week}週"
                "zh-CN" -> if (weekYear == now.year) "第${week}周" else "${weekYear}年第${week}周"
                else -> if (weekYear == now.year) "W$week" else "$weekYear W$week"
            }
        }
        "year" -> period.start.year.toString()
        else -> when (language) {
            "zh-TW", "zh-CN" -> if (period.start.year == now.year) "${period.start.monthValue}月" else "${period.start.year}年${period.start.monthValue}月"
            else -> period.start.format(DateTimeFormatter.ofPattern(if (period.start.year == now.year) "MMM" else "MMM yyyy", locale))
        }
    }
}

private fun formatStatisticsPeriod(language: String, range: String, period: StatisticsPeriod): String {
    val end = period.endExclusive.minusDays(1)
    val locale = statisticsLocale(language)
    return when (range) {
        "week" -> when (language) {
            "zh-TW", "zh-CN" -> "${period.start.monthValue}月${period.start.dayOfMonth}日－${end.monthValue}月${end.dayOfMonth}日"
            else -> "${period.start.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale))} – ${end.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale))}"
        }
        "year" -> when (language) { "zh-TW", "zh-CN" -> "${period.start.year}年"; else -> period.start.year.toString() }
        else -> when (language) {
            "zh-TW", "zh-CN" -> "${period.start.year}年${period.start.monthValue}月"
            else -> period.start.format(DateTimeFormatter.ofPattern("MMMM yyyy", locale))
        }
    }
}

private fun statisticsLocale(language: String): Locale = when (language) {
    "zh-TW" -> Locale.TRADITIONAL_CHINESE
    "zh-CN" -> Locale.SIMPLIFIED_CHINESE
    else -> Locale.forLanguageTag(language)
}

private fun localizedTrendLabel(language: String, label: String): String {
    if (!label.startsWith("W") || label.length == 1) return label
    val week = label.drop(1)
    return when (language) {
        "zh-TW" -> "第${week}週"
        "zh-CN" -> "第${week}周"
        else -> "W$week"
    }
}

internal fun storedCategoryColor(argb: Long): Color = Color(argb)

private fun categoryColor(category: String): Color = when (category) {
    "Food" -> Color(0xFFE99542); "Health" -> Color(0xFFE26A78); "Education" -> Color(0xFF6B7FD7); "Transport" -> Color(0xFF3B9BA5)
    "Shopping" -> Color(0xFFAC6AC7); "Entertainment" -> Color(0xFFCB5F95); "Housing" -> Color(0xFF8D7456); "Travel" -> Color(0xFF4B8ACA)
    "Communication" -> Color(0xFF5A8F80); "Subscriptions" -> Color(0xFF806FC1); "Salary" -> Color(0xFF16865B); "Scholarship" -> Color(0xFF2F9E8F)
    "Refund" -> Color(0xFF5B8C5A); "Transfer In" -> Color(0xFF3B82A0); "Investment Income", "Investment" -> Color(0xFF8A6D3B); "Part-time Job" -> Color(0xFF6E7D3A)
    else -> Color(DEFAULT_CATEGORY_COLOR)
}

private fun categorySymbol(category: String): String = when (category) {
    "Food" -> "●"; "Health" -> "+"; "Education" -> "A"; "Transport" -> "→"; "Shopping" -> "◇"; "Entertainment" -> "☆";
    "Housing" -> "⌂"; "Travel" -> "✦"; "Communication" -> "⌁"; "Salary", "Scholarship", "Refund", "Investment" -> "+"; else -> "·"
}

private fun accountSymbol(type: String): String = when (type) {
    "Cash" -> "¤"; "Bank Account" -> "▣"; "Credit Card" -> "▭"; "E-wallet" -> "◫"; "Transport Card" -> "↔"; "Investment Account" -> "↗"; else -> "○"
}
