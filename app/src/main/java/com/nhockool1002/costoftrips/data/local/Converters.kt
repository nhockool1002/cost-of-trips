package com.nhockool1002.costoftrips.data.local

import androidx.room.TypeConverter
import com.nhockool1002.costoftrips.data.local.entity.ExpenseCategory

class Converters {
    @TypeConverter
    fun fromExpenseCategory(category: ExpenseCategory): String = category.name

    @TypeConverter
    fun toExpenseCategory(value: String): ExpenseCategory = ExpenseCategory.valueOf(value)
}
