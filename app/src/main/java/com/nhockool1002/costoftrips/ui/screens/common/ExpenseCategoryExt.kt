package com.nhockool1002.costoftrips.ui.screens.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.nhockool1002.costoftrips.R
import com.nhockool1002.costoftrips.data.local.entity.ExpenseCategory

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
