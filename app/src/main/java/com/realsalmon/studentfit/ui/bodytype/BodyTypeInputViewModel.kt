package com.realsalmon.studentfit.ui.bodytype

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.realsalmon.studentfit.data.ProfileRepository
import com.realsalmon.studentfit.domain.BodyProfile
import com.realsalmon.studentfit.domain.BodyTypeClassifier
import com.realsalmon.studentfit.domain.BodyTypeResult
import com.realsalmon.studentfit.domain.Gender
import com.realsalmon.studentfit.domain.PostureIssue

data class BodyInputState(
    val height: String = "",
    val weight: String = "",
    val age: String = "",
    val gender: Gender = Gender.MALE,
    val postureIssues: Set<PostureIssue> = emptySet(),
    val error: String? = null,
    val result: BodyTypeResult? = null,
)

class BodyTypeInputViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = ProfileRepository(app)

    var state by mutableStateOf(BodyInputState())
        private set

    init {
        repo.load()?.let { p ->
            state = BodyInputState(
                height = p.heightCm.toCleanString(),
                weight = p.weightKg.toCleanString(),
                age = p.age.toString(),
                gender = p.gender,
                postureIssues = p.postureIssues,
                result = BodyTypeClassifier.classify(p),
            )
        }
    }

    fun onHeight(v: String) { state = state.copy(height = v.filterNumber(), error = null) }
    fun onWeight(v: String) { state = state.copy(weight = v.filterNumber(), error = null) }
    fun onAge(v: String) { state = state.copy(age = v.filter(Char::isDigit).take(2), error = null) }
    fun onGender(g: Gender) { state = state.copy(gender = g) }

    fun togglePosture(issue: PostureIssue) {
        val next = state.postureIssues.toMutableSet()
        if (!next.add(issue)) next.remove(issue)
        state = state.copy(postureIssues = next)
    }

    /** 저장에 성공하면 true. */
    fun save(): Boolean {
        val h = state.height.toFloatOrNull()
        val w = state.weight.toFloatOrNull()
        val a = state.age.toIntOrNull()
        val error = when {
            h == null || h !in 100f..230f -> "키를 100~230cm 사이로 입력해 주세요"
            w == null || w !in 20f..200f -> "몸무게를 20~200kg 사이로 입력해 주세요"
            a == null || a !in 8..25 -> "나이를 8~25세 사이로 입력해 주세요"
            else -> null
        }
        if (error != null) {
            state = state.copy(error = error)
            return false
        }
        val profile = BodyProfile(h!!, w!!, a!!, state.gender, state.postureIssues)
        repo.save(profile)
        state = state.copy(error = null, result = BodyTypeClassifier.classify(profile))
        return true
    }

    private fun String.filterNumber(): String {
        val cleaned = filter { it.isDigit() || it == '.' }
        val firstDot = cleaned.indexOf('.')
        return if (firstDot < 0) cleaned
        else cleaned.substring(0, firstDot + 1) + cleaned.substring(firstDot + 1).replace(".", "")
    }

    private fun Float.toCleanString(): String =
        if (this % 1f == 0f) toInt().toString() else toString()
}
