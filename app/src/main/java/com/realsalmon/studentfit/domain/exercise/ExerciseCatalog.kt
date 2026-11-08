package com.realsalmon.studentfit.domain.exercise

import com.realsalmon.studentfit.domain.BodyConcern as C
import com.realsalmon.studentfit.domain.PostureIssue as P
import com.realsalmon.studentfit.domain.exercise.ExerciseCategory.CARDIO
import com.realsalmon.studentfit.domain.exercise.ExerciseCategory.STRENGTH
import com.realsalmon.studentfit.domain.exercise.ExerciseCategory.STRETCH
import com.realsalmon.studentfit.domain.motion.Anchor
import com.realsalmon.studentfit.domain.motion.ArmPose
import com.realsalmon.studentfit.domain.motion.Key
import com.realsalmon.studentfit.domain.motion.LegPose
import com.realsalmon.studentfit.domain.motion.Motion
import com.realsalmon.studentfit.domain.motion.Pose
import com.realsalmon.studentfit.domain.motion.ViewAngle

/**
 * 앱에 들어 있는 교정 운동 목록.
 * 3D 동작은 관절 각도 키프레임이고, 동작 한 바퀴가 실제 1회 속도에 맞춰져 있어요.
 */
object ExerciseCatalog {

    private val STAND = Pose()

    // 엎드려 네발 자세(손은 어깨 아래, 무릎은 골반 아래)
    private val QUADRUPED = Pose(
        bodyPitch = 90f,
        leftArm = ArmPose.straight(90f, 4f), rightArm = ArmPose.straight(90f, 4f),
        leftLeg = LegPose(hipFlex = 90f, knee = 90f, ankle = -60f),
        rightLeg = LegPose(hipFlex = 90f, knee = 90f, ankle = -60f),
    )

    // 누워서 무릎 세운 자세(발은 바닥, 팔은 몸 옆 바닥)
    private val HOOK_LYING = Pose(
        bodyPitch = -90f,
        leftArm = ArmPose.straight(-8f, 14f), rightArm = ArmPose.straight(-8f, 14f),
        leftLeg = LegPose(hipFlex = 50f, knee = 100f, ankle = 30f),
        rightLeg = LegPose(hipFlex = 50f, knee = 100f, ankle = 30f),
    )

    // 오른쪽으로 옆으로 누운 자세(아래팔로 머리 받침, 아래 다리는 살짝 굽힘)
    private val SIDE_LYING = Pose(
        bodyRoll = 90f,
        leftArm = ArmPose(flex = 60f, abd = -50f, foreFlex = 90f, foreAbd = -70f),
        rightArm = ArmPose(flex = 175f, abd = 0f, foreFlex = 175f, foreAbd = 0f),
        leftLeg = LegPose(hipFlex = 0f, hipAbd = 0f),
        rightLeg = LegPose(hipFlex = 25f, hipAbd = 0f, knee = 40f),
    )

    private fun hold(p: Pose, sec: Float) = Key(p, sec)

    val all: List<Exercise> = listOf(
        Exercise(
            id = "chin_tuck",
            name = "턱 당기기",
            summary = "앞으로 나온 머리를 몸통 위로 되돌리는 거북목 기본 운동",
            category = STRETCH,
            muscles = "목 앞쪽 깊은 근육(심부 목 굴곡근)",
            rx = Prescription(sets = 2, reps = 10, holdSec = 5, restSec = 20),
            setup = listOf(
                "벽에 등을 대고 서요. 뒤꿈치는 벽에서 한 뼘 떨어뜨려요.",
                "어깨 힘을 빼고 시선은 정면 수평으로 둬요.",
            ),
            steps = listOf(
                "턱을 아래로 숙이지 말고, 수평으로 뒤로 밀어요.",
                "이중턱이 생기고 목 뒤가 길어지는 느낌이 들면 멈춰요.",
                "그대로 5초 버텨요.",
                "천천히 힘을 풀어 처음 자리로 돌아와요.",
            ),
            breathing = "버티는 동안 숨을 참지 말고 코로 편하게 쉬어요.",
            mistakes = listOf("고개를 끄덕이듯 숙이기", "어깨를 같이 으쓱하기", "아플 만큼 세게 밀기"),
            tips = "손가락 하나를 턱에 대고 뒤로 민다는 느낌으로 하면 쉬워요. 공부하다가 30분마다 5회씩 하면 좋아요.",
            posture = setOf(P.FORWARD_HEAD),
            view = ViewAngle(75f, 6f),
            focusUpper = true,
            motion = run {
                val relaxed = STAND.copy(headForward = 0.085f, neckFlex = 14f, shoulderForward = 0.02f)
                val tucked = STAND.copy(headForward = -0.035f, neckFlex = -4f)
                Motion(listOf(Key(relaxed, 1f), hold(tucked, 5f), Key(tucked, 1f)), periodSec = 7f)
            },
        ),
        Exercise(
            id = "wall_angel",
            name = "벽 천사",
            summary = "말린 어깨를 펴고 등 위쪽 근육을 깨우는 운동",
            category = STRETCH,
            muscles = "아래 승모근, 앞톱니근, 가슴 근육 늘리기",
            rx = Prescription(sets = 2, reps = 10, restSec = 30),
            setup = listOf(
                "벽에 뒤통수, 등, 엉덩이를 붙이고 발은 벽에서 한 뼘 떨어뜨려요.",
                "무릎은 살짝 굽히고, 허리와 벽 사이가 손바닥 하나 정도 되게 해요.",
                "팔을 W자로 만들어 팔꿈치와 손등을 벽에 대요.",
            ),
            steps = listOf(
                "팔이 벽에서 떨어지지 않게 미끄러뜨리듯 머리 위로 올려요.",
                "팔이 Y자가 되면 1초 멈춰요.",
                "날개뼈를 아래로 끌어내리며 다시 W자로 내려와요.",
            ),
            breathing = "올릴 때 내쉬고, 내릴 때 들이쉬어요.",
            mistakes = listOf("허리가 벽에서 뜨고 갈비뼈가 들림", "팔꿈치나 손등이 벽에서 떨어짐", "어깨가 귀 쪽으로 올라감"),
            tips = "팔이 벽에 안 닿으면 닿는 높이까지만 해도 돼요. 범위는 점점 늘어나요.",
            posture = setOf(P.ROUNDED_SHOULDERS, P.ROUNDED_BACK, P.FORWARD_HEAD),
            view = ViewAngle(18f, 8f),
            motion = run {
                val base = STAND.legs(LegPose(hipFlex = 8f, hipAbd = 5f, knee = 12f))
                val w = base.arms(ArmPose(flex = 0f, abd = 85f, foreFlex = 180f, foreAbd = -5f))
                val y = base.arms(ArmPose(flex = 0f, abd = 160f, foreFlex = 180f, foreAbd = -20f))
                Motion(listOf(Key(w, 1.6f), Key(y, 0.6f), Key(y, 1.8f)), periodSec = 4f)
            },
        ),
        Exercise(
            id = "prone_y",
            name = "엎드려 Y자 들기",
            summary = "등 위쪽과 어깨 뒤쪽을 강하게 만들어 굽은 등을 펴요",
            category = STRENGTH,
            muscles = "아래·중간 승모근, 어깨 뒤쪽(후면 삼각근)",
            rx = Prescription(sets = 3, reps = 10, restSec = 30),
            setup = listOf(
                "바닥에 엎드리고 이마 밑에 접은 수건을 둬요.",
                "팔을 머리 위로 Y자로 뻗고 엄지를 하늘로 향해요.",
            ),
            steps = listOf(
                "날개뼈를 뒤·아래로 모아요.",
                "팔을 바닥에서 손바닥 한 뼘 높이까지 들어요.",
                "2초 버티고 천천히 내려요.",
            ),
            breathing = "들 때 내쉬고, 내릴 때 들이쉬어요.",
            mistakes = listOf("고개를 들어 목이 꺾임", "허리를 젖혀서 들기", "어깨가 귀 쪽으로 올라감"),
            tips = "이마는 수건에 둔 채 팔만 든다고 생각해요. 높이보다 날개뼈 모으는 느낌이 중요해요.",
            posture = setOf(P.ROUNDED_SHOULDERS, P.ROUNDED_BACK, P.UNEVEN_SHOULDERS),
            view = ViewAngle(100f, 18f),
            motion = run {
                val down = Pose(bodyPitch = 90f, neckFlex = 5f).arms(ArmPose.straight(178f, -32f))
                val up = Pose(bodyPitch = 90f, trunkFlex = -5f, neckFlex = 5f).arms(ArmPose.straight(200f, -32f))
                Motion(listOf(Key(down, 1f), Key(up, 2f), Key(up, 1.2f)), periodSec = 4.2f, anchor = Anchor.PELVIS)
            },
        ),
        Exercise(
            id = "cat_cow",
            name = "고양이-소 자세",
            summary = "굳은 척추를 마디마디 움직여 등과 허리 정렬을 풀어요",
            category = STRETCH,
            muscles = "척추 주변 근육, 복근, 등 근육",
            rx = Prescription(sets = 2, reps = 8, restSec = 20),
            setup = listOf(
                "손은 어깨 바로 아래, 무릎은 골반 바로 아래에 두고 네발 자세를 만들어요.",
                "등은 평평하게, 시선은 바닥을 봐요.",
            ),
            steps = listOf(
                "숨을 내쉬며 꼬리뼈를 말고 등을 천장 쪽으로 둥글게 밀어요. 시선은 배꼽(고양이).",
                "2초 머물러요.",
                "숨을 들이쉬며 가슴을 앞으로 내밀고 꼬리뼈를 들어요. 시선은 살짝 앞(소).",
                "2초 머문 뒤 다시 고양이로 넘어가요.",
            ),
            breathing = "고양이에서 내쉬고, 소에서 들이쉬어요. 숨에 맞춰 천천히 움직여요.",
            mistakes = listOf("허리만 꺾고 등 위쪽은 안 움직임", "팔꿈치를 굽혀 몸이 내려감", "목만 크게 움직임"),
            tips = "오리엉덩이라면 '고양이' 쪽을 더 길게 머무르면 좋아요.",
            posture = setOf(P.ROUNDED_BACK, P.ANTERIOR_PELVIC_TILT),
            view = ViewAngle(90f, 8f),
            motion = run {
                // 등이 굽어도 팔은 바닥에 수직으로 남게 위쪽 척추 회전만큼 팔 각도를 보정해요.
                val cat = QUADRUPED.copy(spineCurve = 45f, neckFlex = 40f).arms(ArmPose.straight(90f + 22.5f, 4f))
                val cow = QUADRUPED.copy(spineCurve = -32f, neckFlex = -28f).arms(ArmPose.straight(90f - 16f, 4f))
                Motion(
                    listOf(Key(QUADRUPED, 1f), Key(cat, 2f), Key(cat, 1f), Key(QUADRUPED, 1f), Key(cow, 2f), Key(cow, 1f)),
                    periodSec = 8f,
                    anchor = Anchor.HANDS,
                )
            },
        ),
        Exercise(
            id = "glute_bridge",
            name = "글루트 브릿지",
            summary = "엉덩이 근육을 깨워 앞으로 기운 골반을 바로 세워요",
            category = STRENGTH,
            muscles = "큰볼기근(대둔근), 햄스트링, 코어",
            rx = Prescription(sets = 3, reps = 12, restSec = 40),
            setup = listOf(
                "누워서 무릎을 세우고 발은 골반 너비로 둬요.",
                "팔을 뻗었을 때 손끝이 뒤꿈치에 닿을 정도로 발을 당겨요.",
                "팔은 몸 옆 바닥에 둬요.",
            ),
            steps = listOf(
                "배에 살짝 힘을 주고 꼬리뼈를 말아요.",
                "뒤꿈치로 바닥을 밀며 엉덩이를 들어요.",
                "어깨-골반-무릎이 일직선이 되면 엉덩이를 꽉 조이고 2초 버텨요.",
                "등부터 엉덩이 순서로 천천히 내려와요.",
            ),
            breathing = "올릴 때 내쉬고, 내릴 때 들이쉬어요.",
            mistakes = listOf("허리를 젖혀 너무 높이 들기", "무릎이 안쪽으로 모이거나 바깥으로 벌어짐", "발끝으로 밀어 허벅지 앞만 힘듦"),
            tips = "허벅지 뒤가 쥐가 나면 발을 엉덩이 쪽으로 조금 더 당겨요.",
            posture = setOf(P.ANTERIOR_PELVIC_TILT, P.PELVIC_IMBALANCE),
            concerns = setOf(C.FLAT_HIP, C.THICK_THIGHS),
            view = ViewAngle(80f, 10f),
            motion = run {
                val top = HOOK_LYING.arms(ArmPose.straight(14f, 14f)).copy(
                    bodyPitch = -112f,
                    leftLeg = LegPose(-4f, 3f, 104f, 30f),
                    rightLeg = LegPose(-4f, 3f, 104f, 30f),
                )
                Motion(listOf(Key(HOOK_LYING, 1.4f), Key(top, 2f), Key(top, 1.6f)), periodSec = 5f)
            },
        ),
        Exercise(
            id = "dead_bug",
            name = "데드버그",
            summary = "허리를 바닥에 붙인 채 팔다리를 움직여 코어를 단련해요",
            category = STRENGTH,
            level = Level.NORMAL,
            muscles = "복근(복횡근, 복직근), 골반 주변 안정근",
            rx = Prescription(sets = 3, reps = 8, restSec = 40),
            setup = listOf(
                "누워서 팔을 천장으로 곧게 뻗어요.",
                "무릎과 골반을 90도로 굽혀 다리를 들어요(테이블 자세).",
                "허리와 바닥 사이에 틈이 없게 배를 납작하게 눌러요.",
            ),
            steps = listOf(
                "숨을 내쉬며 오른팔은 머리 위로, 왼다리는 앞으로 천천히 뻗어요.",
                "바닥에 닿기 직전에 멈춰요.",
                "처음 자세로 돌아와 반대쪽(왼팔·오른다리)도 해요.",
                "양쪽을 한 번씩 하면 1회예요.",
            ),
            breathing = "뻗을 때 길게 내쉬고, 돌아올 때 들이쉬어요.",
            mistakes = listOf("허리가 바닥에서 뜸", "빠르게 휘두르듯 움직임", "고개를 들어 목에 힘이 들어감"),
            tips = "허리가 뜨면 다리를 덜 뻗거나 발뒤꿈치를 바닥에 톡 대는 쉬운 버전으로 해요.",
            posture = setOf(P.ANTERIOR_PELVIC_TILT),
            concerns = setOf(C.BELLY_FAT),
            view = ViewAngle(100f, 20f),
            motion = run {
                val base = Pose(bodyPitch = -90f).arms(ArmPose.straight(90f, 8f)).legs(LegPose(90f, 4f, 90f))
                val a = base.copy(rightArm = ArmPose.straight(172f, 8f), leftLeg = LegPose(12f, 4f, 4f))
                val b = base.copy(leftArm = ArmPose.straight(172f, 8f), rightLeg = LegPose(12f, 4f, 4f))
                Motion(
                    listOf(Key(base, 1.5f), Key(a, 0.6f), Key(a, 1.5f), Key(base, 1.5f), Key(b, 0.6f), Key(b, 1.5f)),
                    periodSec = 7f,
                    anchor = Anchor.PELVIS,
                )
            },
        ),
        Exercise(
            id = "plank",
            name = "플랭크",
            summary = "몸통 전체를 단단하게 잡아주는 기본 코어 운동",
            category = STRENGTH,
            muscles = "복근 전체, 엉덩이, 어깨 안정근",
            rx = Prescription(sets = 3, holdSec = 30, restSec = 30),
            setup = listOf(
                "엎드려서 팔꿈치를 어깨 바로 아래에 둬요.",
                "아래팔은 나란히, 발은 골반 너비로 벌려요.",
            ),
            steps = listOf(
                "발끝으로 바닥을 밀며 몸을 들어요.",
                "머리-등-엉덩이-발뒤꿈치가 일직선이 되게 해요.",
                "배꼽을 등 쪽으로 당기고 엉덩이를 살짝 조인 채 버텨요.",
                "시간이 끝나면 무릎부터 바닥에 내려요.",
            ),
            breathing = "숨을 참지 말고 짧게 끊지 않게 고르게 쉬어요.",
            mistakes = listOf("엉덩이가 처져 허리가 꺾임", "엉덩이가 너무 솟음", "어깨가 귀 쪽으로 으쓱"),
            tips = "30초가 힘들면 무릎을 바닥에 댄 무릎 플랭크로 시작해요.",
            posture = setOf(P.ANTERIOR_PELVIC_TILT),
            concerns = setOf(C.BELLY_FAT),
            view = ViewAngle(75f, 10f),
            holdAtSec = 3f,
            motion = run {
                val plank = Pose(bodyPitch = 79f).arms(ArmPose(flex = 79f, abd = 5f, foreFlex = 170f, foreAbd = -6f))
                    .legs(LegPose(hipAbd = 3f, ankle = -50f))
                // 무릎을 바닥에 댄 시작 자세: 허벅지는 비스듬히, 정강이는 바닥에 눕혀요.
                val knees = Pose(bodyPitch = 62f).arms(ArmPose(flex = 62f, abd = 5f, foreFlex = 152f, foreAbd = -6f))
                    .legs(LegPose(hipFlex = 35f, hipAbd = 3f, knee = 63f, ankle = 20f))
                Motion(listOf(Key(knees, 1.2f), Key(plank, 4f), Key(plank, 1f)), periodSec = 6.2f, anchor = Anchor.HANDS)
            },
        ),
        Exercise(
            id = "squat",
            name = "스쿼트",
            summary = "허벅지와 엉덩이를 함께 쓰는 하체 대표 운동",
            category = STRENGTH,
            muscles = "허벅지 앞(대퇴사두근), 엉덩이, 허벅지 뒤",
            rx = Prescription(sets = 3, reps = 12, restSec = 45),
            setup = listOf(
                "발을 어깨 너비로 벌리고 발끝은 바깥으로 15~30도 열어요.",
                "가슴을 펴고 시선은 정면을 봐요.",
            ),
            steps = listOf(
                "엉덩이를 뒤로 빼며 의자에 앉듯 내려가요. 팔은 앞으로 뻗어 균형을 잡아요.",
                "무릎은 발끝과 같은 방향을 향하게 해요.",
                "허벅지가 바닥과 평행해질 때까지 내려가요.",
                "뒤꿈치로 바닥을 밀며 일어나고, 맨 위에서 엉덩이를 조여요.",
            ),
            breathing = "내려갈 때 들이쉬고, 올라올 때 내쉬어요.",
            mistakes = listOf("무릎이 안쪽으로 모임", "뒤꿈치가 들림", "등이 둥글게 굽음"),
            tips = "의자를 뒤에 두고 엉덩이가 살짝 닿으면 일어나는 '박스 스쿼트'로 연습하면 자세가 빨리 잡혀요.",
            posture = setOf(P.KNOCK_KNEES),
            concerns = setOf(C.THICK_THIGHS, C.FLAT_HIP, C.LOW_STAMINA),
            view = ViewAngle(62f, 8f),
            motion = run {
                val top = STAND.legs(LegPose(hipAbd = 10f, ankle = 0f)).arms(ArmPose.straight(8f))
                val bottom = STAND.copy(trunkFlex = 38f).legs(LegPose(95f, 14f, 108f, -14f)).arms(ArmPose.straight(62f, 10f))
                Motion(listOf(Key(top, 1.8f), Key(bottom, 0.3f), Key(bottom, 1.4f)), periodSec = 3.5f)
            },
        ),
        Exercise(
            id = "sumo_squat",
            name = "와이드 스쿼트",
            summary = "다리를 넓게 벌리고 앉아 허벅지 안쪽과 엉덩이를 써요",
            category = STRENGTH,
            muscles = "허벅지 안쪽(내전근), 엉덩이, 허벅지 앞",
            rx = Prescription(sets = 3, reps = 12, restSec = 45),
            setup = listOf(
                "발을 어깨보다 1.5배 넓게 벌리고 발끝을 바깥으로 45도 열어요.",
                "손은 가슴 앞에 모아요.",
            ),
            steps = listOf(
                "상체를 곧게 세운 채 엉덩이를 수직으로 내려요.",
                "무릎이 발끝 방향으로 벌어지게 해요.",
                "허벅지가 바닥과 평행해지면 허벅지 안쪽을 조이며 일어나요.",
            ),
            breathing = "내려갈 때 들이쉬고, 올라올 때 내쉬어요.",
            mistakes = listOf("무릎이 안쪽으로 무너짐", "상체가 앞으로 숙여짐", "뒤꿈치가 들림"),
            tips = "맨 위에서 허벅지 안쪽을 1초 꽉 조이면 효과가 커져요.",
            posture = setOf(P.BOW_LEGS),
            concerns = setOf(C.THICK_THIGHS, C.SADDLEBAGS),
            view = ViewAngle(15f, 10f),
            motion = run {
                val arms = ArmPose(flex = 20f, abd = 25f, foreFlex = 95f, foreAbd = -40f)
                val top = STAND.legs(LegPose(hipAbd = 24f)).arms(arms)
                val bottom = STAND.copy(trunkFlex = 10f).legs(LegPose(72f, 42f, 98f, -22f)).arms(arms)
                Motion(listOf(Key(top, 1.8f), Key(bottom, 0.3f), Key(bottom, 1.4f)), periodSec = 3.5f)
            },
        ),
        Exercise(
            id = "lunge",
            name = "스플릿 스쿼트",
            summary = "앞뒤로 다리를 벌리고 앉았다 일어나 한쪽씩 하체를 단련해요",
            category = STRENGTH,
            level = Level.NORMAL,
            muscles = "허벅지 앞, 엉덩이, 골반 안정근",
            rx = Prescription(sets = 2, reps = 10, restSec = 40, perSide = true),
            setup = listOf(
                "선 자세에서 한 발을 크게(다리 길이만큼) 앞으로 내디뎌요.",
                "뒷발은 뒤꿈치를 들고 발 앞쪽으로 디뎌요.",
                "상체를 곧게 세우고 손은 허리에 둬요.",
            ),
            steps = listOf(
                "몸을 엘리베이터처럼 수직으로 내려요.",
                "뒷무릎이 바닥에 닿기 직전, 앞무릎이 90도가 되면 멈춰요.",
                "앞발 뒤꿈치로 밀며 올라와요. 발은 그 자리에 둬요.",
                "한쪽 횟수를 끝내면 다리를 바꿔요.",
            ),
            breathing = "내려갈 때 들이쉬고, 올라올 때 내쉬어요.",
            mistakes = listOf("앞무릎이 안쪽으로 꺾임", "상체가 앞으로 쏟아짐", "보폭이 좁아 앞무릎이 발끝을 한참 넘어감"),
            tips = "균형이 어려우면 벽이나 의자를 한 손으로 잡고 해요.",
            posture = setOf(P.PELVIC_IMBALANCE),
            concerns = setOf(C.THICK_THIGHS, C.FLAT_HIP),
            view = ViewAngle(78f, 8f),
            motion = run {
                val hands = ArmPose(flex = -20f, abd = 35f, foreFlex = 60f, foreAbd = -60f)
                val top = STAND.copy(leftLeg = LegPose(28f, 4f, 4f), rightLeg = LegPose(-26f, 4f, 2f, -32f)).arms(hands)
                val bottom = STAND.copy(leftLeg = LegPose(80f, 4f, 88f, -8f), rightLeg = LegPose(-12f, 4f, 95f, -55f)).arms(hands)
                Motion(listOf(Key(top, 1.6f), Key(bottom, 0.3f), Key(bottom, 1.4f)), periodSec = 3.3f, anchor = Anchor.LEFT_FOOT)
            },
        ),
        Exercise(
            id = "side_leg_raise",
            name = "옆으로 누워 다리 들기",
            summary = "엉덩이 옆 근육(중둔근)을 써서 승마살과 X다리를 잡아요",
            category = STRENGTH,
            muscles = "엉덩이 옆(중둔근), 허벅지 바깥",
            rx = Prescription(sets = 2, reps = 15, restSec = 30, perSide = true),
            setup = listOf(
                "옆으로 누워 아래쪽 팔을 머리 위로 뻗고 그 위에 머리를 올려요.",
                "위쪽 손은 가슴 앞 바닥을 짚어 몸이 뒤로 넘어가지 않게 받쳐요.",
                "아래 다리는 살짝 굽히고 위 다리는 곧게 펴요.",
                "머리부터 위 다리까지 일직선이 되게 해요.",
            ),
            steps = listOf(
                "위 다리를 곧게 편 채, 발뒤꿈치가 앞장서듯 위로 들어요.",
                "45도 정도까지 들고 1초 멈춰요.",
                "천천히 내리되 아래 다리에 닿기 직전에 다시 들어요.",
            ),
            breathing = "들 때 내쉬고, 내릴 때 들이쉬어요.",
            mistakes = listOf("골반이 뒤로 넘어가며 발끝이 하늘을 봄", "다리를 너무 높이 차올림", "반동으로 빠르게 함"),
            tips = "발끝은 정면이나 살짝 아래를 향해야 엉덩이 옆에 자극이 와요.",
            posture = setOf(P.KNOCK_KNEES, P.PELVIC_IMBALANCE),
            concerns = setOf(C.SADDLEBAGS, C.THICK_THIGHS),
            view = ViewAngle(0f, 18f),
            motion = run {
                val up = SIDE_LYING.copy(leftLeg = LegPose(hipFlex = 0f, hipAbd = 42f))
                Motion(listOf(Key(SIDE_LYING, 1f), Key(up, 1f), Key(up, 1.4f)), periodSec = 2.8f, anchor = Anchor.PELVIS)
            },
        ),
        Exercise(
            id = "clamshell",
            name = "클램쉘",
            summary = "조개처럼 무릎을 열어 엉덩이 옆 근육과 골반 균형을 잡아요",
            category = STRENGTH,
            muscles = "엉덩이 옆·뒤 깊은 근육(중둔근, 외회전근)",
            rx = Prescription(sets = 2, reps = 15, restSec = 30, perSide = true),
            setup = listOf(
                "옆으로 누워 아래쪽 팔을 머리 위로 뻗고 그 위에 머리를 올려요.",
                "엉덩이와 무릎을 45도쯤 굽히고 두 발을 붙여요.",
                "위쪽 손은 골반 위에 올려 골반이 움직이는지 확인해요.",
            ),
            steps = listOf(
                "발은 붙인 채 위쪽 무릎만 천천히 열어요.",
                "골반이 뒤로 넘어가기 직전까지 열고 1초 멈춰요.",
                "천천히 닫아요.",
            ),
            breathing = "열 때 내쉬고, 닫을 때 들이쉬어요.",
            mistakes = listOf("골반이 같이 뒤로 돌아감", "발이 떨어짐", "빠르게 퍼덕이듯 움직임"),
            tips = "고무 밴드를 무릎 위에 감으면 더 효과적이에요.",
            posture = setOf(P.KNOCK_KNEES, P.PELVIC_IMBALANCE),
            concerns = setOf(C.SADDLEBAGS, C.FLAT_HIP),
            view = ViewAngle(8f, 32f),
            motion = run {
                val bent = SIDE_LYING.legs(LegPose(50f, 0f, 85f))
                val open = bent.copy(leftLeg = LegPose(50f, 38f, 85f))
                Motion(listOf(Key(bent, 1f), Key(open, 1f), Key(open, 1.4f)), periodSec = 3f, anchor = Anchor.PELVIS)
            },
        ),
        Exercise(
            id = "knee_squeeze",
            name = "누워서 무릎 모으기",
            summary = "허벅지 안쪽 근육을 깨워 O다리 정렬을 도와요",
            category = STRENGTH,
            muscles = "허벅지 안쪽(내전근)",
            rx = Prescription(sets = 2, reps = 10, holdSec = 5, restSec = 20),
            setup = listOf(
                "누워서 무릎을 세우고 발은 골반 너비로 둬요.",
                "무릎 사이에 쿠션이나 접은 수건, 작은 공을 끼워요.",
            ),
            steps = listOf(
                "무릎으로 쿠션을 꾹 누르며 허벅지 안쪽에 힘을 줘요.",
                "5초 버텨요.",
                "천천히 힘을 풀어요.",
            ),
            breathing = "누를 때 내쉬고, 풀 때 들이쉬어요.",
            mistakes = listOf("엉덩이가 들림", "숨을 참음", "발이 바닥에서 미끄러짐"),
            tips = "익숙해지면 이 자세로 글루트 브릿지를 하면 효과가 더 좋아요.",
            posture = setOf(P.BOW_LEGS),
            concerns = setOf(C.THICK_THIGHS),
            view = ViewAngle(0f, 28f),
            motion = run {
                val open = HOOK_LYING.legs(LegPose(50f, 16f, 100f, 30f))
                val closed = HOOK_LYING.legs(LegPose(50f, 2f, 100f, 30f))
                Motion(listOf(Key(open, 1f), Key(closed, 5f), Key(closed, 1f)), periodSec = 7f, anchor = Anchor.PELVIS)
            },
        ),
        Exercise(
            id = "side_stretch",
            name = "옆구리 늘리기",
            summary = "짧아진 옆구리를 늘려 어깨와 골반 높이를 맞춰요",
            category = STRETCH,
            muscles = "옆구리(요방형근, 광배근)",
            rx = Prescription(sets = 2, holdSec = 20, restSec = 10, perSide = true),
            setup = listOf(
                "발을 골반 너비로 벌리고 바르게 서요.",
                "늘릴 쪽 팔을 머리 위로 곧게 뻗어요.",
            ),
            steps = listOf(
                "반대쪽으로 천천히 상체를 기울여요.",
                "골반은 정면에 둔 채 옆구리가 당기는 곳에서 멈춰요.",
                "20초 버티고 천천히 돌아와요.",
            ),
            breathing = "버티는 동안 늘어나는 옆구리로 숨을 불어넣듯 깊게 쉬어요.",
            mistakes = listOf("상체가 앞으로 숙여짐", "골반이 같이 옆으로 빠짐", "반동을 줌"),
            tips = "어깨가 높은 쪽 팔을 올리고 반대로 기울이면 더 많이 늘어나요. 그쪽을 한 번 더 해도 좋아요.",
            posture = setOf(P.UNEVEN_SHOULDERS, P.PELVIC_IMBALANCE),
            view = ViewAngle(0f, 6f),
            holdAtSec = 2.5f,
            motion = run {
                val start = STAND.copy(leftArm = ArmPose.straight(0f, 165f), rightArm = ArmPose.straight(0f, 10f))
                val bend = STAND.copy(trunkSide = -24f, leftArm = ArmPose.straight(0f, 172f),
                    rightArm = ArmPose(flex = 0f, abd = 20f, foreFlex = 30f, foreAbd = -40f))
                Motion(listOf(Key(start, 1.5f), Key(bend, 3f), Key(bend, 1.5f)), periodSec = 6f, anchor = Anchor.FEET)
            },
        ),
        Exercise(
            id = "calf_stretch",
            name = "벽 밀고 종아리 늘리기",
            summary = "뭉친 종아리를 늘려 알을 부드럽게 풀어요",
            category = STRETCH,
            muscles = "종아리(비복근, 가자미근)",
            rx = Prescription(sets = 2, holdSec = 30, restSec = 10, perSide = true),
            setup = listOf(
                "벽을 마주 보고 두 손으로 어깨 높이에서 벽을 짚어요.",
                "늘릴 다리를 뒤로 크게 빼고, 뒤꿈치를 바닥에 붙여요.",
                "뒷발 발끝은 정면을 향해요.",
            ),
            steps = listOf(
                "앞무릎을 굽히며 몸을 벽 쪽으로 밀어요.",
                "뒷다리는 곧게 편 채 종아리가 당기면 멈춰요.",
                "30초 버티고 천천히 돌아와요.",
            ),
            breathing = "버티는 동안 천천히 깊게 쉬어요.",
            mistakes = listOf("뒤꿈치가 들림", "뒷발 끝이 바깥으로 돌아감", "허리가 꺾임"),
            tips = "뒷무릎을 살짝 굽히면 종아리 안쪽 깊은 근육(가자미근)까지 늘어나요.",
            concerns = setOf(C.CALF_MUSCLE),
            view = ViewAngle(82f, 8f),
            holdAtSec = 2f,
            motion = run {
                val start = STAND.copy(trunkFlex = 12f, leftLeg = LegPose(16f, 4f, 18f), rightLeg = LegPose(-28f, 4f, 0f, -26f))
                    .arms(ArmPose.straight(78f, 10f))
                val lean = STAND.copy(trunkFlex = 18f, leftLeg = LegPose(32f, 4f, 50f, 5f), rightLeg = LegPose(-36f, 4f, 0f, -34f))
                    .arms(ArmPose.bent(72f, 45f, 10f))
                Motion(listOf(Key(start, 1.5f), Key(lean, 3f), Key(lean, 1.5f)), periodSec = 6f, anchor = Anchor.LEFT_FOOT)
            },
        ),
        Exercise(
            id = "tricep_kickback",
            name = "트라이셉스 킥백",
            summary = "팔 뒤쪽 근육을 써서 처진 팔뚝을 탄탄하게 해요",
            category = STRENGTH,
            muscles = "팔 뒤쪽(삼두근)",
            rx = Prescription(sets = 3, reps = 12, restSec = 40),
            setup = listOf(
                "물병(500ml)을 양손에 들고 발을 골반 너비로 벌려요.",
                "무릎을 살짝 굽히고 등을 편 채 상체를 45도 숙여요.",
                "팔꿈치를 옆구리 높이로 끌어올려 90도로 굽혀요.",
            ),
            steps = listOf(
                "팔꿈치는 그 자리에 고정하고 아래팔만 뒤로 쭉 펴요.",
                "다 폈을 때 팔 뒤쪽을 1초 조여요.",
                "천천히 90도로 돌아와요.",
            ),
            breathing = "펼 때 내쉬고, 굽힐 때 들이쉬어요.",
            mistakes = listOf("팔꿈치가 아래로 떨어짐", "반동으로 휘두름", "등이 둥글게 굽음"),
            tips = "가벼운 무게로 천천히 하는 게 더 효과적이에요.",
            concerns = setOf(C.ARM_FAT),
            view = ViewAngle(85f, 8f),
            motion = run {
                val base = STAND.copy(trunkFlex = 45f).legs(LegPose(32f, 6f, 25f))
                // 위팔은 몸통과 나란히(옆구리 옆), 아래팔은 땅을 향하게 → 뒤로 쭉 펴기
                val bent = base.arms(ArmPose(flex = -15f, abd = 10f, foreFlex = 45f, foreAbd = 10f))
                val ext = base.arms(ArmPose.straight(-22f, 10f))
                Motion(listOf(Key(bent, 1f), Key(ext, 1f), Key(ext, 1.2f)), periodSec = 3.2f)
            },
        ),
        Exercise(
            id = "jumping_jack",
            name = "팔 벌려 뛰기",
            summary = "온몸을 써서 심박수를 올리는 쉬운 유산소",
            category = CARDIO,
            muscles = "전신, 심폐 지구력",
            rx = Prescription(sets = 3, reps = 20, restSec = 30),
            setup = listOf("발을 모으고 팔은 몸 옆에 두고 바르게 서요."),
            steps = listOf(
                "가볍게 점프하며 발을 어깨보다 넓게 벌리고 팔을 머리 위로 올려요.",
                "다시 점프하며 발을 모으고 팔을 내려요.",
                "발 앞쪽으로 사뿐하게 착지해요.",
            ),
            breathing = "리듬에 맞춰 입으로 짧게 내쉬어요.",
            mistakes = listOf("뒤꿈치로 쿵쿵 착지", "무릎을 쭉 편 채 착지", "팔을 반만 올림"),
            tips = "층간소음이 걱정되면 점프 없이 한 발씩 옆으로 내딛는 '스텝 잭'으로 해요.",
            concerns = setOf(C.LOW_STAMINA, C.BELLY_FAT, C.THICK_THIGHS),
            view = ViewAngle(12f, 8f),
            motion = run {
                val closed = STAND.legs(LegPose(hipAbd = 2f, knee = 8f, hipFlex = 4f)).arms(ArmPose.straight(0f, 6f))
                val air1 = STAND.copy(lift = 0.09f).legs(LegPose(hipAbd = 9f, ankle = -35f)).arms(ArmPose.straight(0f, 90f))
                val open = STAND.legs(LegPose(hipAbd = 16f, knee = 10f, hipFlex = 4f)).arms(ArmPose(0f, 160f, 180f, -18f))
                val air2 = STAND.copy(lift = 0.09f).legs(LegPose(hipAbd = 9f, ankle = -35f)).arms(ArmPose.straight(0f, 90f))
                Motion(listOf(Key(closed), Key(air1), Key(open), Key(air2)), periodSec = 1.1f, anchor = Anchor.PELVIS)
            },
        ),
        Exercise(
            id = "high_knees",
            name = "제자리 무릎 들어 걷기",
            summary = "무릎을 높이 들며 걸어 체력과 하체 라인을 함께 챙겨요",
            category = CARDIO,
            muscles = "엉덩이 굽힘근, 복근, 종아리, 심폐 지구력",
            rx = Prescription(sets = 3, reps = 20, restSec = 30),
            setup = listOf("바르게 서서 팔꿈치를 90도로 굽혀요."),
            steps = listOf(
                "한쪽 무릎을 골반 높이까지 들어요.",
                "내리면서 반대쪽 무릎을 들어요. 팔은 다리와 반대로 흔들어요.",
                "왼쪽·오른쪽을 한 번씩 들면 1회예요.",
            ),
            breathing = "리듬에 맞춰 자연스럽게 쉬어요.",
            mistakes = listOf("상체가 뒤로 젖혀짐", "무릎을 낮게 듦", "발을 쿵쿵 찍음"),
            tips = "익숙해지면 속도를 올려 제자리 달리기로 바꿔요.",
            concerns = setOf(C.LOW_STAMINA, C.BELLY_FAT, C.CALF_MUSCLE),
            view = ViewAngle(45f, 10f),
            motion = run {
                val mid = STAND.arms(ArmPose.bent(5f, 90f))
                val left = STAND.copy(
                    leftLeg = LegPose(85f, 4f, 95f, -10f),
                    leftArm = ArmPose.bent(-30f, 90f), rightArm = ArmPose.bent(40f, 90f),
                )
                Motion(
                    listOf(Key(left, 1f), Key(mid, 1f), Key(left.mirrored(), 1f), Key(mid, 1f)),
                    periodSec = 1.6f,
                    anchor = Anchor.PELVIS,
                )
            },
        ),
    )

    private val byId = all.associateBy { it.id }

    fun find(id: String): Exercise? = byId[id]
}
