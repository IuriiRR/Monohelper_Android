package com.monohelper.ui.dashboard

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.monohelper.testutil.TestData
import com.monohelper.ui.components.LOADING_TAG
import com.monohelper.ui.theme.MonohelperTheme
import java.time.YearMonth
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], application = android.app.Application::class)
class DashboardScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun successStateShowsJarTitle() {
        composeRule.setContent {
            MonohelperTheme {
                DashboardScreen(
                    month = YearMonth.of(2024, 1),
                    state = DashboardUiState.Success(TestData.monthlyReport()),
                    onPreviousMonth = {},
                    onNextMonth = {},
                    onRetry = {},
                )
            }
        }

        composeRule.onNodeWithText("Groceries").assertIsDisplayed()
    }

    @Test
    fun loadingStateShowsLoadingIndicator() {
        composeRule.setContent {
            MonohelperTheme {
                DashboardScreen(
                    month = YearMonth.of(2024, 1),
                    state = DashboardUiState.Loading,
                    onPreviousMonth = {},
                    onNextMonth = {},
                    onRetry = {},
                )
            }
        }

        composeRule.onNodeWithTag(LOADING_TAG).assertIsDisplayed()
    }

    @Test
    fun monthLabelIsShown() {
        composeRule.setContent {
            MonohelperTheme {
                DashboardScreen(
                    month = YearMonth.of(2024, 1),
                    state = DashboardUiState.Loading,
                    onPreviousMonth = {},
                    onNextMonth = {},
                    onRetry = {},
                )
            }
        }

        composeRule.onNodeWithText("January 2024").assertIsDisplayed()
    }
}
