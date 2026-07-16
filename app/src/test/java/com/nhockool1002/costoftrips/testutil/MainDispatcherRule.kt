package com.nhockool1002.costoftrips.testutil

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Swaps [Dispatchers.Main] for a [TestDispatcher] so ViewModels using `viewModelScope` are
 * testable. Uses [UnconfinedTestDispatcher] by default so `viewModelScope.launch { ... }` blocks
 * (e.g. `combine`/`stateIn` pipelines) start eagerly instead of needing manual `advanceUntilIdle()`
 * calls, since the real Room I/O they await runs on Room's own background executor rather than
 * this test dispatcher's virtual clock.
 */
@ExperimentalCoroutinesApi
class MainDispatcherRule(
    val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
