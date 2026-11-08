package com.realsalmon.studentfit.domain

enum class Gender(val label: String) {
    MALE("남"),
    FEMALE("여"),
}

/** 자세(정렬) 문제. 직접 체크하거나 카메라 분석으로 찾아요. */
enum class PostureIssue(val label: String, val description: String) {
    FORWARD_HEAD("거북목", "옆에서 볼 때 귀가 어깨보다 앞으로 나와 있어요"),
    ROUNDED_SHOULDERS("라운드숄더", "어깨가 앞으로 말려 있어요"),
    ROUNDED_BACK("굽은 등", "등 윗부분이 둥글게 굽어 있어요"),
    ANTERIOR_PELVIC_TILT("오리엉덩이", "골반이 앞으로 기울어 엉덩이가 뒤로 빠지고 허리가 과하게 휘어요"),
    PELVIC_IMBALANCE("골반 비대칭", "한쪽 골반이 더 높거나 틀어져 있어요"),
    UNEVEN_SHOULDERS("어깨 높이 차이", "양쪽 어깨 높이가 달라요"),
    BOW_LEGS("O다리", "발을 붙여도 무릎 사이가 벌어져요"),
    KNOCK_KNEES("X다리", "무릎을 붙이면 발목 사이가 벌어져요"),
}

/** 몸매·체력 고민. 직접 체크해요. */
enum class BodyConcern(val label: String, val description: String) {
    THICK_THIGHS("허벅지 살", "허벅지 앞쪽과 안쪽에 살이 많아요"),
    SADDLEBAGS("승마살", "허벅지 바깥쪽 위에 살이 튀어나와 있어요"),
    BELLY_FAT("뱃살", "아랫배가 나와 있어요"),
    ARM_FAT("팔뚝 살", "팔 뒤쪽이 처져 있어요"),
    CALF_MUSCLE("종아리 알", "종아리가 단단하고 두꺼워요"),
    FLAT_HIP("납작 엉덩이", "엉덩이가 처지거나 납작해요"),
    LOW_STAMINA("체력 부족", "계단만 올라도 숨이 차요"),
}

/** 어디서 입력된 정보인지. */
enum class ProfileSource { MANUAL, CAMERA }

data class BodyProfile(
    val heightCm: Float,
    val weightKg: Float,
    val age: Int,
    val gender: Gender,
    val postureIssues: Set<PostureIssue> = emptySet(),
    val concerns: Set<BodyConcern> = emptySet(),
    val source: ProfileSource = ProfileSource.MANUAL,
)
