package com.realsalmon.studentfit.domain.motion

import com.realsalmon.studentfit.domain.exercise.ExerciseCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SkeletonTest {

    @Test
    fun standingPersonIsAboutOnePointSevenTall() {
        val f = Skeleton.solve(Pose())
        val top = f[Joint.HEAD].y + Skeleton.HEAD_RADIUS
        assertTrue("height=$top", top in 1.6f..1.85f)
        assertEquals(0f, minOf(f[Joint.L_ANKLE].y, f[Joint.L_TOE].y), 0.001f)
    }

    @Test
    fun allExerciseFramesStayOnTheFloor() {
        for (ex in ExerciseCatalog.all) {
            for (i in 0..20) {
                val f = ex.motion.frameAt(ex.motion.periodSec * i / 20f)
                val minY = f.joints.values.minOf { it.y }
                assertTrue("${ex.id} sinks: $minY", minY > -Skeleton.HEAD_RADIUS - 0.001f)
                f.joints.values.forEach { assertTrue(ex.id, !it.x.isNaN() && !it.y.isNaN() && !it.z.isNaN()) }
            }
        }
    }
}
