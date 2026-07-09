package com.nhockool1002.costoftrips.ui.screens.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.nhockool1002.costoftrips.R
import com.nhockool1002.costoftrips.data.local.entity.ExpenseCategory
import com.nhockool1002.costoftrips.ui.theme.CategoryAccommodationBg
import com.nhockool1002.costoftrips.ui.theme.CategoryEntertainmentBg
import com.nhockool1002.costoftrips.ui.theme.CategoryFoodBg
import com.nhockool1002.costoftrips.ui.theme.CategoryOtherBg
import com.nhockool1002.costoftrips.ui.theme.CategoryShoppingBg
import com.nhockool1002.costoftrips.ui.theme.CategoryTransportBg

@Composable
fun ExpenseCategory.displayName(): String = stringResource(
    when (this) {
        ExpenseCategory.TRANSPORT -> R.string.category_transport
        ExpenseCategory.ACCOMMODATION -> R.string.category_accommodation
        ExpenseCategory.FOOD -> R.string.category_food
        ExpenseCategory.ENTERTAINMENT -> R.string.category_entertainment
        ExpenseCategory.SHOPPING -> R.string.category_shopping
        ExpenseCategory.OTHER -> R.string.category_other
    }
)

fun ExpenseCategory.emoji(): String = when (this) {
    ExpenseCategory.TRANSPORT -> "🚗"
    ExpenseCategory.ACCOMMODATION -> "🏨"
    ExpenseCategory.FOOD -> "🍜"
    ExpenseCategory.ENTERTAINMENT -> "🎉"
    ExpenseCategory.SHOPPING -> "🛍️"
    ExpenseCategory.OTHER -> "📦"
}

fun ExpenseCategory.badgeColor(): Color = when (this) {
    ExpenseCategory.TRANSPORT -> CategoryTransportBg
    ExpenseCategory.ACCOMMODATION -> CategoryAccommodationBg
    ExpenseCategory.FOOD -> CategoryFoodBg
    ExpenseCategory.ENTERTAINMENT -> CategoryEntertainmentBg
    ExpenseCategory.SHOPPING -> CategoryShoppingBg
    ExpenseCategory.OTHER -> CategoryOtherBg
}
