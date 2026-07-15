package com.nhockool1002.costoftrips.ui.screens.tripdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhockool1002.costoftrips.data.local.entity.Expense
import com.nhockool1002.costoftrips.data.local.entity.Trip
import com.nhockool1002.costoftrips.data.local.entity.TripMember
import com.nhockool1002.costoftrips.data.repository.TripRepository
import com.nhockool1002.costoftrips.util.Settlement
import com.nhockool1002.costoftrips.util.simplifyDebts
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettlementSuggestion(val from: TripMember, val to: TripMember, val amount: Double)

data class TripDetailUiState(
    val trip: Trip? = null,
    val expenses: List<Expense> = emptyList(),
    val total: Double = 0.0,
    val members: List<TripMember> = emptyList(),
    val splitMemberIdsByExpenseId: Map<Long, List<Long>> = emptyMap(),
    val settlements: List<SettlementSuggestion> = emptyList()
)

class TripDetailViewModel(
    private val repository: TripRepository,
    private val tripId: Long
) : ViewModel() {

    val uiState: StateFlow<TripDetailUiState> = combine(
        repository.observeTrip(tripId),
        repository.observeExpensesForTrip(tripId),
        repository.observeMembersForTrip(tripId),
        repository.observeSplitsForTrip(tripId)
    ) { trip, expenses, members, splits ->
        val splitMemberIdsByExpenseId = splits.groupBy({ it.expenseId }, { it.memberId })

        val balances = members.associate { it.id to 0.0 }.toMutableMap()
        expenses.forEach { expense ->
            val splitMemberIds = splitMemberIdsByExpenseId[expense.id]
            val payerId = expense.paidByMemberId
            if (payerId != null && !splitMemberIds.isNullOrEmpty()) {
                val share = expense.amount / splitMemberIds.size
                splitMemberIds.forEach { memberId -> balances[memberId] = (balances[memberId] ?: 0.0) - share }
                balances[payerId] = (balances[payerId] ?: 0.0) + expense.amount
            }
        }
        val memberById = members.associateBy { it.id }
        val settlements = simplifyDebts(balances).mapNotNull { settlement: Settlement ->
            val from = memberById[settlement.fromMemberId]
            val to = memberById[settlement.toMemberId]
            if (from != null && to != null) SettlementSuggestion(from, to, settlement.amount) else null
        }

        TripDetailUiState(
            trip = trip,
            expenses = expenses,
            total = expenses.sumOf { it.amount },
            members = members,
            splitMemberIdsByExpenseId = splitMemberIdsByExpenseId,
            settlements = settlements
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TripDetailUiState())

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch { repository.deleteExpense(expense) }
    }

    fun reorderExpenses(orderedExpenseIds: List<Long>) {
        viewModelScope.launch { repository.reorderExpenses(orderedExpenseIds) }
    }

    fun addMember(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch { repository.addMember(TripMember(tripId = tripId, name = trimmed)) }
    }

    fun deleteMember(member: TripMember) {
        viewModelScope.launch { repository.deleteMember(member) }
    }

    fun setBudget(budget: Double?) {
        val trip = uiState.value.trip ?: return
        viewModelScope.launch { repository.updateTrip(trip.copy(budget = budget)) }
    }
}
