package com.nhockool1002.costoftrips.util

/** One suggested payment that would zero out both members' balances. */
data class Settlement(val fromMemberId: Long, val toMemberId: Long, val amount: Double)

/**
 * Reduces a set of net balances (positive = owed money, negative = owes money)
 * to a minimal list of payments, by always matching the largest debtor with the
 * largest creditor. Amounts smaller than [epsilon] are treated as settled to
 * avoid floating-point noise producing near-zero "phantom" payments.
 */
fun simplifyDebts(balances: Map<Long, Double>, epsilon: Double = 0.01): List<Settlement> {
    val creditors = balances.filter { it.value > epsilon }
        .map { it.key to it.value }
        .sortedByDescending { it.second }
        .toMutableList()
    val debtors = balances.filter { it.value < -epsilon }
        .map { it.key to -it.value }
        .sortedByDescending { it.second }
        .toMutableList()

    val settlements = mutableListOf<Settlement>()
    var i = 0
    var j = 0
    while (i < debtors.size && j < creditors.size) {
        val (debtorId, debtAmount) = debtors[i]
        val (creditorId, creditAmount) = creditors[j]
        val settled = minOf(debtAmount, creditAmount)
        settlements.add(Settlement(debtorId, creditorId, settled))
        debtors[i] = debtorId to (debtAmount - settled)
        creditors[j] = creditorId to (creditAmount - settled)
        if (debtors[i].second <= epsilon) i++
        if (creditors[j].second <= epsilon) j++
    }
    return settlements
}
