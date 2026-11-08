package com.realsalmon.studentfit.domain.motion

import kotlin.math.PI
import kotlin.math.cos

/** 어떤 관절을 바닥에 고정해서 미끄러지지 않게 할지. */
enum class Anchor { FEET, PELVIS, HANDS, HEAD }

/**
 * 운동 동작. 키프레임 사이를 부드럽게 이어서 [periodSec]마다 반복해요.
 * 마지막 키프레임 다음에는 첫 키프레임으로 돌아가요.
 */
data class Motion(
    val keyframes: List<Pose>,
    val periodSec: Float = 2.4f,
    val anchor: Anchor = Anchor.FEET,
) {
    init { require(keyframes.isNotEmpty()) }

    private val origin: Vec3 by lazy {
        val f = Skeleton.solve(keyframes.first())
        val a = anchorPoint(f)
        // 첫 자세의 가로·세로 가운데가 화면 가운데에 오도록 고정점 위치를 정해요.
        val xs = f.joints.values.map { it.x }
        val zs = f.joints.values.map { it.z }
        val cx = (xs.min() + xs.max()) / 2f
        val cz = (zs.min() + zs.max()) / 2f
        Vec3(a.x - cx, 0f, a.z - cz)
    }

    private fun anchorPoint(f: SkeletonFrame): Vec3 = when (anchor) {
        Anchor.FEET -> (f[Joint.L_ANKLE] + f[Joint.R_ANKLE]) * 0.5f
        Anchor.PELVIS -> f[Joint.PELVIS]
        Anchor.HANDS -> (f[Joint.L_WRIST] + f[Joint.R_WRIST]) * 0.5f
        Anchor.HEAD -> f[Joint.HEAD]
    }

    /** [timeSec] 시점의 자세. */
    fun poseAt(timeSec: Float): Pose {
        if (keyframes.size == 1) return keyframes[0]
        val n = keyframes.size
        val phase = ((timeSec / periodSec) % 1f + 1f) % 1f * n
        val i = phase.toInt().coerceIn(0, n - 1)
        val local = phase - i
        val eased = ((1 - cos(local * PI)) / 2).toFloat()
        return keyframes[i].lerp(keyframes[(i + 1) % n], eased)
    }

    /** [timeSec] 시점의 3D 관절 위치. 고정점은 화면 가운데 근처에 머물러요. */
    fun frameAt(timeSec: Float): SkeletonFrame = place(Skeleton.solve(poseAt(timeSec)))

    fun firstFrame(): SkeletonFrame = place(Skeleton.solve(keyframes.first()))

    private fun place(f: SkeletonFrame): SkeletonFrame {
        val a = anchorPoint(f)
        val shift = Vec3(origin.x - a.x, 0f, origin.z - a.z)
        return SkeletonFrame(f.joints.mapValues { it.value + shift })
    }
}
