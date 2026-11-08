package com.realsalmon.studentfit.domain.exercise

import kotlin.math.ceil
import kotlin.math.floor

enum class Side(val label: String) { BOTH(""), LEFT("왼쪽"), RIGHT("오른쪽") }

/** 따라하기의 한 구간. */
sealed interface Segment {
    val durationSec: Float

    data class Prepare(override val durationSec: Float) : Segment
    data class Work(val set: Int, val side: Side, override val durationSec: Float) : Segment
    data class Switch(override val durationSec: Float) : Segment
    data class Rest(val nextSet: Int, override val durationSec: Float) : Segment
}

/** 지금 따라하기가 어디쯤인지. */
data class GuideState(
    val index: Int,
    val segment: Segment?,
    /** 이 구간에서 지난 시간. */
    val elapsedInSegment: Float,
    val remainingInSegment: Float,
    /** 횟수 운동이면 지금 몇 번째 회(1부터), 버티기면 null. */
    val rep: Int?,
    val totalProgress: Float,
    val finished: Boolean,
)

/**
 * 세트·횟수·휴식을 시간표로 펼쳐요. 동작 한 바퀴([repSec]) = 1회.
 * 한쪽씩 하는 운동은 세트마다 왼쪽 → (바꾸기) → 오른쪽 순서예요.
 */
class GuidedPlan(
    val exercise: Exercise,
    prepareSec: Float = 5f,
    switchSec: Float = 5f,
) {
    val repSec: Float = exercise.motion.periodSec
    private val rx = exercise.rx

    val workSec: Float = when {
        rx.reps != null -> rx.reps * repSec
        else -> (rx.holdSec ?: 0).toFloat()
    }

    val segments: List<Segment> = buildList {
        add(Segment.Prepare(prepareSec))
        for (set in 1..rx.sets) {
            if (rx.perSide) {
                add(Segment.Work(set, Side.LEFT, workSec))
                add(Segment.Switch(switchSec))
                add(Segment.Work(set, Side.RIGHT, workSec))
            } else {
                add(Segment.Work(set, Side.BOTH, workSec))
            }
            if (set < rx.sets) add(Segment.Rest(set + 1, rx.restSec.toFloat()))
        }
    }

    private val starts: List<Float> = segments.runningFold(0f) { acc, s -> acc + s.durationSec }
    val totalSec: Float = starts.last()

    fun startOf(index: Int): Float = starts[index.coerceIn(0, segments.size)]

    fun stateAt(timeSec: Float): GuideState {
        if (timeSec >= totalSec) return GuideState(segments.size, null, 0f, 0f, null, 1f, true)
        val t = timeSec.coerceAtLeast(0f)
        var i = starts.indexOfLast { it <= t }.coerceIn(0, segments.lastIndex)
        while (i < segments.lastIndex && segments[i].durationSec <= 0f) i++
        val seg = segments[i]
        val local = t - starts[i]
        val rep = if (seg is Segment.Work && rx.reps != null) {
            (floor(local / repSec).toInt() + 1).coerceAtMost(rx.reps)
        } else null
        return GuideState(
            index = i,
            segment = seg,
            elapsedInSegment = local,
            remainingInSegment = seg.durationSec - local,
            rep = rep,
            totalProgress = t / totalSec,
            finished = false,
        )
    }

    /** 남은 초를 화면에 보일 정수로. */
    fun secondsLeft(state: GuideState): Int = ceil(state.remainingInSegment.toDouble()).toInt()
}
