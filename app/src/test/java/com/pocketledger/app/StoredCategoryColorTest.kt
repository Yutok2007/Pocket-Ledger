package com.pocketledger.app

import androidx.compose.ui.graphics.toArgb
import org.junit.Assert.assertEquals
import org.junit.Test

class StoredCategoryColorTest {
    @Test
    fun storedArgbColorCanBeRenderedAndHaveItsAlphaChanged() {
        val color = storedCategoryColor(0xFF718078)

        assertEquals(0xFF718078.toInt(), color.toArgb())
        assertEquals(0.18f, color.copy(alpha = 0.18f).alpha, 0.001f)
    }
}
