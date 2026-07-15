package com.pocketledger.app.i18n

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import java.util.Locale

data class LanguageOption(val code: String, val label: String)

val languages = listOf(
    LanguageOption("zh-TW", "繁體中文"), LanguageOption("zh-CN", "简体中文"), LanguageOption("en", "English"),
    LanguageOption("ja", "日本語"), LanguageOption("ko", "한국어"), LanguageOption("es", "Español"),
    LanguageOption("fr", "Français"), LanguageOption("de", "Deutsch"), LanguageOption("pt", "Português"), LanguageOption("ar", "العربية")
)

private val en = mapOf(
    "app" to "Pocket Ledger", "home" to "Home", "chart" to "Chart", "report" to "Report", "profile" to "Profile",
    "income" to "Income", "expense" to "Expense", "balance" to "Balance", "this_month" to "This month", "no_entries" to "No entries yet",
    "add_first" to "Tap + to add your first transaction", "search" to "Search purpose, note, category, or account", "all" to "All",
    "add_entry" to "Add transaction", "sentence" to "One sentence", "manual" to "Manual", "parse" to "Parse", "speak" to "Speak",
    "sentence_hint" to "e.g. Yesterday I spent 32 yuan on lunch using WeChat", "confirm" to "Review and save", "type" to "Type",
    "amount" to "Amount", "currency" to "Currency", "purpose" to "Purpose", "category" to "Category", "account" to "Account",
    "date_time" to "Date and time", "note" to "Note", "cancel" to "Cancel", "save" to "Save", "delete" to "Delete", "edit" to "Edit",
    "required" to "Complete the amount and purpose before saving", "week" to "Week", "month" to "Month", "year" to "Year",
    "trend" to "Income and expense trend", "categories" to "Category breakdown", "analytics" to "Analytics", "accounts" to "Accounts",
    "week_spend" to "This week spent", "month_spend" to "This month spent", "year_spend" to "This year spent", "budget" to "Monthly budget",
    "used" to "Used", "remaining" to "Remaining", "over_budget" to "Over budget", "set_budget" to "Set budget", "add_account" to "Add account",
    "account_name" to "Account name", "account_type" to "Account type", "include_assets" to "Include in total assets", "archive" to "Archive",
    "settings" to "Settings", "appearance" to "Appearance",
    "system" to "System", "light" to "Light", "dark" to "Dark", "text_size" to "Text size", "language" to "Language",
    "small" to "Small", "standard" to "Standard", "large" to "Large", "extra_large" to "Extra large", "continue" to "Continue",
    "top_category" to "Top category", "net" to "Net", "no_data" to "Add transactions to see insights", "save_success" to "Transaction saved",
    "filters" to "Filters", "clear" to "Clear", "total_assets" to "Total assets", "close" to "Close",
    "this_week" to "This week", "last_week" to "Last week", "last_month" to "Last month", "this_year" to "This year", "last_year" to "Last year",
    "total_expense" to "Total expense", "total_income" to "Total income", "category_details" to "Category details",
    "expense_trend" to "Expense trend", "income_trend" to "Income trend"
)

private fun translated(vararg values: Pair<String, String>) = en + mapOf(*values)

private val bundles = mapOf(
    "en" to en,
    "zh-TW" to translated(
        "app" to "隨身帳本", "home" to "首頁", "chart" to "圖表", "report" to "報告", "profile" to "我的", "income" to "收入", "expense" to "支出",
        "balance" to "餘額", "this_month" to "本月", "no_entries" to "還沒有帳目", "add_first" to "點擊＋新增第一筆帳目", "search" to "搜尋用途、備註、分類或帳戶", "all" to "全部",
        "add_entry" to "新增帳目", "sentence" to "一句話記帳", "manual" to "手動記帳", "parse" to "解析", "speak" to "語音", "sentence_hint" to "例如：昨天用微信買午飯，花了 32 元",
        "confirm" to "確認並保存", "type" to "類型", "amount" to "金額", "currency" to "貨幣", "purpose" to "用途", "category" to "分類", "account" to "帳戶",
        "date_time" to "日期與時間", "note" to "備註", "cancel" to "取消", "save" to "保存", "delete" to "刪除", "edit" to "修改", "required" to "保存前請填寫金額和用途",
        "week" to "週", "month" to "月", "year" to "年", "trend" to "收支趨勢", "categories" to "分類統計", "analytics" to "分析", "accounts" to "帳戶",
        "week_spend" to "本週消費", "month_spend" to "本月消費", "year_spend" to "本年消費", "budget" to "每月預算", "used" to "已使用", "remaining" to "剩餘", "over_budget" to "已超出預算",
        "set_budget" to "設定預算", "add_account" to "新增帳戶", "account_name" to "帳戶名稱", "account_type" to "帳戶類型", "include_assets" to "計入總資產", "archive" to "封存",
        "settings" to "設定", "appearance" to "外觀", "system" to "跟隨系統", "light" to "亮色", "dark" to "暗色",
        "text_size" to "文字大小", "language" to "語言", "small" to "小", "standard" to "標準", "large" to "大", "extra_large" to "特大",
        "continue" to "繼續", "top_category" to "最高分類", "net" to "淨餘額", "no_data" to "新增帳目後即可查看分析", "save_success" to "帳目已保存",
        "filters" to "篩選", "clear" to "清除", "total_assets" to "總資產", "close" to "關閉",
        "this_week" to "本週", "last_week" to "上週", "last_month" to "上月", "this_year" to "本年", "last_year" to "上年",
        "total_expense" to "支出總額", "total_income" to "收入總額", "category_details" to "分類明細", "expense_trend" to "支出趨勢", "income_trend" to "收入趨勢"
    ),
    "zh-CN" to translated(
        "app" to "随身账本", "home" to "首页", "chart" to "图表", "report" to "报告", "profile" to "我的", "income" to "收入", "expense" to "支出", "balance" to "余额",
        "this_month" to "本月", "no_entries" to "还没有账目", "add_first" to "点击＋新增第一笔账目", "search" to "搜索用途、备注、分类或账户", "all" to "全部", "add_entry" to "新增账目",
        "sentence" to "一句话记账", "manual" to "手动记账", "parse" to "解析", "speak" to "语音", "confirm" to "确认并保存", "type" to "类型", "amount" to "金额", "currency" to "货币",
        "purpose" to "用途", "category" to "分类", "account" to "账户", "date_time" to "日期与时间", "note" to "备注", "cancel" to "取消", "save" to "保存", "delete" to "删除", "edit" to "修改",
        "week" to "周", "month" to "月", "year" to "年", "trend" to "收支趋势", "categories" to "分类统计", "analytics" to "分析", "accounts" to "账户", "budget" to "每月预算",
        "used" to "已使用", "remaining" to "剩余", "set_budget" to "设置预算", "add_account" to "新增账户", "settings" to "设置",
        "appearance" to "外观", "system" to "跟随系统", "light" to "浅色", "dark" to "深色", "text_size" to "文字大小", "language" to "语言", "close" to "关闭",
        "this_week" to "本周", "last_week" to "上周", "this_month" to "本月", "last_month" to "上月", "this_year" to "本年", "last_year" to "上年",
        "total_expense" to "支出总额", "total_income" to "收入总额", "category_details" to "分类明细", "expense_trend" to "支出趋势", "income_trend" to "收入趋势"
    ),
    "ja" to translated("home" to "ホーム", "chart" to "チャート", "report" to "レポート", "profile" to "プロフィール", "income" to "収入", "expense" to "支出", "balance" to "残高", "add_entry" to "取引を追加", "sentence" to "一文で入力", "manual" to "手動入力", "parse" to "解析", "save" to "保存", "cancel" to "キャンセル", "week" to "週", "month" to "月", "year" to "年", "analytics" to "分析", "accounts" to "口座", "budget" to "月間予算", "settings" to "設定", "language" to "言語", "currency" to "通貨"),
    "ko" to translated("home" to "홈", "chart" to "차트", "report" to "보고서", "profile" to "프로필", "income" to "수입", "expense" to "지출", "balance" to "잔액", "add_entry" to "거래 추가", "sentence" to "한 문장 입력", "manual" to "직접 입력", "parse" to "분석", "save" to "저장", "cancel" to "취소", "week" to "주", "month" to "월", "year" to "년", "analytics" to "분석", "accounts" to "계좌", "budget" to "월 예산", "settings" to "설정", "language" to "언어", "currency" to "통화"),
    "es" to translated("home" to "Inicio", "chart" to "Gráficos", "report" to "Informe", "profile" to "Perfil", "income" to "Ingresos", "expense" to "Gastos", "balance" to "Saldo", "add_entry" to "Añadir movimiento", "sentence" to "Una frase", "manual" to "Manual", "parse" to "Analizar", "save" to "Guardar", "cancel" to "Cancelar", "week" to "Semana", "month" to "Mes", "year" to "Año", "analytics" to "Análisis", "accounts" to "Cuentas", "budget" to "Presupuesto mensual", "settings" to "Ajustes", "language" to "Idioma", "currency" to "Moneda"),
    "fr" to translated("home" to "Accueil", "chart" to "Graphiques", "report" to "Rapport", "profile" to "Profil", "income" to "Revenus", "expense" to "Dépenses", "balance" to "Solde", "add_entry" to "Ajouter une opération", "sentence" to "Une phrase", "manual" to "Manuel", "parse" to "Analyser", "save" to "Enregistrer", "cancel" to "Annuler", "week" to "Semaine", "month" to "Mois", "year" to "Année", "analytics" to "Analyse", "accounts" to "Comptes", "budget" to "Budget mensuel", "settings" to "Paramètres", "language" to "Langue", "currency" to "Devise"),
    "de" to translated("home" to "Start", "chart" to "Diagramm", "report" to "Bericht", "profile" to "Profil", "income" to "Einnahmen", "expense" to "Ausgaben", "balance" to "Saldo", "add_entry" to "Buchung hinzufügen", "sentence" to "Ein Satz", "manual" to "Manuell", "parse" to "Analysieren", "save" to "Speichern", "cancel" to "Abbrechen", "week" to "Woche", "month" to "Monat", "year" to "Jahr", "analytics" to "Analyse", "accounts" to "Konten", "budget" to "Monatsbudget", "settings" to "Einstellungen", "language" to "Sprache", "currency" to "Währung"),
    "pt" to translated("home" to "Início", "chart" to "Gráficos", "report" to "Relatório", "profile" to "Perfil", "income" to "Receitas", "expense" to "Despesas", "balance" to "Saldo", "add_entry" to "Adicionar transação", "sentence" to "Uma frase", "manual" to "Manual", "parse" to "Analisar", "save" to "Guardar", "cancel" to "Cancelar", "week" to "Semana", "month" to "Mês", "year" to "Ano", "analytics" to "Análises", "accounts" to "Contas", "budget" to "Orçamento mensal", "settings" to "Definições", "language" to "Idioma", "currency" to "Moeda"),
    "ar" to translated("home" to "الرئيسية", "chart" to "الرسوم", "report" to "التقرير", "profile" to "الملف", "income" to "الدخل", "expense" to "المصروف", "balance" to "الرصيد", "add_entry" to "إضافة معاملة", "sentence" to "جملة واحدة", "manual" to "يدوي", "parse" to "تحليل", "save" to "حفظ", "cancel" to "إلغاء", "week" to "أسبوع", "month" to "شهر", "year" to "سنة", "analytics" to "التحليلات", "accounts" to "الحسابات", "budget" to "الميزانية الشهرية", "settings" to "الإعدادات", "language" to "اللغة", "currency" to "العملة")
)

@Composable
fun tr(language: String, key: String): String {
    val context = LocalContext.current
    val resources = LocalResources.current
    val currentConfiguration = LocalConfiguration.current
    val resourceName = if (key == "continue") "continue_action" else key
    val resourceId = resources.getIdentifier(resourceName, "string", context.packageName)
    if (resourceId != 0) {
        val configuration = Configuration(currentConfiguration).apply { setLocale(Locale.forLanguageTag(language)) }
        return context.createConfigurationContext(configuration).resources.getString(resourceId)
    }
    return bundles[language]?.get(key) ?: en[key] ?: key
}

private val localizedCategories = mapOf(
    "zh-TW" to listOf("食品", "健康", "教育", "交通", "購物", "娛樂", "居住", "旅行", "通訊", "訂閱", "其他", "工資", "獎學金", "退款", "轉入", "投資收入", "兼職", "其他收入"),
    "zh-CN" to listOf("食品", "健康", "教育", "交通", "购物", "娱乐", "居住", "旅行", "通信", "订阅", "其他", "工资", "奖学金", "退款", "转入", "投资收入", "兼职", "其他收入"),
    "ja" to listOf("食費", "健康", "教育", "交通", "買い物", "娯楽", "住居", "旅行", "通信", "購読", "その他", "給与", "奨学金", "返金", "振替", "投資収入", "アルバイト", "その他収入"),
    "ko" to listOf("식비", "건강", "교육", "교통", "쇼핑", "오락", "주거", "여행", "통신", "구독", "기타", "급여", "장학금", "환불", "이체", "투자 수입", "아르바이트", "기타 수입")
)
private val categoryKeys = listOf("Food", "Health", "Education", "Transport", "Shopping", "Entertainment", "Housing", "Travel", "Communication", "Subscriptions", "Other", "Salary", "Scholarship", "Refund", "Transfer In", "Investment Income", "Part-time Job", "Other Income")

fun categoryLabel(language: String, category: String): String {
    val index = categoryKeys.indexOf(category)
    return if (index >= 0) localizedCategories[language]?.getOrNull(index) ?: category else category
}
