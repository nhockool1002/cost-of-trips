package com.nhockool1002.costoftrips.ui.screens.addexpense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhockool1002.costoftrips.data.local.entity.Expense
import com.nhockool1002.costoftrips.data.local.entity.ExpenseCategory
import com.nhockool1002.costoftrips.data.local.entity.TripMember
import com.nhockool1002.costoftrips.data.repository.TripRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AddExpenseViewModel(
    private val repository: TripRepository,
    private val tripId: Long
) : ViewModel() {

    val members: StateFlow<List<TripMember>> = repository.observeMembersForTrip(tripId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addExpense(
        category: ExpenseCategory,
        amount: Double,
        note: String,
        paidByMemberId: Long?,
        splitMemberIds: List<Long>,
        onSaved: () -> Unit
    ) {
        if (amount <= 0.0) return
        viewModelScope.launch {
            repository.addExpense(
                Expense(
                    tripId = tripId,
                    category = category,
                    amount = amount,
                    note = note.trim(),
                    paidByMemberId = paidByMemberId
                ),
                splitMemberIds = splitMemberIds
            )
            onSaved()
        }
    }
}
