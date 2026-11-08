package com.realsalmon.studentfit.data

import android.content.Context
import androidx.core.content.edit
import java.time.LocalDate

/** 날짜별로 끝낸 운동을 기기 안에 기록해요. */
class RoutineLog(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences("routine_log", Context.MODE_PRIVATE)

    private fun key(date: LocalDate) = "done_$date"

    fun doneOn(date: LocalDate = LocalDate.now()): Set<String> =
        prefs.getStringSet(key(date), emptySet()).orEmpty().toSet()

    fun setDone(exerciseId: String, done: Boolean, date: LocalDate = LocalDate.now()) {
        val next = doneOn(date).toMutableSet()
        if (done) next += exerciseId else next -= exerciseId
        prefs.edit { putStringSet(key(date), next) }
    }

    /** 오늘(또는 어제)부터 거꾸로 하루도 빠짐없이 운동한 날 수. */
    fun streak(today: LocalDate = LocalDate.now()): Int {
        var day = if (doneOn(today).isEmpty()) today.minusDays(1) else today
        var count = 0
        while (doneOn(day).isNotEmpty() && count < 365) {
            count++
            day = day.minusDays(1)
        }
        return count
    }
}
