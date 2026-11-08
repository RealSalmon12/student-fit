package com.realsalmon.studentfit.data

import android.content.Context
import androidx.core.content.edit
import com.realsalmon.studentfit.domain.BodyProfile
import com.realsalmon.studentfit.domain.Gender
import com.realsalmon.studentfit.domain.PostureIssue
import com.realsalmon.studentfit.domain.ProfileSource

/** 기기 안에만 체형 정보를 저장해요. 서버로 보내지 않아요. */
class ProfileRepository(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("body_profile", Context.MODE_PRIVATE)

    fun load(): BodyProfile? {
        if (!prefs.contains(KEY_HEIGHT)) return null
        return BodyProfile(
            heightCm = prefs.getFloat(KEY_HEIGHT, 0f),
            weightKg = prefs.getFloat(KEY_WEIGHT, 0f),
            age = prefs.getInt(KEY_AGE, 0),
            gender = enumValueOrNull<Gender>(prefs.getString(KEY_GENDER, null)) ?: Gender.MALE,
            postureIssues = prefs.getStringSet(KEY_POSTURE, emptySet()).orEmpty()
                .mapNotNull { enumValueOrNull<PostureIssue>(it) }
                .toSet(),
            source = enumValueOrNull<ProfileSource>(prefs.getString(KEY_SOURCE, null))
                ?: ProfileSource.MANUAL,
        )
    }

    fun save(profile: BodyProfile) {
        prefs.edit {
            putFloat(KEY_HEIGHT, profile.heightCm)
            putFloat(KEY_WEIGHT, profile.weightKg)
            putInt(KEY_AGE, profile.age)
            putString(KEY_GENDER, profile.gender.name)
            putStringSet(KEY_POSTURE, profile.postureIssues.map { it.name }.toSet())
            putString(KEY_SOURCE, profile.source.name)
        }
    }

    private inline fun <reified T : Enum<T>> enumValueOrNull(name: String?): T? =
        enumValues<T>().firstOrNull { it.name == name }

    private companion object {
        const val KEY_HEIGHT = "height_cm"
        const val KEY_WEIGHT = "weight_kg"
        const val KEY_AGE = "age"
        const val KEY_GENDER = "gender"
        const val KEY_POSTURE = "posture_issues"
        const val KEY_SOURCE = "source"
    }
}
