package com.monohelper.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.monohelper.ui.accounts.AccountsRoute
import com.monohelper.ui.dashboard.DashboardRoute
import com.monohelper.ui.sync.SyncRoute
import com.monohelper.ui.transactions.TransactionsRoute

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = Routes.DASHBOARD,
        modifier = modifier,
    ) {
        composable(Routes.DASHBOARD) {
            DashboardRoute()
        }
        composable(Routes.ACCOUNTS) {
            AccountsRoute(
                onAccountClick = { accountId ->
                    navController.navigate(Routes.transactions(accountId)) {
                        popUpTo(Routes.ACCOUNTS) { inclusive = true }
                    }
                },
            )
        }
        composable(
            route = Routes.TRANSACTIONS_PATTERN,
            arguments = listOf(
                navArgument(Routes.ARG_ACCOUNT_ID) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) {
            TransactionsRoute()
        }
        composable(Routes.SYNC) {
            SyncRoute()
        }
    }
}
