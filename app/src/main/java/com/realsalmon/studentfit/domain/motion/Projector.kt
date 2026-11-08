package com.realsalmon.studentfit.domain.motion

/** 화면에 그릴 2D 점. depth가 클수록 화면 안쪽(멀리)이에요. scale은 1m가 몇 픽셀인지. */
data class Projected(val x: Float, val y: Float, val depth: Float, val scale: Float)

/** 보는 방향. yaw 0 = 정면, 90 = 몸의 왼쪽 옆, -90 = 오른쪽 옆. elevation = 내려다보는 각도. */
data class ViewAngle(val yaw: Float, val elevation: Float = 12f) {
    companion object {
        val FRONT = ViewAngle(0f, 8f)
        val SIDE = ViewAngle(90f, 8f)
        val THREE_QUARTER = ViewAngle(38f, 14f)
    }
}

/**
 * 3D 관절을 원근 투영으로 2D 화면에 옮겨요.
 * 화면 맞춤은 [fit]으로 계산해요.
 */
class Projector(
    private val yawDeg: Float,
    private val elevationDeg: Float,
    private val unit: Float,
    private val originX: Float,
    private val originY: Float,
    private val center: Vec3 = Vec3(0f, 0.6f, 0f),
) {
    fun project(p: Vec3): Projected {
        val v = (p - center).rotY(yawDeg).rotX(elevationDeg)
        val dist = CAM_DIST - v.z
        val s = CAM_DIST / dist
        return Projected(originX + v.x * s * unit, originY - v.y * s * unit, dist, s * unit)
    }

    /** 화면 맞춤(크기·위치)은 그대로 두고 보는 방향만 바꿔요. */
    fun withView(yaw: Float, elevation: Float) = Projector(yaw, elevation, unit, originX, originY, center)

    companion object {
        const val CAM_DIST = 5f

        /**
         * 동작 전체가 화면에 꽉 차도록 크기와 위치를 정해요.
         * 여러 방향([yaws])에서 본 범위를 합쳐서, 돌려 봐도 크기가 갑자기 바뀌지 않게 해요.
         */
        fun fit(
            frames: List<SkeletonFrame>,
            width: Float,
            height: Float,
            yaw: Float,
            elevation: Float,
            yaws: List<Float> = listOf(0f, 45f, 90f, 135f),
            padding: Float = 0.06f,
            margin: Float = 0.13f,
            focus: Set<Joint>? = null,
        ): Projector {
            val chosen = frames.flatMap { f -> f.joints.filterKeys { focus == null || it in focus }.values }
            // 몸 전체를 볼 때는 바닥 그림자도 보이게 발밑 점을 넣어요.
            val pts = if (focus != null) chosen else chosen + chosen.map { Vec3(it.x, 0f, it.z) }
            val cx = (pts.minOf { it.x } + pts.maxOf { it.x }) / 2f
            val cy = (pts.minOf { it.y } + pts.maxOf { it.y }) / 2f
            val cz = (pts.minOf { it.z } + pts.maxOf { it.z }) / 2f
            val center = Vec3(cx, cy, cz)

            var halfW = 0f; var halfH = 0f
            for (y in (yaws + yaw)) {
                val probe = Projector(y, elevation, 1f, 0f, 0f, center)
                for (p in pts) {
                    val q = probe.project(p)
                    halfW = maxOf(halfW, kotlin.math.abs(q.x) + margin)
                    halfH = maxOf(halfH, kotlin.math.abs(q.y) + margin)
                }
            }
            val usableW = width * (1 - 2 * padding)
            val usableH = height * (1 - 2 * padding)
            val unit = minOf(usableW / (2 * halfW), usableH / (2 * halfH))
            return Projector(yaw, elevation, unit, width / 2f, height / 2f, center)
        }
    }
}
