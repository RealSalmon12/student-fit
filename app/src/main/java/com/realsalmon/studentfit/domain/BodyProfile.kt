package com.realsalmon.studentfit.domain

enum class Gender(val label: String) {
    MALE("남"),
    FEMALE("여"),
}

/** 학생이 스스로 체크하거나, 2단계에서 카메라 포즈 인식으로 찾아낼 자세 문제. */
enum class PostureIssue(val label: String, val description: String) {
    FORWARD_HEAD("거북목", "옆에서 볼 때 귀가 어깨보다 앞으로 나와 있어요"),
    ROUNDED_SHOULDERS("라운드숄더", "어깨가 앞으로 말려 있어요"),
    PELVIC_TILT("골반 틀어짐", "한쪽 골반이 더 높거나 앞뒤로 기울어 있어요"),
    UNEVEN_SHOULDERS("어깨 높이 차이", "양쪽 어깨 높이가 달라요"),
    BOW_LEGS("O다리", "발을 붙여도 무릎 사이가 벌어져요"),
    KNOCK_KNEES("X다리", "무릎을 붙이면 발목 사이가 벌어져요"),
}

/** 어디서 입력된 정보인지. 2단계부터 CAMERA 값이 쓰여요. */
enum class ProfileSource { MANUAL, CAMERA }

data class BodyProfile(
    val heightCm: Float,
    val weightKg: Float,
    val age: Int,
    val gender: Gender,
    val postureIssues: Set<PostureIssue> = emptySet(),
    val source: ProfileSource = ProfileSource.MANUAL,
)
