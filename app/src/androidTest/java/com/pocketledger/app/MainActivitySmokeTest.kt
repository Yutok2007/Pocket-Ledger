package com.pocketledger.app

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class MainActivitySmokeTest {
    @get:Rule val composeRule = createAndroidComposeRule<MainActivity>()

    @Test fun bottomNavigationAndAddTransactionOpen() {
        composeRule.onNodeWithTag("nav_add").performClick()
        composeRule.onNodeWithTag("nav_add").assertDoesNotExist()
    }

    @Test fun homeToolbarOpensSearchFiltersAndMonthPicker() {
        composeRule.onNodeWithTag("home_search").performClick()
        composeRule.onNodeWithTag("home_search_field").fetchSemanticsNode()
        composeRule.onNodeWithTag("home_filters").performClick()
        composeRule.onNodeWithTag("home_filter_strip").fetchSemanticsNode()
        composeRule.onNodeWithTag("home_month").performClick()
        composeRule.onNodeWithTag("month_picker").fetchSemanticsNode()
    }

    @Test fun statisticsTypePeriodAndRangeControlsAreInteractive() {
        composeRule.onNodeWithTag("nav_chart").performClick()
        composeRule.onNodeWithTag("statistics_type_selector").performClick()
        composeRule.onNodeWithTag("statistics_type_INCOME").performClick()
        composeRule.onNodeWithTag("statistics_range_week").performClick()

        composeRule.onNodeWithTag("statistics_period_selector").fetchSemanticsNode()
    }
}
