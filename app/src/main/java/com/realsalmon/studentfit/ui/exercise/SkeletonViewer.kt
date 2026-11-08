package com.realsalmon.studentfit.ui.exercise

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import com.realsalmon.studentfit.domain.motion.BoneSide
import com.realsalmon.studentfit.domain.motion.Joint
import com.realsalmon.studentfit.domain.motion.Motion
import com.realsalmon.studentfit.domain.motion.Projector
import com.realsalmon.studentfit.domain.motion.Skeleton
import com.realsalmon.studentfit.domain.motion.SkeletonFrame
import com.realsalmon.studentfit.domain.motion.Vec3

data class SkeletonColors(
    val left: Color,
    val right: Color,
    val center: Color,
    val floor: Color,
)

/**
 * 운동 동작을 3D 막대 인형으로 보여줘요.
 * 손가락으로 좌우로 끌면 돌려볼 수 있어요.
 */
@Composable
fun SkeletonViewer(
    motion: Motion,
    colors: SkeletonColors,
    modifier: Modifier = Modifier,
    playing: Boolean = true,
    interactive: Boolean = true,
    initialYaw: Float = 35f,
    speed: Float = 1f,
) {
    var time by remember(motion) { mutableFloatStateOf(0f) }
    var yaw by remember(motion) { mutableFloatStateOf(initialYaw) }
    var elevation by remember(motion) { mutableFloatStateOf(14f) }

    LaunchedEffect(motion, playing, speed) {
        if (!playing) return@LaunchedEffect
        var last = withFrameNanos { it }
        while (true) {
            val now = withFrameNanos { it }
            time += (now - last) / 1_000_000_000f * speed
            last = now
        }
    }

    val gestures = if (interactive) {
        Modifier.pointerInput(motion) {
            detectDragGestures { change, drag ->
                change.consume()
                yaw += drag.x * 0.4f
                elevation = (elevation + drag.y * 0.2f).coerceIn(-5f, 60f)
            }
        }
    } else Modifier

    Canvas(modifier.then(gestures)) {
        val frame = if (playing) motion.frameAt(time) else motion.firstFrame()
        drawSkeleton(frame, Projector(size.width, size.height, yaw, elevation), colors)
    }
}

private fun DrawScope.drawSkeleton(frame: SkeletonFrame, pr: Projector, c: SkeletonColors) {
    // 바닥 격자
    val grid = 1.2f
    for (i in -6..6) {
        val k = i * grid / 6f
        line(pr, Vec3(k, 0f, -grid), Vec3(k, 0f, grid), c.floor, 1.5f)
        line(pr, Vec3(-grid, 0f, k), Vec3(grid, 0f, k), c.floor, 1.5f)
    }

    // 그림자
    val pelvis = frame[Joint.PELVIS]
    val shadowCenter = pr.project(Vec3(pelvis.x, 0f, pelvis.z))
    drawOval(
        brush = Brush.radialGradient(
            listOf(Color.Black.copy(alpha = 0.18f), Color.Transparent),
            center = Offset(shadowCenter.x, shadowCenter.y),
            radius = shadowCenter.scale * 0.6f,
        ),
        topLeft = Offset(shadowCenter.x - shadowCenter.scale * 0.6f, shadowCenter.y - shadowCenter.scale * 0.18f),
        size = Size(shadowCenter.scale * 1.2f, shadowCenter.scale * 0.36f),
    )

    // 먼 뼈부터 그려서 가까운 뼈가 위에 오게 해요.
    val items = Skeleton.bones.map { b ->
        val a = pr.project(frame[b.from]); val e = pr.project(frame[b.to])
        Triple(b, a, e) to (a.depth + e.depth) / 2f
    }.sortedByDescending { it.second }

    val head = pr.project(frame[Joint.HEAD])
    var headDrawn = false
    for ((t, depth) in items) {
        if (!headDrawn && depth < head.depth) {
            drawHead(head.x, head.y, Skeleton.HEAD_RADIUS * head.scale, c.center)
            headDrawn = true
        }
        val (bone, a, e) = t
        val color = when (bone.side) {
            BoneSide.LEFT -> c.left
            BoneSide.RIGHT -> c.right
            BoneSide.CENTER -> c.center
        }
        val w = 0.07f * (a.scale + e.scale) / 2f
        drawLine(color, Offset(a.x, a.y), Offset(e.x, e.y), strokeWidth = w, cap = StrokeCap.Round)
        drawCircle(color.copy(alpha = 1f), radius = w * 0.62f, center = Offset(e.x, e.y))
    }
    if (!headDrawn) drawHead(head.x, head.y, Skeleton.HEAD_RADIUS * head.scale, c.center)
}

private fun DrawScope.drawHead(x: Float, y: Float, r: Float, color: Color) {
    drawCircle(color, radius = r, center = Offset(x, y))
    drawCircle(Color.White.copy(alpha = 0.25f), radius = r * 0.45f, center = Offset(x - r * 0.3f, y - r * 0.3f))
    drawCircle(color, radius = r, center = Offset(x, y), style = Stroke(width = r * 0.08f))
}

private fun DrawScope.line(pr: Projector, a: Vec3, b: Vec3, color: Color, w: Float) {
    val pa = pr.project(a); val pb = pr.project(b)
    drawLine(color, Offset(pa.x, pa.y), Offset(pb.x, pb.y), strokeWidth = w)
}
