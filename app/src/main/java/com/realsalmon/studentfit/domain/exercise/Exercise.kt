package com.realsalmon.studentfit.domain.exercise

import com.realsalmon.studentfit.domain.BodyConcern
import com.realsalmon.studentfit.domain.PostureIssue
import com.realsalmon.studentfit.domain.motion.Motion

enum class ExerciseCategory(val label: String) {
    STRETCH("스트레칭"),
    STRENGTH("근력"),
    CARDIO("유산소"),
}

data class Exercise(
    val id: String,
    val name: String,
    val summary: String,
    val category: ExerciseCategory,
    val dose: String,
    val steps: List<String>,
    val tips: String,
    val posture: Set<PostureIssue> = emptySet(),
    val concerns: Set<BodyConcern> = emptySet(),
    val motion: Motion,
) {
    val targetLabels: List<String> get() = posture.map { it.label } + concerns.map { it.label }
}
