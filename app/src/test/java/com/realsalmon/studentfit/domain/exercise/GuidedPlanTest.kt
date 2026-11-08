package com.realsalmon.studentfit.domain.exercise

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GuidedPlanTest {

    @Test
    fun repsExerciseCountsRepsByMotionCycle() {
        val squat = ExerciseCatalog.find("squat")!!
        val plan = GuidedPlan(squat)
        // 준비 + (운동, 휴식) × 2 + 운동
        assertEquals(1 + 3 + 2, plan.segments.size)
        val workStart = plan.startOf(1)
        val s = plan.stateAt(workStart + plan.repSec * 2.5f)
        assertTrue(s.segment is Segment.Work)
        assertEquals(3, s.rep)
    }

    @Test
    fun perSideExerciseDoesLeftThenRight() {
        val lunge = ExerciseCatalog.find("lunge")!!
        val plan = GuidedPlan(lunge)
        val works = plan.segments.filterIsInstance<Segment.Work>()
        assertEquals(lunge.rx.sets * 2, works.size)
        assertEquals(Side.LEFT, works[0].side)
        assertEquals(Side.RIGHT, works[1].side)
        assertTrue(plan.segments[2] is Segment.Switch)
    }

    @Test
    fun holdExerciseHasNoRepAndLastsHoldTime() {
        val plank = ExerciseCatalog.find("plank")!!
        val plan = GuidedPlan(plank)
        val s = plan.stateAt(plan.startOf(1) + 1f)
        assertNull(s.rep)
        assertEquals(29, plan.secondsLeft(s))
    }

    @Test
    fun finishesAtTotal() {
        val plan = GuidedPlan(ExerciseCatalog.find("chin_tuck")!!)
        assertTrue(plan.stateAt(plan.totalSec + 0.1f).finished)
    }

    @Test
    fun everyExerciseHasContent() {
        for (ex in ExerciseCatalog.all) {
            assertTrue(ex.id, ex.setup.isNotEmpty() && ex.steps.size >= 2 && ex.mistakes.isNotEmpty())
            assertTrue(ex.id, ex.breathing.isNotBlank() && ex.muscles.isNotBlank())
        }
    }
}
