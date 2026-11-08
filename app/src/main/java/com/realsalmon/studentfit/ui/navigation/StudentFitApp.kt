package com.realsalmon.studentfit.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material.icons.outlined.Accessibility
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Watch
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.ui.unit.dp
import com.realsalmon.studentfit.ui.bodytype.BodyScreen
import com.realsalmon.studentfit.ui.bodytype.BodyTypeInputScreen
import com.realsalmon.studentfit.ui.camera.CameraScanScreen
import com.realsalmon.studentfit.ui.exercise.ExerciseDetailScreen
import com.realsalmon.studentfit.ui.exercise.ExercisesScreen
import com.realsalmon.studentfit.ui.home.HomeScreen
import com.realsalmon.studentfit.ui.watch.WatchScreen

object Routes {
    const val HOME = "home"
    const val BODY = "body"
    const val EXERCISES = "exercises"
    const val WATCH = "watch"
    const val BODY_INPUT = "body_input"
    const val CAMERA_SCAN = "camera_scan"
    const val EXERCISE_DETAIL = "exercise/{id}"
    fun exercise(id: String) = "exercise/$id"
}

private data class Tab(val route: String, val label: String, val icon: ImageVector, val selectedIcon: ImageVector)

private val tabs = listOf(
    Tab(Routes.HOME, "홈", Icons.Outlined.Home, Icons.Filled.Home),
    Tab(Routes.BODY, "체형", Icons.Outlined.Accessibility, Icons.Filled.Accessibility),
    Tab(Routes.EXERCISES, "운동", Icons.Outlined.FitnessCenter, Icons.Filled.FitnessCenter),
    Tab(Routes.WATCH, "워치", Icons.Outlined.Watch, Icons.Filled.Watch),
)

/** 앱 안에서 화면을 옮길 때 쓰는 동작 모음. */
class AppNavigator(private val nav: NavHostController) {
    fun tab(route: String) = nav.navigate(route) {
        popUpTo(nav.graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
    fun bodyInput() = nav.navigate(Routes.BODY_INPUT)
    fun camera() = nav.navigate(Routes.CAMERA_SCAN)
    fun exercise(id: String) = nav.navigate(Routes.exercise(id))
    fun back() { nav.popBackStack() }
}

@Composable
fun StudentFitApp() {
    val nav = rememberNavController()
    val go = AppNavigator(nav)
    val entry by nav.currentBackStackEntryAsState()
    val current = entry?.destination?.route
    val showBar = tabs.any { it.route == current }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBar) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainerLow) {
                    tabs.forEach { tab ->
                        val selected = current == tab.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = { go.tab(tab.route) },
                            icon = { Icon(if (selected) tab.selectedIcon else tab.icon, contentDescription = null) },
                            label = { Text(tab.label) },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                            ),
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(bottom = if (showBar) padding.calculateBottomPadding() else 0.dp),
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() },
        ) {
            composable(Routes.HOME) { HomeScreen(go) }
            composable(Routes.BODY) { BodyScreen(go) }
            composable(Routes.EXERCISES) { ExercisesScreen(go) }
            composable(Routes.WATCH) { WatchScreen() }
            composable(Routes.BODY_INPUT) { BodyTypeInputScreen(onBack = go::back) }
            composable(Routes.CAMERA_SCAN) { CameraScanScreen(onBack = go::back, onSaved = { needsInput -> go.back(); if (needsInput) go.bodyInput() else go.tab(Routes.EXERCISES) }) }
            composable(
                Routes.EXERCISE_DETAIL,
                arguments = listOf(navArgument("id") { type = NavType.StringType }),
            ) { e ->
                ExerciseDetailScreen(id = e.arguments?.getString("id").orEmpty(), onBack = go::back)
            }
        }
    }
}
