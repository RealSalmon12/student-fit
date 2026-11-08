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
) {
    val summary: String
        get() = buildString {
            append("BMI ${(bmi * 10).roundToInt() / 10f} (${bmiCategory.label})")
            if (postureIssues.isEmpty()) {
                append(" · 자세 문제 없음")
            } else {
                append(" · ")
                append(postureIssues.joinToString { it.label })
            }
        }
}

/**
 * 직접 입력한 정보로 체형을 분류해요.
 *
 * BMI 기준은 대한비만학회 성인 기준(18.5 / 23 / 25)을 써요.
 * 만 18세 이하는 원래 성장도표 백분위로 봐야 해서 대략적인 값이에요.
 * TODO: 소아청소년 성장도표(2017) 백분위 기준으로 바꾸기.
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
        )
    }
}
