package com.realsalmon.studentfit.domain.exercise

import com.realsalmon.studentfit.domain.BodyConcern as C
import com.realsalmon.studentfit.domain.PostureIssue as P
import com.realsalmon.studentfit.domain.exercise.ExerciseCategory.CARDIO
import com.realsalmon.studentfit.domain.exercise.ExerciseCategory.STRENGTH
import com.realsalmon.studentfit.domain.exercise.ExerciseCategory.STRETCH
import com.realsalmon.studentfit.domain.motion.Anchor
import com.realsalmon.studentfit.domain.motion.ArmPose
import com.realsalmon.studentfit.domain.motion.LegPose
import com.realsalmon.studentfit.domain.motion.Motion
import com.realsalmon.studentfit.domain.motion.Pose

/** 앱에 들어 있는 교정 운동 목록. 3D 동작은 관절 각도 키프레임으로 정의해요. */
object ExerciseCatalog {

    private val STAND = Pose()
    private val ARMS_DOWN = ArmPose.straight(0f, 8f)

    // 엎드려 네발 자세(손과 무릎)
    private val QUADRUPED = Pose(
        bodyPitch = 90f,
        leftArm = ArmPose.straight(90f, 6f), rightArm = ArmPose.straight(90f, 6f),
        leftLeg = LegPose(hipFlex = 90f, knee = 90f, ankle = -60f),
        rightLeg = LegPose(hipFlex = 90f, knee = 90f, ankle = -60f),
    )

    // 누워서 무릎 세운 자세
    private val HOOK_LYING = Pose(
        bodyPitch = -90f,
        leftArm = ArmPose.straight(-10f, 15f), rightArm = ArmPose.straight(-10f, 15f),
        leftLeg = LegPose(hipFlex = 50f, knee = 100f, ankle = 30f),
        rightLeg = LegPose(hipFlex = 50f, knee = 100f, ankle = 30f),
    )

    // 오른쪽으로 옆으로 누운 자세
    private val SIDE_LYING = Pose(
        bodyRoll = 90f,
        leftArm = ArmPose.straight(90f, -60f), rightArm = ArmPose.straight(170f, 0f),
        leftLeg = LegPose(hipAbd = 0f), rightLeg = LegPose(hipAbd = 0f),
    )

    val all: List<Exercise> = listOf(
        Exercise(
            id = "chin_tuck",
            name = "턱 당기기",
            summary = "앞으로 나온 머리를 제자리로 돌려놓는 가장 기본 운동",
            category = STRETCH,
            dose = "5초 유지 × 10회",
            steps = listOf(
                "벽에 등을 대고 바르게 서요.",
                "시선은 정면, 턱을 뒤로 밀어 이중턱을 만든다는 느낌으로 당겨요.",
                "뒤통수가 벽에 닿으면 5초 버텨요.",
                "천천히 풀고 다시 반복해요.",
            ),
            tips = "고개를 숙이지 말고 수평으로 뒤로 미는 게 핵심이에요.",
            posture = setOf(P.FORWARD_HEAD),
            motion = Motion(
                listOf(
                    STAND.copy(headForward = 0.07f, neckFlex = 12f),
                    STAND.copy(headForward = -0.02f, neckFlex = -4f),
                    STAND.copy(headForward = -0.02f, neckFlex = -4f),
                ),
                periodSec = 3.6f,
            ),
        ),
        Exercise(
            id = "wall_angel",
            name = "벽 천사",
            summary = "말린 어깨를 펴고 등 근육을 깨우는 운동",
            category = STRETCH,
            dose = "10회 × 2세트",
            steps = listOf(
                "벽에 뒤통수, 등, 엉덩이를 붙이고 서요.",
                "팔을 W자로 만들어 팔꿈치와 손등을 벽에 대요.",
                "벽에서 떨어지지 않게 팔을 위로 천천히 밀어 올려요.",
                "다시 W자로 내려와요.",
            ),
            tips = "허리가 벽에서 뜨면 무릎을 살짝 굽혀요.",
            posture = setOf(P.ROUNDED_SHOULDERS, P.ROUNDED_BACK, P.FORWARD_HEAD),
            motion = Motion(
                listOf(
                    STAND.arms(ArmPose(flex = 0f, abd = 80f, foreFlex = 180f, foreAbd = -10f)),
                    STAND.arms(ArmPose(flex = 0f, abd = 150f, foreFlex = 180f, foreAbd = -20f)),
                ),
                periodSec = 3.2f,
            ),
        ),
        Exercise(
            id = "prone_y",
            name = "엎드려 Y자 들기",
            summary = "등 위쪽과 어깨 뒤쪽을 강하게 만들어 굽은 등을 펴요",
            category = STRENGTH,
            dose = "12회 × 3세트",
            steps = listOf(
                "바닥에 엎드리고 팔을 머리 위로 Y자로 뻗어요.",
                "엄지를 하늘로 향하게 해요.",
                "어깨뼈를 모으며 팔과 가슴을 살짝 들어요.",
                "2초 버티고 내려와요.",
            ),
            tips = "목은 꺾지 말고 시선은 바닥을 봐요.",
            posture = setOf(P.ROUNDED_SHOULDERS, P.ROUNDED_BACK, P.UNEVEN_SHOULDERS),
            motion = Motion(
                listOf(
                    Pose(bodyPitch = 90f).arms(ArmPose.straight(180f, -30f)),
                    Pose(bodyPitch = 90f, trunkFlex = -12f).arms(ArmPose.straight(205f, -30f)),
                    Pose(bodyPitch = 90f, trunkFlex = -12f).arms(ArmPose.straight(205f, -30f)),
                ),
                periodSec = 3f,
                anchor = Anchor.PELVIS,
            ),
        ),
        Exercise(
            id = "cat_cow",
            name = "고양이-소 자세",
            summary = "굳은 척추를 부드럽게 풀어 허리와 등 정렬을 맞춰요",
            category = STRETCH,
            dose = "천천히 10회",
            steps = listOf(
                "손은 어깨 아래, 무릎은 골반 아래에 두고 네발 자세를 만들어요.",
                "숨을 내쉬며 등을 둥글게 말고 배꼽을 봐요(고양이).",
                "숨을 들이쉬며 가슴을 열고 시선을 앞으로 해요(소).",
            ),
            tips = "허리만 꺾지 말고 등 전체를 고르게 움직여요.",
            posture = setOf(P.ROUNDED_BACK, P.ANTERIOR_PELVIC_TILT),
            motion = Motion(
                listOf(
                    QUADRUPED.copy(spineCurve = 40f, neckFlex = 35f),
                    QUADRUPED.copy(spineCurve = -30f, neckFlex = -30f),
                ),
                periodSec = 4f,
                anchor = Anchor.HANDS,
            ),
        ),
        Exercise(
            id = "glute_bridge",
            name = "글루트 브릿지",
            summary = "엉덩이 근육을 깨워 앞으로 기운 골반을 바로 세워요",
            category = STRENGTH,
            dose = "15회 × 3세트",
            steps = listOf(
                "누워서 무릎을 세우고 발은 골반 너비로 벌려요.",
                "배에 힘을 주고 엉덩이를 조이며 골반을 들어요.",
                "어깨부터 무릎까지 일직선이 되면 2초 버텨요.",
                "천천히 내려와요.",
            ),
            tips = "허리가 아닌 엉덩이로 드는 느낌이 나야 해요.",
            posture = setOf(P.ANTERIOR_PELVIC_TILT, P.PELVIC_IMBALANCE),
            concerns = setOf(C.FLAT_HIP, C.THICK_THIGHS),
            motion = Motion(
                listOf(
                    HOOK_LYING,
                    HOOK_LYING.arms(ArmPose.straight(14f, 15f)).copy(bodyPitch = -112f, leftLeg = LegPose(-4f, 3f, 104f, 30f), rightLeg = LegPose(-4f, 3f, 104f, 30f)),
                    HOOK_LYING.arms(ArmPose.straight(14f, 15f)).copy(bodyPitch = -112f, leftLeg = LegPose(-4f, 3f, 104f, 30f), rightLeg = LegPose(-4f, 3f, 104f, 30f)),
                ),
                periodSec = 3.2f,
            ),
        ),
        Exercise(
            id = "dead_bug",
            name = "데드버그",
            summary = "허리를 바닥에 붙인 채 코어를 단련해요",
            category = STRENGTH,
            dose = "좌우 10회씩 × 2세트",
            steps = listOf(
                "누워서 팔은 천장으로, 무릎은 90도로 들어요.",
                "허리를 바닥에 꾹 붙여요.",
                "오른팔과 왼다리를 동시에 천천히 뻗어요.",
                "제자리로 돌아와 반대쪽도 해요.",
            ),
            tips = "허리가 뜨면 뻗는 범위를 줄여요.",
            posture = setOf(P.ANTERIOR_PELVIC_TILT),
            concerns = setOf(C.BELLY_FAT),
            motion = run {
                val base = Pose(bodyPitch = -90f).arms(ArmPose.straight(90f, 8f)).legs(LegPose(90f, 3f, 90f))
                Motion(
                    listOf(
                        base,
                        base.copy(rightArm = ArmPose.straight(170f, 8f), leftLeg = LegPose(15f, 3f, 5f)),
                        base,
                        base.copy(leftArm = ArmPose.straight(170f, 8f), rightLeg = LegPose(15f, 3f, 5f)),
                    ),
                    periodSec = 5f,
                    anchor = Anchor.PELVIS,
                )
            },
        ),
        Exercise(
            id = "plank",
            name = "플랭크",
            summary = "몸통 전체를 단단하게 잡아주는 기본 코어 운동",
            category = STRENGTH,
            dose = "30초 × 3세트",
            steps = listOf(
                "팔꿈치를 어깨 아래에 두고 엎드려요.",
                "발끝으로 버티며 몸을 들어 머리부터 발까지 일직선을 만들어요.",
                "배와 엉덩이에 힘을 준 채 숨을 쉬며 버텨요.",
            ),
            tips = "엉덩이가 처지거나 너무 올라가지 않게 거울로 확인해요.",
            posture = setOf(P.ANTERIOR_PELVIC_TILT),
            concerns = setOf(C.BELLY_FAT),
            motion = run {
                val p = Pose(bodyPitch = 78f).arms(ArmPose(flex = 78f, abd = 6f, foreFlex = 170f, foreAbd = -8f))
                    .legs(LegPose(ankle = -50f))
                Motion(listOf(p, p.copy(bodyPitch = 76f)), periodSec = 3f, anchor = Anchor.FEET)
            },
        ),
        Exercise(
            id = "squat",
            name = "스쿼트",
            summary = "허벅지와 엉덩이를 함께 쓰는 하체 대표 운동",
            category = STRENGTH,
            dose = "15회 × 3세트",
            steps = listOf(
                "발을 어깨 너비로 벌리고 발끝은 살짝 바깥으로 해요.",
                "의자에 앉듯 엉덩이를 뒤로 빼며 내려가요.",
                "허벅지가 바닥과 평행해지면 발뒤꿈치로 밀며 일어나요.",
            ),
            tips = "무릎이 안쪽으로 모이지 않게 발끝 방향과 맞춰요.",
            posture = setOf(P.KNOCK_KNEES),
            concerns = setOf(C.THICK_THIGHS, C.FLAT_HIP, C.LOW_STAMINA),
            motion = Motion(
                listOf(
                    STAND.legs(LegPose(hipAbd = 10f)).arms(ArmPose.straight(10f)),
                    STAND.copy(trunkFlex = 40f).legs(LegPose(95f, 14f, 105f, -10f)).arms(ArmPose.straight(55f)),
                ),
                periodSec = 2.6f,
            ),
        ),
        Exercise(
            id = "sumo_squat",
            name = "와이드 스쿼트",
            summary = "허벅지 안쪽을 집중적으로 써서 안쪽 살을 정리해요",
            category = STRENGTH,
            dose = "15회 × 3세트",
            steps = listOf(
                "발을 어깨보다 넓게 벌리고 발끝을 45도 바깥으로 향해요.",
                "상체를 세운 채 엉덩이를 아래로 내려요.",
                "허벅지 안쪽에 힘을 주며 일어나요.",
            ),
            tips = "무릎이 발끝 방향을 따라가야 해요.",
            posture = setOf(P.BOW_LEGS),
            concerns = setOf(C.THICK_THIGHS, C.SADDLEBAGS),
            motion = Motion(
                listOf(
                    STAND.legs(LegPose(hipAbd = 24f)).arms(ArmPose.bent(30f, 60f)),
                    STAND.copy(trunkFlex = 12f).legs(LegPose(70f, 40f, 95f, -20f)).arms(ArmPose.bent(30f, 60f)),
                ),
                periodSec = 2.8f,
            ),
        ),
        Exercise(
            id = "lunge",
            name = "런지",
            summary = "한쪽씩 하체를 단련해 좌우 균형과 허벅지 라인을 잡아요",
            category = STRENGTH,
            dose = "좌우 10회씩 × 2세트",
            steps = listOf(
                "한 발을 크게 앞으로 내디뎌요.",
                "두 무릎이 90도가 될 때까지 몸을 수직으로 내려요.",
                "앞발 뒤꿈치로 밀며 올라와요.",
            ),
            tips = "앞무릎이 발끝보다 너무 앞으로 나가지 않게 해요.",
            posture = setOf(P.PELVIC_IMBALANCE),
            concerns = setOf(C.THICK_THIGHS, C.FLAT_HIP),
            motion = Motion(
                listOf(
                    STAND.copy(leftLeg = LegPose(30f, 3f, 5f), rightLeg = LegPose(-25f, 3f, 5f, -30f)),
                    STAND.copy(leftLeg = LegPose(85f, 3f, 90f), rightLeg = LegPose(-15f, 3f, 95f, -50f)),
                ),
                periodSec = 2.8f,
            ),
        ),
        Exercise(
            id = "side_leg_raise",
            name = "옆으로 누워 다리 들기",
            summary = "엉덩이 옆 근육을 써서 승마살과 X다리를 잡아요",
            category = STRENGTH,
            dose = "좌우 15회씩 × 2세트",
            steps = listOf(
                "옆으로 누워 아래팔로 머리를 받쳐요.",
                "위쪽 다리를 곧게 편 채 발끝을 살짝 몸쪽으로 당겨요.",
                "다리를 45도까지 들었다가 천천히 내려요.",
            ),
            tips = "골반이 뒤로 넘어가지 않게 몸통을 고정해요.",
            posture = setOf(P.KNOCK_KNEES, P.PELVIC_IMBALANCE),
            concerns = setOf(C.SADDLEBAGS, C.THICK_THIGHS),
            motion = Motion(
                listOf(SIDE_LYING, SIDE_LYING.copy(leftLeg = LegPose(hipAbd = 45f))),
                periodSec = 2.4f,
                anchor = Anchor.PELVIS,
            ),
        ),
        Exercise(
            id = "clamshell",
            name = "클램쉘",
            summary = "조개처럼 무릎을 열어 엉덩이 옆과 골반 균형을 잡아요",
            category = STRENGTH,
            dose = "좌우 15회씩 × 2세트",
            steps = listOf(
                "옆으로 누워 무릎을 45도로 굽혀요.",
                "발은 붙인 채 위쪽 무릎만 천천히 열어요.",
                "골반이 뒤로 넘어가지 않게 하고 다시 닫아요.",
            ),
            tips = "밴드를 무릎 위에 감으면 더 효과적이에요.",
            posture = setOf(P.KNOCK_KNEES, P.PELVIC_IMBALANCE),
            concerns = setOf(C.SADDLEBAGS, C.FLAT_HIP),
            motion = run {
                val bent = SIDE_LYING.legs(LegPose(50f, 0f, 80f))
                Motion(
                    listOf(bent, bent.copy(leftLeg = LegPose(50f, 40f, 80f))),
                    periodSec = 2.4f,
                    anchor = Anchor.PELVIS,
                )
            },
        ),
        Exercise(
            id = "knee_squeeze",
            name = "누워서 무릎 모으기",
            summary = "허벅지 안쪽 근육을 강화해 O다리 정렬을 도와요",
            category = STRENGTH,
            dose = "5초 × 12회",
            steps = listOf(
                "누워서 무릎을 세우고 무릎 사이에 쿠션을 끼워요.",
                "쿠션을 꾹 누르며 5초 버텨요.",
                "천천히 힘을 풀어요.",
            ),
            tips = "쿠션이 없으면 접은 수건이나 공을 써도 돼요.",
            posture = setOf(P.BOW_LEGS),
            concerns = setOf(C.THICK_THIGHS),
            motion = Motion(
                listOf(
                    HOOK_LYING.legs(LegPose(50f, 18f, 100f, 30f)),
                    HOOK_LYING.legs(LegPose(50f, 1f, 100f, 30f)),
                    HOOK_LYING.legs(LegPose(50f, 1f, 100f, 30f)),
                ),
                periodSec = 3.6f,
                anchor = Anchor.PELVIS,
            ),
        ),
        Exercise(
            id = "side_stretch",
            name = "옆구리 늘리기",
            summary = "짧아진 옆구리를 늘려 어깨와 골반 높이를 맞춰요",
            category = STRETCH,
            dose = "좌우 15초씩 × 3회",
            steps = listOf(
                "발을 골반 너비로 벌리고 서요.",
                "한 팔을 머리 위로 뻗어요.",
                "반대쪽으로 천천히 상체를 기울여 옆구리를 늘려요.",
            ),
            tips = "높은 쪽 어깨 방향을 더 길게 늘려 주세요.",
            posture = setOf(P.UNEVEN_SHOULDERS, P.PELVIC_IMBALANCE),
            motion = Motion(
                listOf(
                    STAND.copy(trunkSide = -22f, leftArm = ArmPose.straight(0f, 165f), rightArm = ArmPose.bent(0f, 40f, 20f)),
                    STAND.copy(leftArm = ArmPose.straight(0f, 120f), rightArm = ArmPose.straight(0f, 10f)),
                    STAND.copy(trunkSide = 22f, rightArm = ArmPose.straight(0f, 165f), leftArm = ArmPose.bent(0f, 40f, 20f)),
                    STAND.copy(rightArm = ArmPose.straight(0f, 120f), leftArm = ArmPose.straight(0f, 10f)),
                ),
                periodSec = 6f,
            ),
        ),
        Exercise(
            id = "calf_stretch",
            name = "벽 밀고 종아리 늘리기",
            summary = "뭉친 종아리를 늘려 알을 부드럽게 풀어요",
            category = STRETCH,
            dose = "좌우 30초씩 × 2회",
            steps = listOf(
                "벽을 마주 보고 두 손으로 벽을 짚어요.",
                "한 발을 뒤로 빼고 뒤꿈치를 바닥에 붙여요.",
                "앞무릎을 굽히며 몸을 벽 쪽으로 밀어 뒷종아리를 늘려요.",
            ),
            tips = "뒷발 발끝이 정면을 향해야 잘 늘어나요.",
            concerns = setOf(C.CALF_MUSCLE),
            motion = Motion(
                listOf(
                    STAND.copy(trunkFlex = 15f, leftLeg = LegPose(20f, 3f, 25f), rightLeg = LegPose(-25f, 3f, 0f, -25f))
                        .arms(ArmPose.straight(80f)),
                    STAND.copy(trunkFlex = 22f, leftLeg = LegPose(30f, 3f, 45f), rightLeg = LegPose(-32f, 3f, 0f, -32f))
                        .arms(ArmPose.bent(75f, 40f)),
                ),
                periodSec = 4f,
            ),
        ),
        Exercise(
            id = "tricep_kickback",
            name = "트라이셉스 킥백",
            summary = "팔 뒤쪽 근육을 써서 처진 팔뚝을 탄탄하게 해요",
            category = STRENGTH,
            dose = "15회 × 3세트",
            steps = listOf(
                "물병을 들고 무릎을 살짝 굽혀 상체를 45도 숙여요.",
                "팔꿈치를 옆구리에 붙이고 90도로 굽혀요.",
                "팔꿈치는 고정한 채 아래팔을 뒤로 쭉 펴요.",
            ),
            tips = "팔꿈치가 내려가지 않게 위치를 지켜요.",
            concerns = setOf(C.ARM_FAT),
            motion = Motion(
                listOf(
                    STAND.copy(trunkFlex = 45f).legs(LegPose(35f, 6f, 25f)).arms(ArmPose(-70f, 8f, 20f, 8f)),
                    STAND.copy(trunkFlex = 45f).legs(LegPose(35f, 6f, 25f)).arms(ArmPose.straight(-75f)),
                ),
                periodSec = 2.2f,
            ),
        ),
        Exercise(
            id = "jumping_jack",
            name = "팔 벌려 뛰기",
            summary = "온몸을 써서 심박수를 올리는 쉬운 유산소",
            category = CARDIO,
            dose = "40초 × 3세트",
            steps = listOf(
                "발을 모으고 팔은 옆에 두고 서요.",
                "점프하며 발을 벌리고 팔을 머리 위로 올려요.",
                "다시 점프하며 처음 자세로 돌아와요.",
            ),
            tips = "층간소음이 걱정되면 점프 없이 한 발씩 옆으로 내디뎌요.",
            concerns = setOf(C.LOW_STAMINA, C.BELLY_FAT, C.THICK_THIGHS),
            motion = Motion(
                listOf(
                    STAND.legs(LegPose(hipAbd = 2f)).arms(ARMS_DOWN),
                    STAND.copy(lift = 0.08f).legs(LegPose(hipAbd = 14f, ankle = -30f)).arms(ArmPose.straight(0f, 155f)),
                ),
                periodSec = 1.1f,
            ),
        ),
        Exercise(
            id = "high_knees",
            name = "제자리 무릎 들어 걷기",
            summary = "무릎을 높이 들며 걸어 체력과 하체 라인을 함께 챙겨요",
            category = CARDIO,
            dose = "1분 × 3세트",
            steps = listOf(
                "바르게 서서 팔을 90도로 굽혀요.",
                "한쪽 무릎을 골반 높이까지 들어요.",
                "팔을 반대로 흔들며 번갈아 들어요.",
            ),
            tips = "익숙해지면 속도를 올려 제자리 달리기로 바꿔요.",
            concerns = setOf(C.LOW_STAMINA, C.BELLY_FAT, C.CALF_MUSCLE),
            motion = Motion(
                listOf(
                    STAND.copy(leftLeg = LegPose(80f, 3f, 90f), leftArm = ArmPose.bent(-25f, 90f), rightArm = ArmPose.bent(35f, 90f)),
                    STAND.copy(leftArm = ArmPose.bent(5f, 90f), rightArm = ArmPose.bent(5f, 90f)),
                    STAND.copy(rightLeg = LegPose(80f, 3f, 90f), rightArm = ArmPose.bent(-25f, 90f), leftArm = ArmPose.bent(35f, 90f)),
                    STAND.copy(leftArm = ArmPose.bent(5f, 90f), rightArm = ArmPose.bent(5f, 90f)),
                ),
                periodSec = 1.6f,
            ),
        ),
    )

    private val byId = all.associateBy { it.id }

    fun find(id: String): Exercise? = byId[id]
}
