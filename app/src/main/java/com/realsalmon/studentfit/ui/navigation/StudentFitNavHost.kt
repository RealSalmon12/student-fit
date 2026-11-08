package com.realsalmon.studentfit.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.realsalmon.studentfit.ui.bodytype.BodyTypeInputScreen
import com.realsalmon.studentfit.ui.comingsoon.ComingSoonScreen
import com.realsalmon.studentfit.ui.home.HomeScreen

object Routes {
    const val HOME = "home"
    const val BODY_INPUT = "body_input"
    const val CAMERA_SCAN = "camera_scan"   // 2단계: ML Kit 포즈 인식
    const val EXERCISES = "exercises"       // 3단계: 교정 운동 + 3D 예시
    const val WATCH = "watch"               // 4단계: Health Connect / 갤럭시 워치
}

@Composable
fun StudentFitNavHost() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(onNavigate = { route -> nav.navigate(route) })
        }
        composable(Routes.BODY_INPUT) {
            BodyTypeInputScreen(onBack = { nav.popBackStack() })
        }
        composable(Routes.CAMERA_SCAN) {
            ComingSoonScreen("카메라 체형 인식", "카메라로 자세를 찍으면 거북목, 라운드숄더 등을 찾아줘요.") { nav.popBackStack() }
        }
        composable(Routes.EXERCISES) {
            ComingSoonScreen("교정 운동", "내 체형에 맞는 운동을 3D 동작으로 보여줘요.") { nav.popBackStack() }
        }
        composable(Routes.WATCH) {
            ComingSoonScreen("워치 연동", "갤럭시 워치의 심박수와 운동 기록을 가져와요.") { nav.popBackStack() }
        }
    }
}
