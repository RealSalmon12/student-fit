package com.realsalmon.studentfit.domain.pose

import com.realsalmon.studentfit.domain.PostureIssue
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.roundToInt

/** 카메라 포즈 인식에서 쓰는 관절. ML Kit 랜드마크 중 필요한 것만 써요. */
enum class BodyPoint {
    NOSE, LEFT_EAR, RIGHT_EAR,
    LEFT_SHOULDER, RIGHT_SHOULDER,
    LEFT_HIP, RIGHT_HIP,
    LEFT_KNEE, RIGHT_KNEE,
    LEFT_ANKLE, RIGHT_ANKLE,
}

/** 이미지 좌표(아래로 갈수록 y가 커짐)의 점과 인식 신뢰도(0~1). */
data class PosePoint(val x: Float, val y: Float, val confidence: Float)

typealias PoseSample = Map<BodyPoint, PosePoint>

enum class ScanView(val label: String, val guide: String) {
    FRONT("정면", "카메라를 정면으로 보고, 발을 모은 채 팔을 편하게 내리고 서 주세요."),
    SIDE("측면", "몸을 옆으로 돌려 서고, 평소처럼 편하게 서 주세요."),
}

data class Finding(
    val issue: PostureIssue,
    val detected: Boolean,
    val measurement: String,
)

data class ScanResult(val view: ScanView, val findings: List<Finding>, val missing: List<String>) {
    val detected: List<Finding> get() = findings.filter { it.detected }
}

/**
 * 포즈 랜드마크로 자세 문제를 추정해요. 의학적 진단이 아니라 참고용이에요.
 *
 * 정면: 어깨·골반 기울기, 무릎·발목 간격(O다리/X다리).
 * 측면: 머리-목 각도(CVA, 거북목), 어깨·골반 앞뒤 위치(라운드숄더, 오리엉덩이).
 */
object PostureAnalyzer {

    const val MIN_CONFIDENCE = 0.5f

    // 기준값. 연구에서 흔히 쓰는 값을 단순화했어요.
    const val TILT_DEG = 3f            // 어깨·골반 좌우 기울기
    const val LEG_GAP_RATIO = 0.25f    // 골반 너비 대비 무릎/발목 간격 차이
    const val CVA_DEG = 50f            // 머리-목 각도가 이보다 작으면 거북목
    const val SHOULDER_AHEAD = 0.12f   // 몸통 길이 대비 어깨가 골반보다 앞
    const val HIP_BEHIND = 0.10f       // 몸통 길이 대비 골반이 발목보다 뒤

    /** 여러 프레임을 평균내서 손떨림과 인식 오차를 줄여요. */
    fun average(samples: List<PoseSample>): PoseSample {
        val out = HashMap<BodyPoint, PosePoint>()
        for (bp in BodyPoint.entries) {
            val pts = samples.mapNotNull { it[bp] }.filter { it.confidence >= MIN_CONFIDENCE }
            if (pts.isEmpty()) continue
            out[bp] = PosePoint(
                pts.map { it.x }.average().toFloat(),
                pts.map { it.y }.average().toFloat(),
                pts.map { it.confidence }.average().toFloat(),
            )
        }
        return out
    }

    fun analyze(view: ScanView, sample: PoseSample): ScanResult = when (view) {
        ScanView.FRONT -> analyzeFront(sample)
        ScanView.SIDE -> analyzeSide(sample)
    }

    private fun PoseSample.ok(vararg pts: BodyPoint) = pts.all { (this[it]?.confidence ?: 0f) >= MIN_CONFIDENCE }

    private fun tiltDeg(a: PosePoint, b: PosePoint): Float =
        Math.toDegrees(atan2(abs(a.y - b.y).toDouble(), abs(a.x - b.x).toDouble())).toFloat()

    private fun Float.r1() = (this * 10).roundToInt() / 10f

    private fun analyzeFront(s: PoseSample): ScanResult {
        val findings = mutableListOf<Finding>()
        val missing = mutableListOf<String>()

        if (s.ok(BodyPoint.LEFT_SHOULDER, BodyPoint.RIGHT_SHOULDER)) {
            val t = tiltDeg(s.getValue(BodyPoint.LEFT_SHOULDER), s.getValue(BodyPoint.RIGHT_SHOULDER))
            findings += Finding(PostureIssue.UNEVEN_SHOULDERS, t > TILT_DEG, "어깨 기울기 ${t.r1()}° (기준 ${TILT_DEG.toInt()}°)")
        } else missing += "어깨"

        if (s.ok(BodyPoint.LEFT_HIP, BodyPoint.RIGHT_HIP)) {
            val t = tiltDeg(s.getValue(BodyPoint.LEFT_HIP), s.getValue(BodyPoint.RIGHT_HIP))
            findings += Finding(PostureIssue.PELVIC_IMBALANCE, t > TILT_DEG, "골반 기울기 ${t.r1()}° (기준 ${TILT_DEG.toInt()}°)")
        } else missing += "골반"

        if (s.ok(BodyPoint.LEFT_HIP, BodyPoint.RIGHT_HIP, BodyPoint.LEFT_KNEE, BodyPoint.RIGHT_KNEE,
                BodyPoint.LEFT_ANKLE, BodyPoint.RIGHT_ANKLE)
        ) {
            val hipW = abs(s.getValue(BodyPoint.LEFT_HIP).x - s.getValue(BodyPoint.RIGHT_HIP).x).coerceAtLeast(1f)
            val kneeGap = abs(s.getValue(BodyPoint.LEFT_KNEE).x - s.getValue(BodyPoint.RIGHT_KNEE).x) / hipW
            val ankleGap = abs(s.getValue(BodyPoint.LEFT_ANKLE).x - s.getValue(BodyPoint.RIGHT_ANKLE).x) / hipW
            val diff = kneeGap - ankleGap
            val pct = { v: Float -> "${(v * 100).roundToInt()}%" }
            findings += Finding(PostureIssue.BOW_LEGS, diff > LEG_GAP_RATIO,
                "무릎 간격 ${pct(kneeGap)}, 발목 간격 ${pct(ankleGap)} (골반 너비 대비)")
            findings += Finding(PostureIssue.KNOCK_KNEES, -diff > LEG_GAP_RATIO,
                "발목 간격이 무릎보다 ${pct((-diff).coerceAtLeast(0f))} 넓어요")
        } else missing += "다리 전체"

        return ScanResult(ScanView.FRONT, findings, missing)
    }

    private fun analyzeSide(s: PoseSample): ScanResult {
        val findings = mutableListOf<Finding>()
        val missing = mutableListOf<String>()

        // 카메라 쪽에 더 잘 보이는 몸 반쪽을 써요.
        val leftScore = listOf(BodyPoint.LEFT_EAR, BodyPoint.LEFT_SHOULDER, BodyPoint.LEFT_HIP, BodyPoint.LEFT_ANKLE)
            .sumOf { (s[it]?.confidence ?: 0f).toDouble() }
        val rightScore = listOf(BodyPoint.RIGHT_EAR, BodyPoint.RIGHT_SHOULDER, BodyPoint.RIGHT_HIP, BodyPoint.RIGHT_ANKLE)
            .sumOf { (s[it]?.confidence ?: 0f).toDouble() }
        val left = leftScore >= rightScore
        val ear = if (left) BodyPoint.LEFT_EAR else BodyPoint.RIGHT_EAR
        val sh = if (left) BodyPoint.LEFT_SHOULDER else BodyPoint.RIGHT_SHOULDER
        val hip = if (left) BodyPoint.LEFT_HIP else BodyPoint.RIGHT_HIP
        val ankle = if (left) BodyPoint.LEFT_ANKLE else BodyPoint.RIGHT_ANKLE

        if (!s.ok(ear, sh, hip, BodyPoint.NOSE)) {
            return ScanResult(ScanView.SIDE, emptyList(), listOf("귀·어깨·골반"))
        }
        val e = s.getValue(ear); val shp = s.getValue(sh); val h = s.getValue(hip)
        // 코가 귀보다 어느 쪽에 있는지로 바라보는 방향을 정해요(+1이면 화면 오른쪽).
        val facing = if (s.getValue(BodyPoint.NOSE).x >= e.x) 1f else -1f
        val torso = hypot((shp.x - h.x).toDouble(), (shp.y - h.y).toDouble()).toFloat().coerceAtLeast(1f)

        // 머리-목 각도(CVA): 어깨에서 귀를 잇는 선이 수평과 이루는 각도
        val forward = (e.x - shp.x) * facing
        val rise = shp.y - e.y
        val cva = if (forward <= 0f) 90f else Math.toDegrees(atan2(rise.toDouble(), forward.toDouble())).toFloat()
        findings += Finding(PostureIssue.FORWARD_HEAD, cva < CVA_DEG, "머리-목 각도 ${cva.r1()}° (${CVA_DEG.toInt()}° 미만이면 거북목)")

        val shoulderAhead = (shp.x - h.x) * facing / torso
        findings += Finding(PostureIssue.ROUNDED_SHOULDERS, shoulderAhead > SHOULDER_AHEAD,
            "어깨가 골반보다 몸통 길이의 ${(shoulderAhead * 100).roundToInt()}% 앞 (기준 ${(SHOULDER_AHEAD * 100).toInt()}%)")

        if (s.ok(ankle)) {
            val hipBehind = (s.getValue(ankle).x - h.x) * facing / torso
            findings += Finding(PostureIssue.ANTERIOR_PELVIC_TILT, hipBehind > HIP_BEHIND && shoulderAhead > 0f,
                "엉덩이가 발목보다 몸통 길이의 ${(hipBehind * 100).roundToInt()}% 뒤 (기준 ${(HIP_BEHIND * 100).toInt()}%)")
        } else missing += "발목"

        return ScanResult(ScanView.SIDE, findings, missing)
    }
}
