package com.monohelper.ui.nav

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], application = Application::class)
class AppNavHostTest {

    @get:Rule
    val composeRule = createComposeRule()

    /** Regression test: tapping Accounts after drilling into account transactions must show Accounts, not Transactions. */
    @Test
    fun accountClickNavigatesToTransactionsAndRemovesAccountsFromBackStack() {
        lateinit var navController: NavHostController

        composeRule.setContent {
            navController = rememberNavController()
            NavHost(navController = navController, startDestination = Routes.DASHBOARD) {
                composable(Routes.DASHBOARD) { Box(Modifier.fillMaxSize()) }
                composable(Routes.ACCOUNTS) { Box(Modifier.fillMaxSize()) }
                composable(
                    route = Routes.TRANSACTIONS_PATTERN,
                    arguments = listOf(
                        navArgument(Routes.ARG_ACCOUNT_ID) {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        },
                    ),
                ) { Box(Modifier.fillMaxSize()) }
                composable(Routes.SYNC) { Box(Modifier.fillMaxSize()) }
            }
        }

        // 1. Navigate to Accounts tab
        composeRule.runOnIdle { navController.navigate(Routes.ACCOUNTS) }
        composeRule.waitForIdle()
        assertEquals(Routes.ACCOUNTS, navController.currentDestination?.route)

        // 2. Simulate onAccountClick — the fix applies popUpTo(ACCOUNTS) { inclusive = true }
        composeRule.runOnIdle {
            navController.navigate(Routes.transactions("acc-1")) {
                popUpTo(Routes.ACCOUNTS) { inclusive = true }
            }
        }
        composeRule.waitForIdle()

        val routeAfterClick = navController.currentDestination?.route?.substringBefore('?')
        assertEquals(Routes.TRANSACTIONS, routeAfterClick)

        // Accounts must NOT be in back stack (so it won't be restored on top of Transactions)
        val accountsInStack = navController.currentBackStack.value.any { it.destination.route == Routes.ACCOUNTS }
        assertFalse("Accounts must not remain in back stack after account click", accountsInStack)

        // 3. Simulate tapping Accounts in the bottom nav (save/restore pattern)
        composeRule.runOnIdle {
            navController.navigate(Routes.ACCOUNTS) {
                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
        composeRule.waitForIdle()

        // Must land on Accounts, not Transactions
        assertEquals(
            "Bottom nav tap on Accounts must show Accounts, not Transactions",
            Routes.ACCOUNTS,
            navController.currentDestination?.route,
        )
    }
}
