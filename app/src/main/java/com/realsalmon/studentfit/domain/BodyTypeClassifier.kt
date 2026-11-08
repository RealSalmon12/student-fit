package com.realsalmon.studentfit.domain

import kotlin.math.roundToInt

enum class BmiCategory(val label: String) {
    UNDERWEIGHT("저체중"),
    NORMAL("정상"),
    OVERWEIGHT("과체중"),
    OBESE("비만"),
}

data class BodyTypeResult(
    val bmi: Float,
    val bmiCategory: BmiCategory,
    val postureIssues: Set<PostureIssue>,
    val concerns: Set<BodyConcern> = emptySet(),
) {
    val bmiText: String get() = "${(bmi * 10).roundToInt() / 10f}"

    /** 한눈에 보이는 체형 이름. 가장 눈에 띄는 문제를 기준으로 정해요. */
    val typeTitle: String
        get() = when {
            PostureIssue.ANTERIOR_PELVIC_TILT in postureIssues -> "오리엉덩이형"
            PostureIssue.FORWARD_HEAD in postureIssues &&
                (PostureIssue.ROUNDED_SHOULDERS in postureIssues || PostureIssue.ROUNDED_BACK in postureIssues) ->
                "거북목·굽은 어깨형"
            PostureIssue.FORWARD_HEAD in postureIssues -> "거북목형"
            PostureIssue.ROUNDED_SHOULDERS in postureIssues || PostureIssue.ROUNDED_BACK in postureIssues -> "굽은 어깨형"
            PostureIssue.PELVIC_IMBALANCE in postureIssues || PostureIssue.UNEVEN_SHOULDERS in postureIssues -> "좌우 비대칭형"
            PostureIssue.BOW_LEGS in postureIssues -> "O다리형"
            PostureIssue.KNOCK_KNEES in postureIssues -> "X다리형"
            BodyConcern.THICK_THIGHS in concerns || BodyConcern.SADDLEBAGS in concerns -> "하체 집중형"
            BodyConcern.BELLY_FAT in concerns -> "복부 집중형"
            bmiCategory == BmiCategory.UNDERWEIGHT -> "마른 체형"
            bmiCategory == BmiCategory.NORMAL -> "균형 체형"
            else -> "체중 관리형"
        }

    val summary: String
        get() = buildString {
            append("BMI $bmiText (${bmiCategory.label})")
            val tags = postureIssues.map { it.label } + concerns.map { it.label }
            if (tags.isEmpty()) append(" · 고민 없음") else append(" · ").append(tags.joinToString())
        }
}

/**
 * 직접 입력한 정보로 체형을 분류해요.
 *
 * BMI 기준은 대한비만학회 성인 기준(18.5 / 23 / 25)을 써요.
 * 만 18세 이하는 원래 성장도표 백분위로 봐야 해서 대략적인 값이에요.
 */
object BodyTypeClassifier {

    fun bmi(heightCm: Float, weightKg: Float): Float {
        require(heightCm > 0f && weightKg > 0f) { "키와 몸무게는 0보다 커야 해요" }
        val m = heightCm / 100f
        return weightKg / (m * m)
    }

    fun bmiCategory(bmi: Float): BmiCategory = when {
        bmi < 18.5f -> BmiCategory.UNDERWEIGHT
        bmi < 23f -> BmiCategory.NORMAL
        bmi < 25f -> BmiCategory.OVERWEIGHT
        else -> BmiCategory.OBESE
    }

    fun classify(profile: BodyProfile): BodyTypeResult {
        val value = bmi(profile.heightCm, profile.weightKg)
        return BodyTypeResult(
            bmi = value,
            bmiCategory = bmiCategory(value),
            postureIssues = profile.postureIssues,
            concerns = profile.concerns,
        )
    }
}
