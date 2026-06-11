package com.monohelper.core.result

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Test

class AppResultTest {

    @Test
    fun `map transforms success value`() {
        val result: AppResult<Int> = AppResult.Success(21)
        assertEquals(AppResult.Success(42), result.map { it * 2 })
    }

    @Test
    fun `map passes failure through unchanged`() {
        val failure: AppResult<Int> = AppResult.Failure("boom")
        assertSame(failure, failure.map { it * 2 })
    }

    @Test
    fun `onSuccess invokes block on success and returns receiver`() {
        val result: AppResult<Int> = AppResult.Success(7)
        var seen: Int? = null
        val returned = result.onSuccess { seen = it }
        assertEquals(7, seen)
        assertSame(result, returned)
    }

    @Test
    fun `onSuccess does not invoke block on failure`() {
        val failure: AppResult<Int> = AppResult.Failure("boom")
        var invoked = false
        val returned = failure.onSuccess { invoked = true }
        assertFalse(invoked)
        assertSame(failure, returned)
    }

    @Test
    fun `onFailure invokes block on failure and returns receiver`() {
        val failure: AppResult<Int> = AppResult.Failure("boom")
        var message: String? = null
        val returned = failure.onFailure { message = it }
        assertEquals("boom", message)
        assertSame(failure, returned)
    }

    @Test
    fun `onFailure does not invoke block on success`() {
        val result: AppResult<Int> = AppResult.Success(7)
        var invoked = false
        val returned = result.onFailure { invoked = true }
        assertFalse(invoked)
        assertSame(result, returned)
    }
}
