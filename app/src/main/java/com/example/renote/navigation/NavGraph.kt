package com.example.renote.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.renote.ui.common.ViewMode
import com.example.renote.ui.daily.DailyRoute
import com.example.renote.ui.detail.DetailRoute
import com.example.renote.ui.monthly.MonthlyRoute
import com.example.renote.ui.weekly.WeeklyRoute
import java.time.LocalDate

@Composable
fun ReNoteNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    // Start destination is daily/<today>?from=none: the date argument defaults to null,
    // which DailyViewModel resolves to today.
    NavHost(navController = navController, startDestination = Routes.DAILY, modifier = modifier) {
        composable(
            route = Routes.DAILY,
            arguments = listOf(
                navArgument(Routes.ARG_DATE) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument(Routes.ARG_FROM) {
                    type = NavType.StringType
                    defaultValue = DailyOrigin.NONE.routeValue
                },
            ),
        ) { entry ->
            DailyRoute(
                onBack = { entry.ifResumed { navController.popBackStack() } },
                onAddNote = { date -> entry.ifResumed { navController.navigate(Routes.detailNew(date)) } },
                onNoteClick = { id -> entry.ifResumed { navController.navigate(Routes.detailEdit(id)) } },
                onSwitchView = { mode, viewedMonth ->
                    when (mode) {
                        ViewMode.DAY -> Unit
                        ViewMode.WEEK -> navController.switchTo(Routes.weekly(LocalDate.now()), entry)
                        ViewMode.MONTH -> navController.switchTo(Routes.monthly(viewedMonth), entry)
                    }
                },
            )
        }

        composable(
            route = Routes.WEEKLY,
            arguments = listOf(navArgument(Routes.ARG_ANCHOR_DATE) { type = NavType.StringType }),
        ) { entry ->
            WeeklyRoute(
                onDayClick = { date ->
                    entry.ifResumed { navController.navigate(Routes.daily(date, DailyOrigin.WEEK)) }
                },
                onSwitchView = { mode, viewedMonth ->
                    when (mode) {
                        ViewMode.DAY -> navController.switchTo(Routes.daily(LocalDate.now()), entry)
                        ViewMode.WEEK -> Unit
                        ViewMode.MONTH -> navController.switchTo(Routes.monthly(viewedMonth), entry)
                    }
                },
            )
        }

        composable(
            route = Routes.MONTHLY,
            arguments = listOf(navArgument(Routes.ARG_YEAR_MONTH) { type = NavType.StringType }),
        ) { entry ->
            MonthlyRoute(
                onDayClick = { date ->
                    entry.ifResumed { navController.navigate(Routes.daily(date, DailyOrigin.MONTH)) }
                },
                onSwitchView = { mode ->
                    when (mode) {
                        ViewMode.DAY -> navController.switchTo(Routes.daily(LocalDate.now()), entry)
                        ViewMode.WEEK -> navController.switchTo(Routes.weekly(LocalDate.now()), entry)
                        ViewMode.MONTH -> Unit
                    }
                },
            )
        }

        composable(
            route = Routes.DETAIL_NEW,
            arguments = listOf(
                navArgument(Routes.ARG_DATE) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) { entry ->
            DetailRoute(onClose = { entry.ifResumed { navController.popBackStack() } })
        }

        composable(
            route = Routes.DETAIL_EDIT,
            arguments = listOf(navArgument(Routes.ARG_NOTE_ID) { type = NavType.LongType }),
        ) { entry ->
            // Detail is only ever opened from Daily, so popping returns to daily/{date}.
            DetailRoute(onClose = { entry.ifResumed { navController.popBackStack() } })
        }
    }
}

/**
 * The D/W/M switcher's navigation (spec §5): [route] replaces the screen of [entry] instead of
 * stacking on top of it, so switching views never adds to the back stack.
 */
private fun NavHostController.switchTo(route: String, entry: NavBackStackEntry) {
    entry.ifResumed {
        navigate(route) {
            popUpTo(entry.destination.id) { inclusive = true }
        }
    }
}

/**
 * Runs [action] only while this destination is the resumed one. Drops a second tap that lands
 * during a transition, which would otherwise open two screens or pop one too many.
 */
private inline fun NavBackStackEntry.ifResumed(action: () -> Unit) {
    if (lifecycle.currentState == Lifecycle.State.RESUMED) action()
}
