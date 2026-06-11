package com.monohelper.ui.sync

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.monohelper.domain.model.TaskStatus
import com.monohelper.domain.usecase.SyncKind
import com.monohelper.testutil.TestData
import com.monohelper.ui.theme.MonohelperTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], application = Application::class)
class SyncScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setScreen(state: SyncRunState) {
        composeRule.setContent {
            MonohelperTheme {
                SyncScreen(
                    state = state,
                    onSyncAccounts = {},
                    onSyncTransactions = {},
                    onReset = {},
                )
            }
        }
    }

    @Test
    fun `idle shows both buttons enabled`() {
        setScreen(SyncRunState.Idle)

        composeRule.onNodeWithText("Sync accounts").assertIsDisplayed().assertIsEnabled()
        composeRule.onNodeWithText("Sync transactions").assertIsDisplayed().assertIsEnabled()
        composeRule.onNodeWithText("Pick a sync to run").assertIsDisplayed()
    }

    @Test
    fun `finished success shows result summary`() {
        setScreen(
            SyncRunState.Finished(
                SyncKind.ACCOUNTS,
                TestData.taskInfo(status = TaskStatus.SUCCESS),
            ),
        )

        composeRule.onNodeWithText("status: success").assertIsDisplayed()
        composeRule.onNodeWithText("Done").assertIsDisplayed()
    }

    @Test
    fun `polling shows task id`() {
        setScreen(
            SyncRunState.Polling(
                SyncKind.ACCOUNTS,
                TestData.taskInfo(status = TaskStatus.RUNNING),
            ),
        )

        composeRule.onNodeWithText("Task #1", substring = true).assertIsDisplayed()
    }
}
