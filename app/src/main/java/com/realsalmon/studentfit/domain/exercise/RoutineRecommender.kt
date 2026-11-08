package com.realsalmon.studentfit.domain.exercise

import com.realsalmon.studentfit.domain.BmiCategory
import com.realsalmon.studentfit.domain.BodyTypeResult

data class Recommendation(val exercise: Exercise, val reasons: List<String>)

/** 체형 분류 결과로 오늘의 맞춤 루틴을 골라요. */
object RoutineRecommender {

    private val DEFAULT_IDS = listOf("chin_tuck", "wall_angel", "glute_bridge", "squat", "plank", "jumping_jack")

    fun recommend(
        result: BodyTypeResult?,
        catalog: List<Exercise> = ExerciseCatalog.all,
        size: Int = 6,
    ): List<Recommendation> {
        if (result == null || (result.postureIssues.isEmpty() && result.concerns.isEmpty())) {
            return DEFAULT_IDS.mapNotNull { id -> catalog.find { it.id == id } }
                .map { Recommendation(it, listOf("기본 자세 루틴")) }
                .let { base -> if (result != null) addBmiExtra(base, result, catalog, size) else base }
                .take(size)
        }

        val scored = catalog.map { ex ->
            val postureHits = ex.posture.intersect(result.postureIssues)
            val concernHits = ex.concerns.intersect(result.concerns)
            val score = postureHits.size * 3 + concernHits.size * 2 + bmiBonus(ex, result.bmiCategory)
            Triple(ex, score, postureHits.map { it.label } + concernHits.map { it.label })
        }.filter { it.second > 0 && it.third.isNotEmpty() }
            .sortedByDescending { it.second }

        // 한 가지 고민에만 몰리지 않도록, 각 고민마다 최소 하나씩 먼저 넣어요.
        val picked = LinkedHashMap<String, Recommendation>()
        val targets = result.postureIssues.map { it.label } + result.concerns.map { it.label }
        for (t in targets) {
            scored.firstOrNull { t in it.third && it.first.id !in picked }?.let {
                picked[it.first.id] = Recommendation(it.first, it.third)
            }
        }
        for ((ex, _, reasons) in scored) {
            if (picked.size >= size) break
            picked.putIfAbsent(ex.id, Recommendation(ex, reasons))
        }
        return addBmiExtra(picked.values.toList(), result, catalog, size).take(size)
    }

    private fun bmiBonus(ex: Exercise, bmi: BmiCategory) = when (bmi) {
        BmiCategory.OVERWEIGHT, BmiCategory.OBESE -> if (ex.category == ExerciseCategory.CARDIO) 1 else 0
        BmiCategory.UNDERWEIGHT -> if (ex.category == ExerciseCategory.STRENGTH) 1 else 0
        BmiCategory.NORMAL -> 0
    }

    /** 과체중 이상이면 유산소를 하나는 꼭 넣어요. */
    private fun addBmiExtra(
        list: List<Recommendation>,
        result: BodyTypeResult,
        catalog: List<Exercise>,
        size: Int,
    ): List<Recommendation> {
        val needCardio = result.bmiCategory == BmiCategory.OVERWEIGHT || result.bmiCategory == BmiCategory.OBESE
        if (!needCardio || list.any { it.exercise.category == ExerciseCategory.CARDIO }) return list
        val cardio = catalog.firstOrNull { it.category == ExerciseCategory.CARDIO } ?: return list
        val rec = Recommendation(cardio, listOf("체중 관리"))
        return if (list.size >= size) list.dropLast(1) + rec else list + rec
    }
}
