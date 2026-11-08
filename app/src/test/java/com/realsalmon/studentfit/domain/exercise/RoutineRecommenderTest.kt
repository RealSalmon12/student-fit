package com.realsalmon.studentfit.domain.exercise

import com.realsalmon.studentfit.domain.BmiCategory
import com.realsalmon.studentfit.domain.BodyConcern
import com.realsalmon.studentfit.domain.BodyTypeResult
import com.realsalmon.studentfit.domain.PostureIssue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RoutineRecommenderTest {

    @Test
    fun everyIssueAndConcernHasAnExercise() {
        for (issue in PostureIssue.entries) {
            assertTrue("$issue", ExerciseCatalog.all.any { issue in it.posture })
        }
        for (c in BodyConcern.entries) {
            assertTrue("$c", ExerciseCatalog.all.any { c in it.concerns })
        }
    }

    @Test
    fun idsAreUnique() {
        assertEquals(ExerciseCatalog.all.size, ExerciseCatalog.all.map { it.id }.toSet().size)
    }

    @Test
    fun coversEachSelectedTarget() {
        val result = BodyTypeResult(
            21f, BmiCategory.NORMAL,
            setOf(PostureIssue.ANTERIOR_PELVIC_TILT, PostureIssue.FORWARD_HEAD),
            setOf(BodyConcern.THICK_THIGHS, BodyConcern.ARM_FAT),
        )
        val recs = RoutineRecommender.recommend(result)
        assertEquals(6, recs.size)
        for (t in listOf("오리엉덩이", "거북목", "허벅지 살", "팔뚝 살")) {
            assertTrue(t, recs.any { t in it.reasons })
        }
    }

    @Test
    fun overweightGetsCardio() {
        val result = BodyTypeResult(27f, BmiCategory.OBESE, setOf(PostureIssue.FORWARD_HEAD))
        val recs = RoutineRecommender.recommend(result)
        assertTrue(recs.any { it.exercise.category == ExerciseCategory.CARDIO })
    }

    @Test
    fun noProfileGivesDefaultRoutine() {
        assertEquals(6, RoutineRecommender.recommend(null).size)
    }
}
