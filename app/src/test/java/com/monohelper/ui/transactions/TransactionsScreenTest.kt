package com.monohelper.ui.transactions

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
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
@Config(sdk = [34], application = android.app.Application::class)
class TransactionsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun successShowsDescriptionAndSignedAmount() {
        composeRule.setContent {
            MonohelperTheme {
                TransactionsScreen(
                    state = TransactionsUiState.Success(listOf(TestData.transaction())),
                    accountFiltered = false,
                    onRetry = {},
                )
            }
        }

        composeRule.onNodeWithText("Grocery store").assertIsDisplayed()
        composeRule.onNodeWithText("-₴150.00").assertIsDisplayed()
    }

    @Test
    fun accountFilteredShowsHeader() {
        composeRule.setContent {
            MonohelperTheme {
                TransactionsScreen(
                    state = TransactionsUiState.Success(listOf(TestData.transaction())),
                    accountFiltered = true,
                    onRetry = {},
                )
            }
        }

        composeRule.onNodeWithText("Filtered by account").assertIsDisplayed()
    }
}
