package com.realsalmon.studentfit.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class BodyTypeClassifierTest {

    @Test
    fun bmiIsWeightOverHeightSquared() {
        assertEquals(22.86f, BodyTypeClassifier.bmi(175f, 70f), 0.01f)
    }

    @Test
    fun categoryBoundaries() {
        assertEquals(BmiCategory.UNDERWEIGHT, BodyTypeClassifier.bmiCategory(18.4f))
        assertEquals(BmiCategory.NORMAL, BodyTypeClassifier.bmiCategory(18.5f))
        assertEquals(BmiCategory.OVERWEIGHT, BodyTypeClassifier.bmiCategory(23f))
        assertEquals(BmiCategory.OBESE, BodyTypeClassifier.bmiCategory(25f))
    }

    @Test
    fun classifyKeepsPostureIssues() {
        val profile = BodyProfile(
            heightCm = 165f, weightKg = 55f, age = 16, gender = Gender.FEMALE,
            postureIssues = setOf(PostureIssue.FORWARD_HEAD),
        )
        val result = BodyTypeClassifier.classify(profile)
        assertEquals(BmiCategory.NORMAL, result.bmiCategory)
        assertEquals(setOf(PostureIssue.FORWARD_HEAD), result.postureIssues)
    }
}
