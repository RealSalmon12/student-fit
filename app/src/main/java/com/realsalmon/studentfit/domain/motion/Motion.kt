package com.realsalmon.studentfit.domain.motion

import kotlin.math.PI
import kotlin.math.cos

/** 어떤 부위를 바닥에 고정해서 미끄러지지 않게 할지. */
enum class Anchor {
    FEET, LEFT_FOOT, RIGHT_FOOT, PELVIS, HANDS, HEAD;

    fun mirrored() = when (this) {
        LEFT_FOOT -> RIGHT_FOOT
        RIGHT_FOOT -> LEFT_FOOT
        else -> this
    }
}

/** 키프레임 하나. [weight]는 다음 키프레임까지 걸리는 시간 비율(0이면 바로 넘어가요). */
data class Key(val pose: Pose, val weight: Float = 1f)

/** 키프레임 사이를 어떻게 이을지. */
enum class Ease { SMOOTH, LINEAR }

/**
 * 운동 동작 한 번(1회). [periodSec]마다 반복하고, 마지막 키프레임 다음엔 첫 키프레임으로 돌아가요.
 * 같은 자세를 연달아 두면 그만큼 멈춰 있어요(버티기 구간).
 */
class Motion(
    val keys: List<Key>,
    val periodSec: Float = 2.4f,
    val anchor: Anchor = Anchor.FEET,
    val ease: Ease = Ease.SMOOTH,
) {
    init { require(keys.isNotEmpty()) }

    /** 예전 방식: 자세 목록을 같은 간격으로. */
    constructor(poses: List<Pose>, periodSec: Float = 2.4f, anchor: Anchor = Anchor.FEET) :
        this(poses.map { Key(it) }, periodSec, anchor)

    val keyframes: List<Pose> get() = keys.map { it.pose }

    private val totalWeight = keys.sumOf { it.weight.toDouble() }.toFloat().coerceAtLeast(1e-4f)

    /** 좌우를 뒤집은 동작. */
    fun mirrored(): Motion = Motion(keys.map { Key(it.pose.mirrored(), it.weight) }, periodSec, anchor.mirrored(), ease)

    private val origin: Vec3 by lazy {
        val f = Skeleton.solve(keys.first().pose)
        val a = anchorPoint(f)
        // 동작 전체의 가운데가 원점 근처에 오도록 고정점 위치를 정해요.
        val all = (0 until SAMPLES).flatMap { i ->
            val g = Skeleton.solve(poseAt(periodSec * i / SAMPLES))
            val shift = Vec3(a.x - anchorPoint(g).x, 0f, a.z - anchorPoint(g).z)
            g.joints.values.map { it + shift }
        }
        val cx = (all.minOf { it.x } + all.maxOf { it.x }) / 2f
        val cz = (all.minOf { it.z } + all.maxOf { it.z }) / 2f
        Vec3(a.x - cx, 0f, a.z - cz)
    }

    private fun anchorPoint(f: SkeletonFrame): Vec3 = when (anchor) {
        Anchor.FEET -> (f[Joint.L_ANKLE] + f[Joint.R_ANKLE]) * 0.5f
        Anchor.LEFT_FOOT -> f[Joint.L_ANKLE]
        Anchor.RIGHT_FOOT -> f[Joint.R_ANKLE]
        Anchor.PELVIS -> f[Joint.PELVIS]
        Anchor.HANDS -> (f[Joint.L_WRIST] + f[Joint.R_WRIST]) * 0.5f
        Anchor.HEAD -> f[Joint.HEAD]
    }

    /** [timeSec] 시점의 자세. */
    fun poseAt(timeSec: Float): Pose {
        if (keys.size == 1) return keys[0].pose
        val phase = ((timeSec / periodSec) % 1f + 1f) % 1f * totalWeight
        var acc = 0f
        for (i in keys.indices) {
            val w = keys[i].weight
            if (phase < acc + w || i == keys.lastIndex) {
                val local = if (w <= 0f) 1f else ((phase - acc) / w).coerceIn(0f, 1f)
                val t = when (ease) {
                    Ease.SMOOTH -> ((1 - cos(local * PI)) / 2).toFloat()
                    Ease.LINEAR -> local
                }
                return keys[i].pose.lerp(keys[(i + 1) % keys.size].pose, t)
            }
            acc += w
        }
        return keys.last().pose
    }

    /** [timeSec] 시점의 3D 관절 위치. 고정점은 제자리에 머물러요. */
    fun frameAt(timeSec: Float): SkeletonFrame = place(Skeleton.solve(poseAt(timeSec)))

    fun firstFrame(): SkeletonFrame = place(Skeleton.solve(keys.first().pose))

    /** 동작 한 바퀴에서 뽑은 프레임들(화면 맞춤용). */
    val sampleFrames: List<SkeletonFrame> by lazy { (0 until SAMPLES).map { frameAt(periodSec * it / SAMPLES) } }

    private fun place(f: SkeletonFrame): SkeletonFrame {
        val a = anchorPoint(f)
        val shift = Vec3(origin.x - a.x, 0f, origin.z - a.z)
        return SkeletonFrame(f.joints.mapValues { it.value + shift })
    }

    private companion object {
        const val SAMPLES = 16
    }
}
