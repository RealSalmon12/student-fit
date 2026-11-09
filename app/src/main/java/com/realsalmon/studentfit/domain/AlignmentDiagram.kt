package com.realsalmon.studentfit.domain

/**
 * 체형 결과를 "정렬선 그림"으로 보여주기 위한 2D 좌표.
 * 바닥 y = 0, 키 = 1. 옆모습은 +x가 앞쪽, 정면은 +x가 사람의 왼쪽이에요.
 * 바른 자세라면 옆모습에서 귀·어깨·골반·무릎·발목이 세로 정렬선(x = 0) 위에 놓여요.
 */
object AlignmentDiagram {
    data class P(val x: Float, val y: Float)

    /** 정렬선에서 벗어난 지점 하나(그림에 표시해요). */
    data class Mark(val at: P, val label: String)

    data class Figure(
        /** 몸을 그리는 선(굵은 선으로 이어 그려요). */
        val strokes: List<List<P>>,
        val head: P,
        val headRadius: Float,
        /** 정렬선 위에서 확인하는 기준점들 */
        val landmarks: List<P>,
        /** 벗어난 지점 */
        val marks: List<Mark>,
        /** 정렬선 x 위치 */
        val plumbX: Float,
        /** 정면 그림이면 strokes[1](몸통 테두리)을 채워 그리고, 옆모습이면 strokes[0](몸통)을 굵게 그려요. */
        val front: Boolean = false,
    )

    private const val HEAD_R = 0.062f

    fun side(issues: Set<PostureIssue>): Figure {
        var head = P(0.012f, 0.928f)
        var ear = P(0f, 0.925f)
        var shoulder = P(0f, 0.81f)
        var upperBack = P(-0.03f, 0.74f)
        var lowBack = P(-0.012f, 0.6f)
        var hip = P(0f, 0.52f)
        val knee = P(0.012f, 0.285f)
        val ankle = P(0f, 0.045f)
        val toe = P(0.11f, 0.008f)
        val heel = P(-0.035f, 0.008f)
        val marks = mutableListOf<Mark>()

        if (PostureIssue.ROUNDED_BACK in issues) {
            upperBack = P(-0.06f, 0.745f)
            shoulder = P(shoulder.x + 0.02f, shoulder.y - 0.005f)
            marks += Mark(upperBack, PostureIssue.ROUNDED_BACK.label)
        }
        if (PostureIssue.ROUNDED_SHOULDERS in issues) {
            shoulder = P(shoulder.x + 0.035f, shoulder.y)
        }
        if (PostureIssue.ROUNDED_SHOULDERS in issues || PostureIssue.ROUNDED_BACK in issues) {
            marks += Mark(shoulder, PostureIssue.ROUNDED_SHOULDERS.label.takeIf { PostureIssue.ROUNDED_SHOULDERS in issues } ?: "어깨")
        }
        if (PostureIssue.FORWARD_HEAD in issues) {
            ear = P(ear.x + 0.075f, ear.y - 0.012f)
            head = P(head.x + 0.075f, head.y - 0.012f)
            marks += Mark(ear, PostureIssue.FORWARD_HEAD.label)
        }
        if (PostureIssue.ANTERIOR_PELVIC_TILT in issues) {
            hip = P(hip.x - 0.03f, hip.y)
            lowBack = P(lowBack.x + 0.03f, lowBack.y)
            marks += Mark(hip, PostureIssue.ANTERIOR_PELVIC_TILT.label)
        }

        val neck = P((ear.x + shoulder.x) / 2f - 0.005f, (ear.y + shoulder.y) / 2f - 0.01f)
        return Figure(
            strokes = listOf(
                listOf(ear, neck, shoulder, upperBack, lowBack, hip),
                listOf(hip, knee, ankle),
                listOf(heel, ankle, toe, heel),
                listOf(shoulder, P(shoulder.x + 0.01f, 0.64f), P(shoulder.x + 0.03f, 0.5f)),
            ),
            head = head,
            headRadius = HEAD_R,
            landmarks = listOf(ear, shoulder, hip, knee, ankle),
            marks = marks,
            plumbX = 0f,
        )
    }

    fun front(issues: Set<PostureIssue>): Figure {
        var lShoulder = P(0.125f, 0.81f)
        var rShoulder = P(-0.125f, 0.81f)
        var lHip = P(0.07f, 0.52f)
        var rHip = P(-0.07f, 0.52f)
        var lKnee = P(0.065f, 0.285f)
        var rKnee = P(-0.065f, 0.285f)
        var lAnkle = P(0.058f, 0.045f)
        var rAnkle = P(-0.058f, 0.045f)
        val marks = mutableListOf<Mark>()

        if (PostureIssue.UNEVEN_SHOULDERS in issues) {
            lShoulder = P(lShoulder.x, lShoulder.y + 0.028f)
            marks += Mark(lShoulder, PostureIssue.UNEVEN_SHOULDERS.label)
        }
        if (PostureIssue.PELVIC_IMBALANCE in issues) {
            lHip = P(lHip.x, lHip.y + 0.022f)
            marks += Mark(lHip, PostureIssue.PELVIC_IMBALANCE.label)
        }
        if (PostureIssue.BOW_LEGS in issues) {
            lKnee = P(0.1f, lKnee.y); rKnee = P(-0.1f, rKnee.y)
            lAnkle = P(0.045f, lAnkle.y); rAnkle = P(-0.045f, rAnkle.y)
            marks += Mark(lKnee, PostureIssue.BOW_LEGS.label)
        }
        if (PostureIssue.KNOCK_KNEES in issues) {
            lKnee = P(0.03f, lKnee.y); rKnee = P(-0.03f, rKnee.y)
            lAnkle = P(0.085f, lAnkle.y); rAnkle = P(-0.085f, rAnkle.y)
            marks += Mark(lKnee, PostureIssue.KNOCK_KNEES.label)
        }
        val neck = P(0f, 0.85f)
        val head = P(0f, 0.928f)
        val waistL = P((lShoulder.x + lHip.x) / 2f - 0.015f, 0.66f)
        val waistR = P((rShoulder.x + rHip.x) / 2f + 0.015f, 0.66f)
        return Figure(
            strokes = listOf(
                listOf(lShoulder, neck, rShoulder),
                listOf(lShoulder, waistL, lHip, rHip, waistR, rShoulder),
                listOf(lHip, lKnee, lAnkle, P(lAnkle.x + 0.03f, 0.008f)),
                listOf(rHip, rKnee, rAnkle, P(rAnkle.x - 0.03f, 0.008f)),
                listOf(lShoulder, P(lShoulder.x + 0.03f, 0.64f), P(lShoulder.x + 0.035f, 0.49f)),
                listOf(rShoulder, P(rShoulder.x - 0.03f, 0.64f), P(rShoulder.x - 0.035f, 0.49f)),
            ),
            head = head,
            headRadius = HEAD_R,
            landmarks = listOf(lShoulder, rShoulder, lHip, rHip, lKnee, rKnee),
            marks = marks,
            plumbX = 0f,
            front = true,
        )
    }
}
