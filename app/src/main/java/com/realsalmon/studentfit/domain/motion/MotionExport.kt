package com.realsalmon.studentfit.domain.motion

import java.util.Locale

/**
 * 3D 사람 모델(WebView)로 보낼 동작 데이터.
 * 한 바퀴를 [samples]개로 나눈 관절 위치를 JSON으로 만들어요.
 * 관절 순서는 [Joint] 순서와 같고, 좌표는 y 위쪽, 사람은 +z를 바라보며 왼쪽이 +x예요.
 */
object MotionExport {
    fun toJson(motion: Motion, samples: Int = 60): String {
        val sb = StringBuilder(samples * Joint.entries.size * 24)
        sb.append("{\"period\":").append(fmt(motion.periodSec))
        sb.append(",\"joints\":[")
        Joint.entries.forEachIndexed { i, j -> if (i > 0) sb.append(','); sb.append('"').append(j.name).append('"') }
        sb.append("],\"frames\":[")
        for (s in 0 until samples) {
            if (s > 0) sb.append(',')
            val f = motion.frameAt(motion.periodSec * s / samples)
            sb.append('[')
            Joint.entries.forEachIndexed { i, j ->
                val v = f[j]
                if (i > 0) sb.append(',')
                sb.append(fmt(v.x)).append(',').append(fmt(v.y)).append(',').append(fmt(v.z))
            }
            sb.append(']')
        }
        // 점프처럼 일부러 바닥에서 뜨는 높이
        sb.append("],\"lift\":[")
        for (s in 0 until samples) {
            if (s > 0) sb.append(',')
            sb.append(fmt(motion.poseAt(motion.periodSec * s / samples).lift))
        }
        sb.append("]}")
        return sb.toString()
    }

    private fun fmt(v: Float) = String.format(Locale.US, "%.4f", v)
}
