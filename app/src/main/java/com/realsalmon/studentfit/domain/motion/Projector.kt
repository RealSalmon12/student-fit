package com.realsalmon.studentfit.domain.motion

/** 화면에 그릴 2D 점. depth가 클수록 화면 안쪽(멀리)이에요. */
data class Projected(val x: Float, val y: Float, val depth: Float, val scale: Float)

/**
 * 3D 관절을 원근 투영으로 2D 화면에 옮겨요.
 * yaw: 몸을 위에서 본 회전 각도, elevation: 카메라가 내려다보는 각도.
 */
class Projector(
    private val width: Float,
    private val height: Float,
    private val yawDeg: Float,
    private val elevationDeg: Float = 14f,
    private val sceneSize: Float = 2.3f,
    private val centerY: Float = 0.75f,
) {
    private val camDist = 5f
    private val unit = minOf(width, height) / sceneSize

    fun project(p: Vec3): Projected {
        val v = Vec3(p.x, p.y - centerY, p.z).rotY(yawDeg).rotX(-elevationDeg)
        // 카메라는 +z 쪽에서 몸을 바라봐요.
        val dist = camDist - v.z
        val s = camDist / dist
        return Projected(
            x = width / 2f + v.x * s * unit,
            y = height / 2f - v.y * s * unit,
            depth = dist,
            scale = s * unit,
        )
    }
}
