package com.monohelper.ui.accounts

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.monohelper.testutil.TestData
import com.monohelper.ui.components.LOADING_TAG
import com.monohelper.ui.theme.MonohelperTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], application = Application::class)
class AccountsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun successShowsAccountTitleAndClickReportsAccountId() {
        var clickedId: String? = null
        composeRule.setContent {
            MonohelperTheme {
                AccountsScreen(
                    state = AccountsUiState.Success(listOf(TestData.account())),
                    onAccountClick = { clickedId = it },
                    onRetry = {},
                )
            }
        }

        composeRule.onNodeWithText("Vacation").assertIsDisplayed()
        composeRule.onNodeWithText("Vacation").performClick()
        assertEquals("acc-1", clickedId)
    }

    @Test
    fun loadingShowsLoadingIndicator() {
        composeRule.setContent {
            MonohelperTheme {
                AccountsScreen(
                    state = AccountsUiState.Loading,
                    onAccountClick = {},
                    onRetry = {},
                )
            }
        }

        composeRule.onNodeWithTag(LOADING_TAG).assertIsDisplayed()
    }
}
