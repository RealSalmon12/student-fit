package com.realsalmon.studentfit.domain.motion

import com.realsalmon.studentfit.domain.exercise.ExerciseCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MotionExportTest {
    @Test
    fun exportsEveryFrameWithAllJoints() {
        val json = MotionExport.toJson(ExerciseCatalog.all.first().motion, samples = 12)
        assertTrue(json.startsWith("{\"period\":"))
        val frames = json.substringAfter("\"frames\":[").substringBefore("],\"lift\"")
        val count = frames.split("],[").size
        assertEquals(12, count)
        val firstFrame = frames.removePrefix("[").substringBefore("]")
        assertEquals(Joint.entries.size * 3, firstFrame.split(",").size)
        assertTrue(!json.contains("NaN"))
    }
}
