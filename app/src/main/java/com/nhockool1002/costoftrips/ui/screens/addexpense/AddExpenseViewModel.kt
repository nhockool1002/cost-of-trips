package com.nhockool1002.costoftrips.ui.screens.addexpense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhockool1002.costoftrips.data.local.entity.Expense
import com.nhockool1002.costoftrips.data.local.entity.ExpenseCategory
import com.nhockool1002.costoftrips.data.repository.TripRepository
import kotlinx.coroutines.launch

class AddExpenseViewModel(
    private val repository: TripRepository,
    private val tripId: Long
) : ViewModel() {

    fun addExpense(
        category: ExpenseCategory,
        amount: Double,
        note: String,
        onSaved: () -> Unit
    ) {
        if (amount <= 0.0) return
        viewModelScope.launch {
            repository.addExpense(
                Expense(
                    tripId = tripId,
                    category = category,
                    amount = amount,
                    note = note.trim()
                )
            )
            onSaved()
        }
    }
}
