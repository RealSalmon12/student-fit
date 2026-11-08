package com.realsalmon.studentfit.domain.motion

/**
 * 팔 자세(몸통 기준, 도).
 * flex: 아래로 늘어뜨린 상태에서 앞으로 든 각도(90 = 앞으로 수평, 180 = 머리 위, 음수 = 뒤로).
 * abd: 바깥으로 벌린 각도(90 = 옆으로 수평).
 * foreFlex/foreAbd: 아래팔의 방향(위팔과 따로 정해요).
 */
data class ArmPose(
    val flex: Float = 0f,
    val abd: Float = 8f,
    val foreFlex: Float = 0f,
    val foreAbd: Float = 8f,
) {
    fun lerp(o: ArmPose, t: Float) = ArmPose(
        flex.lerp(o.flex, t), abd.lerp(o.abd, t), foreFlex.lerp(o.foreFlex, t), foreAbd.lerp(o.foreAbd, t),
    )

    companion object {
        /** 팔꿈치를 굽힌 상태: 위팔은 (flex, abd), 아래팔은 위팔보다 [bend]만큼 더 앞으로. */
        fun bent(flex: Float, bend: Float, abd: Float = 8f) = ArmPose(flex, abd, flex + bend, abd)
        fun straight(flex: Float, abd: Float = 8f) = ArmPose(flex, abd, flex, abd)
    }
}

/** 다리 자세(골반 기준, 도). hipFlex 양수 = 허벅지를 앞으로 듦, knee 양수 = 무릎 굽힘. */
data class LegPose(
    val hipFlex: Float = 0f,
    val hipAbd: Float = 3f,
    val knee: Float = 0f,
    val ankle: Float = 0f,
) {
    fun lerp(o: LegPose, t: Float) = LegPose(
        hipFlex.lerp(o.hipFlex, t), hipAbd.lerp(o.hipAbd, t), knee.lerp(o.knee, t), ankle.lerp(o.ankle, t),
    )
}

/**
 * 한 순간의 몸 자세. 서 있는 기본 자세가 기본값이에요.
 * bodyPitch: 90 = 엎드림(얼굴이 바닥), -90 = 누움(얼굴이 위).
 * bodyRoll: 90 = 오른쪽으로 누움(왼쪽이 위).
 */
data class Pose(
    val bodyPitch: Float = 0f,
    val bodyRoll: Float = 0f,
    val lift: Float = 0f,
    val pelvisTilt: Float = 0f,
    val trunkFlex: Float = 0f,
    val trunkSide: Float = 0f,
    val spineCurve: Float = 0f,
    val neckFlex: Float = 0f,
    val headForward: Float = 0f,
    val shoulderForward: Float = 0f,
    val leftArm: ArmPose = ArmPose(),
    val rightArm: ArmPose = ArmPose(),
    val leftLeg: LegPose = LegPose(),
    val rightLeg: LegPose = LegPose(),
) {
    fun lerp(o: Pose, t: Float) = Pose(
        bodyPitch.lerp(o.bodyPitch, t), bodyRoll.lerp(o.bodyRoll, t), lift.lerp(o.lift, t),
        pelvisTilt.lerp(o.pelvisTilt, t), trunkFlex.lerp(o.trunkFlex, t), trunkSide.lerp(o.trunkSide, t),
        spineCurve.lerp(o.spineCurve, t), neckFlex.lerp(o.neckFlex, t), headForward.lerp(o.headForward, t),
        shoulderForward.lerp(o.shoulderForward, t),
        leftArm.lerp(o.leftArm, t), rightArm.lerp(o.rightArm, t),
        leftLeg.lerp(o.leftLeg, t), rightLeg.lerp(o.rightLeg, t),
    )

    /** 양쪽 팔을 같게. */
    fun arms(a: ArmPose) = copy(leftArm = a, rightArm = a)
    /** 양쪽 다리를 같게. */
    fun legs(l: LegPose) = copy(leftLeg = l, rightLeg = l)
}

internal fun Float.lerp(to: Float, t: Float) = this + (to - this) * t
