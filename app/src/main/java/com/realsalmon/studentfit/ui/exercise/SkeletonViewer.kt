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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import com.realsalmon.studentfit.domain.motion.Joint
import com.realsalmon.studentfit.domain.motion.ViewAngle
import kotlinx.coroutines.launch
import com.realsalmon.studentfit.domain.motion.Capsule
import com.realsalmon.studentfit.domain.motion.Mannequin
import com.realsalmon.studentfit.domain.motion.Material
import com.realsalmon.studentfit.domain.motion.Motion
import com.realsalmon.studentfit.domain.motion.Projected
import com.realsalmon.studentfit.domain.motion.Projector
import com.realsalmon.studentfit.domain.motion.SkeletonFrame
import com.realsalmon.studentfit.domain.motion.Solid
import com.realsalmon.studentfit.domain.motion.Sphere
import com.realsalmon.studentfit.domain.motion.Vec3
import kotlin.math.hypot
import kotlin.math.max

/** 마네킹 색. 옷 색은 앱 테마를 따라가요. */
data class SkeletonColors(
    val shirt: Color,
    val shorts: Color = Color(0xFF2C2F48),
    val skin: Color = Color(0xFFF1C7A5),
    val shoe: Color = Color(0xFFFF7A59),
    val hair: Color = Color(0xFF2A2230),
    val floor: Color,
    val shadow: Color = Color(0xFF1E143C),
) {
    fun of(m: Material): Color = when (m) {
        Material.SKIN -> skin
        Material.SHIRT -> shirt
        Material.SHORTS -> shorts
        Material.SHOE -> shoe
        Material.HAIR -> hair
        Material.FACE -> lerp(skin, Color(0xFFC98B6B), 0.35f)
        Material.EYE -> hair
    }
}

/** 화면 맞춤에 쓰는 상체 관절. */
private val UPPER_FOCUS = setOf(
    Joint.HEAD, Joint.FACE, Joint.NECK, Joint.CHEST, Joint.PELVIS, Joint.L_SHOULDER, Joint.R_SHOULDER,
)

/**
 * 운동 동작을 옷 입은 3D 마네킹으로 보여줘요.
 * - [view]가 바뀌면 그 시점으로 부드럽게 돌아가요.
 * - 손가락으로 끌면 직접 돌려볼 수 있어요(좌우: 회전, 위아래: 내려다보는 각도).
 * - [time]을 주면 그 시각의 자세를 보여줘요(따라하기에서 횟수와 맞출 때). 없으면 스스로 재생해요.
 */
@Composable
fun SkeletonViewer(
    motion: Motion,
    colors: SkeletonColors,
    modifier: Modifier = Modifier,
    view: ViewAngle = ViewAngle.THREE_QUARTER,
    focusUpper: Boolean = false,
    playing: Boolean = true,
    interactive: Boolean = true,
    speed: Float = 1f,
    time: Float? = null,
) {
    var clock by remember(motion) { mutableFloatStateOf(0f) }
    val yaw = remember(motion) { Animatable(view.yaw) }
    val elevation = remember(motion) { Animatable(view.elevation) }
    val scope = rememberCoroutineScope()
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    LaunchedEffect(view) {
        launch { yaw.animateTo(nearestAngle(yaw.value, view.yaw), tween(650)) }
        launch { elevation.animateTo(view.elevation, tween(650)) }
    }

    LaunchedEffect(motion, playing, speed, time == null) {
        if (!playing || time != null) return@LaunchedEffect
        var last = withFrameNanos { it }
        while (true) {
            val now = withFrameNanos { it }
            clock += (now - last) / 1_000_000_000f * speed
            last = now
        }
    }

    // 동작 전체가 꽉 차게 한 번만 맞춰요(돌려도 크기가 출렁이지 않게).
    val fitted = remember(motion, canvasSize, focusUpper) {
        if (canvasSize == IntSize.Zero) null
        else Projector.fit(
            motion.sampleFrames, canvasSize.width.toFloat(), canvasSize.height.toFloat(),
            view.yaw, view.elevation,
            focus = if (focusUpper) UPPER_FOCUS else null,
            margin = if (focusUpper) 0.2f else 0.13f,
        )
    }

    val gestures = if (interactive) {
        Modifier.pointerInput(motion) {
            detectDragGestures { change, drag ->
                change.consume()
                scope.launch {
                    yaw.snapTo(yaw.value + drag.x * 0.4f)
                    elevation.snapTo((elevation.value + drag.y * 0.2f).coerceIn(-2f, 60f))
                }
            }
        }
    } else Modifier

    Canvas(modifier.onSizeChanged { canvasSize = it }.then(gestures)) {
        val pr = fitted?.withView(yaw.value, elevation.value) ?: return@Canvas
        val t = time ?: clock
        val frame = if (time == null && !playing) motion.firstFrame() else motion.frameAt(t)
        drawMannequin(frame, pr, colors)
    }
}

/** 지금 각도에서 가장 가까운 방향으로 돌도록 목표 각도를 맞춰요. */
private fun nearestAngle(current: Float, target: Float): Float {
    var t = target
    while (t - current > 180f) t -= 360f
    while (t - current < -180f) t += 360f
    return t
}

private fun Color.lighten(k: Float) = lerp(this, Color.White, k)
private fun Color.darken(k: Float) = lerp(this, Color.Black, k)

private fun DrawScope.drawMannequin(frame: SkeletonFrame, pr: Projector, c: SkeletonColors) {
    // 바닥 원판
    val o = pr.project(Vec3(0f, 0f, 0f))
    val fr = o.scale * 1.2f
    drawOval(
        Brush.radialGradient(listOf(c.floor, c.floor.copy(alpha = 0f)), center = Offset(o.x, o.y), radius = fr),
        topLeft = Offset(o.x - fr, o.y - fr * 0.3f),
        size = Size(fr * 2, fr * 0.6f),
    )

    val solids = Mannequin.build(frame)

    // 그림자: 모든 도형을 바닥에 눌러 한 겹으로 그려요(겹쳐도 진해지지 않게 레이어 사용).
    drawIntoCanvas { it.saveLayer(Rect(Offset.Zero, size), Paint().apply { alpha = 0.16f }) }
    val shadowBrush = Brush.linearGradient(listOf(c.shadow, c.shadow))
    for (s in solids) {
        when (s) {
            is Capsule -> capsule(pr.project(Vec3(s.a.x, 0f, s.a.z)), pr.project(Vec3(s.b.x, 0f, s.b.z)), s.ra, s.rb, shadowBrush)
            is Sphere -> {
                val p = pr.project(Vec3(s.c.x, 0f, s.c.z)); val r = s.r * p.scale
                drawOval(c.shadow, Offset(p.x - r, p.y - r * 0.5f), Size(2 * r, r))
            }
        }
    }
    drawIntoCanvas { it.restore() }

    // 먼 도형부터 그려서 가까운 도형이 위에 오게 해요.
    val sorted = solids.map { s ->
        val depth = when (s) {
            is Capsule -> (pr.project(s.a).depth + pr.project(s.b).depth) / 2f
            is Sphere -> pr.project(s.c).depth
        }
        s to depth - s.layer * 0.03f
    }.sortedByDescending { it.second }

    for ((s, _) in sorted) drawSolid(s, pr, c)
}

private fun DrawScope.drawSolid(s: Solid, pr: Projector, c: SkeletonColors) {
    var base = c.of(s.material)
    if (s.rightSide) base = base.darken(0.12f)
    when (s) {
        is Capsule -> {
            val a = pr.project(s.a); val b = pr.project(s.b)
            capsule(a, b, s.ra, s.rb, null, base)
        }
        is Sphere -> {
            val p = pr.project(s.c); val r = s.r * p.scale
            val behind = s.hideBehind
            if (behind != null && p.depth > pr.project(behind).depth - 0.04f) return
            drawCircle(
                Brush.radialGradient(
                    0f to base.lighten(0.35f), 0.45f to base, 1f to base.darken(0.38f),
                    center = Offset(p.x - r * 0.35f, p.y - r * 0.4f), radius = r * 1.4f,
                ),
                radius = r, center = Offset(p.x, p.y),
            )
        }
    }
}

/** 화면 위 캡슐: 양 끝 원 + 사이 사각형. 빛은 왼쪽 위에서 와요. */
private fun DrawScope.capsule(a: Projected, b: Projected, ra0: Float, rb0: Float, flat: Brush?, base: Color = Color.Black) {
    val ra = ra0 * a.scale; val rb = rb0 * b.scale
    var dx = b.x - a.x; var dy = b.y - a.y
    val len = hypot(dx, dy)
    var nx = 0f; var ny = -1f
    val quad = if (len > 0.5f) {
        dx /= len; dy /= len
        nx = -dy; ny = dx
        if (nx + ny > 0) { nx = -nx; ny = -ny }
        Path().apply {
            moveTo(a.x + nx * ra, a.y + ny * ra)
            lineTo(b.x + nx * rb, b.y + ny * rb)
            lineTo(b.x - nx * rb, b.y - ny * rb)
            lineTo(a.x - nx * ra, a.y - ny * ra)
            close()
        }
    } else null

    val brush = flat ?: run {
        val mx = (a.x + b.x) / 2f; val my = (a.y + b.y) / 2f; val r = max(ra, rb)
        Brush.linearGradient(
            0f to base.lighten(0.3f), 0.3f to base, 0.65f to base.darken(0.2f), 1f to base.darken(0.4f),
            start = Offset(mx + nx * r, my + ny * r),
            end = Offset(mx - nx * r + 0.01f, my - ny * r),
        )
    }
    drawCircle(brush, radius = ra, center = Offset(a.x, a.y))
    drawCircle(brush, radius = rb, center = Offset(b.x, b.y))
    quad?.let { drawPath(it, brush) }
}
