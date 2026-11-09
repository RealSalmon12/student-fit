package com.realsalmon.studentfit.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.realsalmon.studentfit.domain.AlignmentDiagram
import com.realsalmon.studentfit.ui.theme.Body
import com.realsalmon.studentfit.ui.theme.Plumb

/**
 * 정렬선 그림: 노란 세로선(다림줄)과 몸의 기준점(귀·어깨·골반·무릎·발목)을 그려요.
 * 선에서 벗어난 곳은 점선과 이름표로 표시해요.
 */
@Composable
fun AlignmentFigure(
    figure: AlignmentDiagram.Figure,
    modifier: Modifier = Modifier,
    bodyColor: Color,
    dotColor: Color,
    alertColor: Color,
    labelColor: Color = alertColor,
    showLabels: Boolean = true,
    labelOnLeft: Boolean = false,
) {
    val measurer = rememberTextMeasurer()
    val labelStyle = TextStyle(fontFamily = Body, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = labelColor)
    Canvas(modifier) {
        val h = size.height * 0.9f
        val floorY = size.height * 0.96f
        val cx = size.width * (if (showLabels && !labelOnLeft) 0.36f else if (showLabels) 0.64f else 0.5f)
        fun o(p: AlignmentDiagram.P) = Offset(cx + (p.x - figure.plumbX) * h, floorY - p.y * h)

        // 바닥
        drawLine(bodyColor.copy(alpha = 0.5f), Offset(cx - 0.2f * h, floorY), Offset(cx + 0.2f * h, floorY), 1.dp.toPx())

        // 몸
        val w = 0.048f * h
        figure.strokes.forEachIndexed { k, line ->
            val path = Path()
            line.forEachIndexed { i, p -> val q = o(p); if (i == 0) path.moveTo(q.x, q.y) else path.lineTo(q.x, q.y) }
            val torso = if (figure.front) k == 1 else k == 0
            if (torso && figure.front) {
                path.close()
                drawPath(path, bodyColor)
            }
            drawPath(path, bodyColor, style = Stroke(width = if (torso && !figure.front) w * 1.9f else w, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
        drawCircle(bodyColor, radius = figure.headRadius * h, center = o(figure.head))

        // 정렬선(다림줄)과 추
        val top = Offset(cx, floorY - 1.0f * h)
        val bobTop = floorY - 0.075f * h
        drawLine(Plumb, top, Offset(cx, bobTop), 2.5.dp.toPx())
        val bob = Path().apply {
            val r = 0.022f * h
            moveTo(cx, bobTop)
            lineTo(cx + r, bobTop + r * 1.4f)
            lineTo(cx, bobTop + r * 2.6f)
            lineTo(cx - r, bobTop + r * 1.4f)
            close()
        }
        drawPath(bob, Plumb)
        drawCircle(Plumb, 3.dp.toPx(), top)

        // 기준점
        figure.landmarks.forEach { drawCircle(dotColor, 3.5.dp.toPx(), o(it)) }

        // 벗어난 곳
        val dash = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 3.dp.toPx()))
        figure.marks.forEach { m ->
            val q = o(m.at)
            drawLine(alertColor, Offset(cx, q.y), q, 1.5.dp.toPx(), pathEffect = dash)
            drawCircle(alertColor, 5.dp.toPx(), q)
            drawCircle(Color.White, 2.dp.toPx(), q)
            if (showLabels) {
                val layout = measurer.measure(m.label, labelStyle)
                val x = if (labelOnLeft) cx - 0.16f * h - layout.size.width else cx + 0.16f * h
                drawLine(alertColor.copy(alpha = 0.6f), q, Offset(if (labelOnLeft) x + layout.size.width + 4.dp.toPx() else x - 4.dp.toPx(), q.y), 1.dp.toPx())
                drawText(layout, topLeft = Offset(x, q.y - layout.size.height / 2f))
            }
        }
    }
}
