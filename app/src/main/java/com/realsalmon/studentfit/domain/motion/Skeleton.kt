package com.realsalmon.studentfit.domain.motion

import kotlin.math.cos
import kotlin.math.sin

data class Vec3(val x: Float, val y: Float, val z: Float) {
    operator fun plus(o: Vec3) = Vec3(x + o.x, y + o.y, z + o.z)
    operator fun minus(o: Vec3) = Vec3(x - o.x, y - o.y, z - o.z)
    operator fun times(s: Float) = Vec3(x * s, y * s, z * s)

    /** x축 기준 회전. 양수면 위쪽(+y)이 앞쪽(+z)으로 넘어가요. */
    fun rotX(deg: Float): Vec3 {
        val r = Math.toRadians(deg.toDouble())
        val c = cos(r).toFloat(); val s = sin(r).toFloat()
        return Vec3(x, y * c - z * s, y * s + z * c)
    }

    /** z축 기준 회전. 양수면 아래쪽(-y)이 왼쪽(+x)으로 넘어가요. */
    fun rotZ(deg: Float): Vec3 {
        val r = Math.toRadians(deg.toDouble())
        val c = cos(r).toFloat(); val s = sin(r).toFloat()
        return Vec3(x * c - y * s, x * s + y * c, z)
    }

    /** y축 기준 회전(위에서 볼 때 반시계). */
    fun rotY(deg: Float): Vec3 {
        val r = Math.toRadians(deg.toDouble())
        val c = cos(r).toFloat(); val s = sin(r).toFloat()
        return Vec3(x * c + z * s, y, -x * s + z * c)
    }

    companion object {
        val ZERO = Vec3(0f, 0f, 0f)
        val UP = Vec3(0f, 1f, 0f)
        val DOWN = Vec3(0f, -1f, 0f)
    }
}

enum class Joint {
    HEAD, NECK, CHEST, PELVIS,
    L_SHOULDER, L_ELBOW, L_WRIST, R_SHOULDER, R_ELBOW, R_WRIST,
    L_HIP, L_KNEE, L_ANKLE, L_TOE, R_HIP, R_KNEE, R_ANKLE, R_TOE,

    /** 얼굴 앞쪽 점(머리가 바라보는 방향). 뼈대 선에는 쓰지 않아요. */
    FACE,
    L_HAND, R_HAND,
}

enum class BoneSide { LEFT, RIGHT, CENTER }

data class Bone(val from: Joint, val to: Joint, val side: BoneSide)

/** 계산된 3D 자세. 바닥은 y = 0이고, 사람 키는 대략 1.65 단위예요(3D 모델 비율). */
data class SkeletonFrame(val joints: Map<Joint, Vec3>) {
    operator fun get(j: Joint): Vec3 = joints.getValue(j)
}

object Skeleton {
    const val HEAD_RADIUS = 0.105f

    private const val HIP_HALF = 0.066f
    private const val THIGH = 0.362f
    private const val SHIN = 0.462f
    private const val FOOT = 0.13f
    private const val SPINE_HALF = 0.1415f
    private const val SHOULDER_HALF = 0.102f
    private const val UPPER_ARM = 0.242f
    private const val FOREARM = 0.216f
    private const val NECK = 0.06f
    private const val HAND = 0.08f
    private const val HIP_DROP = 0.088f

    private val SURFACE = mapOf(
        Joint.HEAD to HEAD_RADIUS, Joint.NECK to 0.05f, Joint.CHEST to 0.13f, Joint.PELVIS to 0.11f,
        Joint.L_SHOULDER to 0.068f, Joint.R_SHOULDER to 0.068f, Joint.L_ELBOW to 0.04f, Joint.R_ELBOW to 0.04f,
        Joint.L_WRIST to 0.03f, Joint.R_WRIST to 0.03f, Joint.L_HAND to 0.028f, Joint.R_HAND to 0.028f,
        Joint.L_HIP to 0.105f, Joint.R_HIP to 0.105f, Joint.L_KNEE to 0.054f, Joint.R_KNEE to 0.054f,
        Joint.L_ANKLE to 0.1f, Joint.R_ANKLE to 0.1f, Joint.L_TOE to 0.04f, Joint.R_TOE to 0.04f,
    )

    val bones = listOf(
        Bone(Joint.PELVIS, Joint.CHEST, BoneSide.CENTER),
        Bone(Joint.CHEST, Joint.NECK, BoneSide.CENTER),
        Bone(Joint.NECK, Joint.HEAD, BoneSide.CENTER),
        Bone(Joint.NECK, Joint.L_SHOULDER, BoneSide.LEFT),
        Bone(Joint.L_SHOULDER, Joint.L_ELBOW, BoneSide.LEFT),
        Bone(Joint.L_ELBOW, Joint.L_WRIST, BoneSide.LEFT),
        Bone(Joint.NECK, Joint.R_SHOULDER, BoneSide.RIGHT),
        Bone(Joint.R_SHOULDER, Joint.R_ELBOW, BoneSide.RIGHT),
        Bone(Joint.R_ELBOW, Joint.R_WRIST, BoneSide.RIGHT),
        Bone(Joint.PELVIS, Joint.L_HIP, BoneSide.LEFT),
        Bone(Joint.L_HIP, Joint.L_KNEE, BoneSide.LEFT),
        Bone(Joint.L_KNEE, Joint.L_ANKLE, BoneSide.LEFT),
        Bone(Joint.L_ANKLE, Joint.L_TOE, BoneSide.LEFT),
        Bone(Joint.PELVIS, Joint.R_HIP, BoneSide.RIGHT),
        Bone(Joint.R_HIP, Joint.R_KNEE, BoneSide.RIGHT),
        Bone(Joint.R_KNEE, Joint.R_ANKLE, BoneSide.RIGHT),
        Bone(Joint.R_ANKLE, Joint.R_TOE, BoneSide.RIGHT),
    )

    /**
     * 팔다리 방향: 아래를 향한 벡터를 [flex]만큼 앞으로 들고, [abd]만큼 바깥으로 벌려요.
     * side는 왼쪽 +1, 오른쪽 -1.
     */
    private fun limbDir(flex: Float, abd: Float, side: Int): Vec3 =
        Vec3.DOWN.rotX(-flex).rotZ(side * abd)

    /** 관절 각도(몸 기준)로 3D 관절 위치를 계산해요. 바닥 맞춤은 하지 않아요. */
    fun solveRaw(p: Pose): SkeletonFrame {
        val j = HashMap<Joint, Vec3>()
        j[Joint.PELVIS] = Vec3.ZERO

        // 몸통: 골반 → 가슴(아래 척추) → 목(위 척추). spineCurve > 0이면 등이 둥글게 말려요.
        fun torso(v: Vec3, extra: Float = 0f) = v.rotX(p.trunkFlex + extra).rotZ(-p.trunkSide)
        val chest = torso(Vec3.UP, -p.spineCurve / 2f) * SPINE_HALF
        val neck = chest + torso(Vec3.UP, p.spineCurve / 2f) * SPINE_HALF
        j[Joint.CHEST] = chest
        j[Joint.NECK] = neck
        val headDir = torso(Vec3.UP, p.spineCurve / 2f + p.neckFlex)
        val headBase = neck + headDir * NECK + torso(Vec3(0f, 0f, p.headForward), p.spineCurve / 2f)
        val headCenter = headBase + headDir * HEAD_RADIUS
        j[Joint.HEAD] = headCenter
        j[Joint.FACE] = headCenter + torso(Vec3(0f, 0f, 1f), p.spineCurve / 2f + p.neckFlex) * HEAD_RADIUS

        fun arm(side: Int, a: ArmPose, sh: Joint, el: Joint, wr: Joint, hand: Joint) {
            val shoulder = neck + torso(Vec3(side * SHOULDER_HALF, -0.034f, p.shoulderForward), p.spineCurve / 2f)
            val upper = torso(limbDir(a.flex, a.abd, side), p.spineCurve / 2f)
            val fore = torso(limbDir(a.foreFlex, a.foreAbd, side), p.spineCurve / 2f)
            val elbow = shoulder + upper * UPPER_ARM
            j[sh] = shoulder
            j[el] = elbow
            j[wr] = elbow + fore * FOREARM
            j[hand] = elbow + fore * (FOREARM + HAND)
        }
        arm(1, p.leftArm, Joint.L_SHOULDER, Joint.L_ELBOW, Joint.L_WRIST, Joint.L_HAND)
        arm(-1, p.rightArm, Joint.R_SHOULDER, Joint.R_ELBOW, Joint.R_WRIST, Joint.R_HAND)

        fun leg(side: Int, l: LegPose, hp: Joint, kn: Joint, an: Joint, toe: Joint) {
            val hip = Vec3(side * HIP_HALF, -HIP_DROP, 0f).rotX(p.pelvisTilt)
            val knee = hip + limbDir(l.hipFlex, l.hipAbd, side) * THIGH
            val ankle = knee + limbDir(l.hipFlex - l.knee, l.hipAbd, side) * SHIN
            j[hp] = hip
            j[kn] = knee
            j[an] = ankle
            j[toe] = ankle + limbDir(l.hipFlex - l.knee + 90f - l.ankle, l.hipAbd, side) * FOOT
        }
        leg(1, p.leftLeg, Joint.L_HIP, Joint.L_KNEE, Joint.L_ANKLE, Joint.L_TOE)
        leg(-1, p.rightLeg, Joint.R_HIP, Joint.R_KNEE, Joint.R_ANKLE, Joint.R_TOE)

        // 몸 전체 방향: 옆으로 눕기(roll) → 엎드리기/눕기(pitch)
        return SkeletonFrame(j.mapValues { (_, v) -> v.rotZ(p.bodyRoll).rotX(p.bodyPitch) })
    }

    /** 가장 낮은 점이 바닥(y = 0)에 닿게 올린 자세. */
    fun solve(p: Pose): SkeletonFrame {
        val raw = solveRaw(p)
        // 관절마다 살 두께를 빼서, 누운 자세에서도 몸 표면이 바닥에 닿게 해요.
        val minY = raw.joints.entries.minOf { (k, v) -> v.y - (SURFACE[k] ?: 0f) }
        val lift = Vec3(0f, -minY + p.lift, 0f)
        return SkeletonFrame(raw.joints.mapValues { it.value + lift })
    }
}
