package com.realsalmon.studentfit.domain.pose

import com.realsalmon.studentfit.domain.PostureIssue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PostureAnalyzerTest {

    private fun p(x: Float, y: Float) = PosePoint(x, y, 0.95f)

    private fun front(rightShoulderY: Float = 200f, kneeGap: Float = 40f, ankleGap: Float = 40f): PoseSample = mapOf(
        BodyPoint.LEFT_SHOULDER to p(260f, 200f), BodyPoint.RIGHT_SHOULDER to p(140f, rightShoulderY),
        BodyPoint.LEFT_HIP to p(240f, 400f), BodyPoint.RIGHT_HIP to p(160f, 400f),
        BodyPoint.LEFT_KNEE to p(200f + kneeGap / 2, 550f), BodyPoint.RIGHT_KNEE to p(200f - kneeGap / 2, 550f),
        BodyPoint.LEFT_ANKLE to p(200f + ankleGap / 2, 700f), BodyPoint.RIGHT_ANKLE to p(200f - ankleGap / 2, 700f),
    )

    private fun ScanResult.has(i: PostureIssue) = findings.first { it.issue == i }.detected

    @Test
    fun levelBodyHasNoFrontIssues() {
        val r = PostureAnalyzer.analyze(ScanView.FRONT, front())
        assertTrue(r.detected.isEmpty())
        assertTrue(r.missing.isEmpty())
    }

    @Test
    fun detectsUnevenShouldersAndBowLegs() {
        val r = PostureAnalyzer.analyze(ScanView.FRONT, front(rightShoulderY = 215f, kneeGap = 50f, ankleGap = 10f))
        assertTrue(r.has(PostureIssue.UNEVEN_SHOULDERS))
        assertTrue(r.has(PostureIssue.BOW_LEGS))
        assertFalse(r.has(PostureIssue.KNOCK_KNEES))
    }

    @Test
    fun detectsKnockKnees() {
        val r = PostureAnalyzer.analyze(ScanView.FRONT, front(kneeGap = 5f, ankleGap = 45f))
        assertTrue(r.has(PostureIssue.KNOCK_KNEES))
    }

    private fun side(earX: Float, shoulderX: Float = 200f, hipX: Float = 200f, ankleX: Float = 200f): PoseSample = mapOf(
        BodyPoint.NOSE to p(earX + 30f, 95f),
        BodyPoint.LEFT_EAR to p(earX, 100f),
        BodyPoint.LEFT_SHOULDER to p(shoulderX, 200f),
        BodyPoint.LEFT_HIP to p(hipX, 400f),
        BodyPoint.LEFT_ANKLE to p(ankleX, 700f),
    )

    @Test
    fun uprightSideViewIsFine() {
        val r = PostureAnalyzer.analyze(ScanView.SIDE, side(earX = 205f))
        assertTrue(r.detected.isEmpty())
        assertEquals(3, r.findings.size)
    }

    @Test
    fun detectsForwardHeadAndDuckButt() {
        // 귀가 어깨보다 100px 앞(CVA 45°), 엉덩이는 발목보다 40px 뒤, 어깨는 골반보다 30px 앞
        val r = PostureAnalyzer.analyze(ScanView.SIDE, side(earX = 330f, shoulderX = 230f, hipX = 200f, ankleX = 240f))
        assertTrue(r.has(PostureIssue.FORWARD_HEAD))
        assertTrue(r.has(PostureIssue.ANTERIOR_PELVIC_TILT))
        assertTrue(r.has(PostureIssue.ROUNDED_SHOULDERS))
    }

    @Test
    fun averageDropsLowConfidencePoints() {
        val a = mapOf(BodyPoint.NOSE to PosePoint(10f, 10f, 0.9f))
        val b = mapOf(BodyPoint.NOSE to PosePoint(20f, 20f, 0.9f))
        val c = mapOf(BodyPoint.NOSE to PosePoint(1000f, 1000f, 0.1f))
        val avg = PostureAnalyzer.average(listOf(a, b, c))
        assertEquals(15f, avg.getValue(BodyPoint.NOSE).x, 0.01f)
    }
}
