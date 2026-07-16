package com.nhockool1002.costoftrips.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SettlementCalculatorTest {

    @Test
    fun `no balances produces no settlements`() {
        assertEquals(emptyList<Settlement>(), simplifyDebts(emptyMap()))
    }

    @Test
    fun `all balances already zero produces no settlements`() {
        val balances = mapOf(1L to 0.0, 2L to 0.0)
        assertEquals(emptyList<Settlement>(), simplifyDebts(balances))
    }

    @Test
    fun `balances within epsilon are treated as settled`() {
        val balances = mapOf(1L to 0.005, 2L to -0.005)
        assertEquals(emptyList<Settlement>(), simplifyDebts(balances))
    }

    @Test
    fun `single debtor and creditor settle exactly`() {
        val balances = mapOf(1L to -50.0, 2L to 50.0)
        val settlements = simplifyDebts(balances)
        assertEquals(listOf(Settlement(fromMemberId = 1L, toMemberId = 2L, amount = 50.0)), settlements)
    }

    @Test
    fun `one debtor split across two creditors`() {
        // Member 1 owes 90 total, split between two creditors of 60 and 30.
        val balances = mapOf(1L to -90.0, 2L to 60.0, 3L to 30.0)
        val settlements = simplifyDebts(balances)
        assertEquals(2, settlements.size)
        assertEquals(Settlement(1L, 2L, 60.0), settlements[0])
        assertEquals(Settlement(1L, 3L, 30.0), settlements[1])
    }

    @Test
    fun `one creditor paid by two debtors`() {
        val balances = mapOf(1L to -60.0, 2L to -30.0, 3L to 90.0)
        val settlements = simplifyDebts(balances)
        assertEquals(2, settlements.size)
        assertEquals(Settlement(1L, 3L, 60.0), settlements[0])
        assertEquals(Settlement(2L, 3L, 30.0), settlements[1])
    }

    @Test
    fun `settlements always zero out the original balances`() {
        val balances = mapOf(1L to -120.0, 2L to 45.0, 3L to -15.0, 4L to 90.0)
        val settlements = simplifyDebts(balances)

        val net = balances.keys.associateWith { 0.0 }.toMutableMap()
        settlements.forEach { s ->
            net[s.fromMemberId] = (net[s.fromMemberId] ?: 0.0) - s.amount
            net[s.toMemberId] = (net[s.toMemberId] ?: 0.0) + s.amount
        }
        balances.forEach { (id, balance) ->
            assertTrue("member $id expected $balance but settlements produced ${net[id]}", Math.abs(balance - (net[id] ?: 0.0)) < 0.01)
        }
    }

    @Test
    fun `minimal number of settlements is produced (largest debtor to largest creditor)`() {
        // 4 members, but the greedy match should still resolve everyone in 3 payments max (n-1).
        val balances = mapOf(1L to -100.0, 2L to -50.0, 3L to 70.0, 4L to 80.0)
        val settlements = simplifyDebts(balances)
        assertTrue(settlements.size <= 3)
    }

    @Test
    fun `custom epsilon is respected`() {
        val balances = mapOf(1L to -5.0, 2L to 5.0)
        assertEquals(emptyList<Settlement>(), simplifyDebts(balances, epsilon = 10.0))
    }
}
