package com.nhockool1002.costoftrips.data.local

import com.nhockool1002.costoftrips.data.local.entity.ExpenseCategory
import org.junit.Assert.assertEquals
import org.junit.Test

class ConvertersTest {

    private val converters = Converters()

    @Test
    fun `every category round-trips through the converter`() {
        ExpenseCategory.entries.forEach { category ->
            val stored = converters.fromExpenseCategory(category)
            assertEquals(category, converters.toExpenseCategory(stored))
        }
    }

    @Test
    fun `fromExpenseCategory stores the enum name`() {
        assertEquals("FOOD", converters.fromExpenseCategory(ExpenseCategory.FOOD))
    }
}
