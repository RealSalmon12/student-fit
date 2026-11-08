package com.realsalmon.studentfit.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.realsalmon.studentfit.data.ProfileRepository
import com.realsalmon.studentfit.data.RoutineLog
import com.realsalmon.studentfit.domain.BodyProfile
import com.realsalmon.studentfit.domain.BodyTypeClassifier
import com.realsalmon.studentfit.domain.BodyTypeResult

/** 화면에 들어올 때마다 저장된 체형 정보를 새로 읽어요. */
@Composable
fun rememberProfile(): Pair<BodyProfile?, BodyTypeResult?> {
    val context = LocalContext.current
    return remember {
        val p = ProfileRepository(context).load()
        p to p?.let { BodyTypeClassifier.classify(it) }
    }
}

@Composable
fun rememberRoutineLog(): RoutineLog {
    val context = LocalContext.current
    return remember { RoutineLog(context) }
}
