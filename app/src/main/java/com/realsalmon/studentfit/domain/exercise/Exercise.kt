package com.realsalmon.studentfit.domain.exercise

import com.realsalmon.studentfit.domain.BodyConcern
import com.realsalmon.studentfit.domain.PostureIssue
import com.realsalmon.studentfit.domain.motion.Motion
import com.realsalmon.studentfit.domain.motion.ViewAngle

enum class ExerciseCategory(val label: String) {
    STRETCH("스트레칭"),
    STRENGTH("근력"),
    CARDIO("유산소"),
}

enum class Level(val label: String) { EASY("쉬움"), NORMAL("보통") }

/**
 * 운동량. [reps]만 있으면 횟수, [holdSec]만 있으면 버티기, 둘 다 있으면 "N초씩 버티며 M회".
 * [perSide]면 왼쪽을 끝내고 오른쪽을 같은 양만큼 해요.
 */
data class Prescription(
    val sets: Int,
    val reps: Int? = null,
    val holdSec: Int? = null,
    val restSec: Int = 30,
    val perSide: Boolean = false,
) {
    init { require(reps != null || holdSec != null) }

    val text: String
        get() = buildString {
            if (perSide) append("좌우 각각 ")
            when {
                reps != null && holdSec != null -> append("${reps}회 (${holdSec}초씩 유지)")
                reps != null -> append("${reps}회")
                else -> append("${holdSec}초")
            }
            append(" × ${sets}세트")
        }

    val isHold: Boolean get() = reps == null
}

data class Exercise(
    val id: String,
    val name: String,
    val summary: String,
    val category: ExerciseCategory,
    val level: Level = Level.EASY,
    val muscles: String,
    val rx: Prescription,
    val setup: List<String>,
    val steps: List<String>,
    val breathing: String,
    val mistakes: List<String>,
    val tips: String,
    val posture: Set<PostureIssue> = emptySet(),
    val concerns: Set<BodyConcern> = emptySet(),
    /** 1회 동작. 따라하기에서 한 바퀴 = 1회로 세요. */
    val motion: Motion,
    /** 동작이 가장 잘 보이는 시점. */
    val view: ViewAngle = ViewAngle.THREE_QUARTER,
    /** 상체만 크게 보여줄지(목 운동처럼 작은 움직임). */
    val focusUpper: Boolean = false,
    /** 버티기 운동에서 보여줄 자세의 시각(초). */
    val holdAtSec: Float = 0f,
) {
    val dose: String get() = rx.text
    val targetLabels: List<String> get() = posture.map { it.label } + concerns.map { it.label }

    /** 목록 썸네일에 쓸 자세의 시각: 동작이 가장 크게 드러나는 키프레임. */
    val thumbnailTime: Float
        get() {
            val total = motion.keys.sumOf { it.weight.toDouble() }.toFloat()
            val i = if (motion.keys.size > 1) 1 else 0
            return motion.periodSec * motion.keys.take(i).sumOf { it.weight.toDouble() }.toFloat() / total
        }

    /** 오른쪽 동작(한쪽씩 하는 운동). */
    val mirroredMotion: Motion by lazy { motion.mirrored() }
}
