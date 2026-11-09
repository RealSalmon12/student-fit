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

    /** [from]~[to] 사이 날짜별로 끝낸 운동 수. */
    fun counts(from: LocalDate, to: LocalDate): Map<LocalDate, Int> {
        val out = LinkedHashMap<LocalDate, Int>()
        var d = from
        while (!d.isAfter(to)) {
            out[d] = doneOn(d).size
            d = d.plusDays(1)
        }
        return out
    }

    /** 기록된 모든 날짜(운동을 하나라도 끝낸 날). */
    fun activeDays(): List<LocalDate> = prefs.all.keys
        .filter { it.startsWith("done_") }
        .mapNotNull { runCatching { LocalDate.parse(it.removePrefix("done_")) }.getOrNull() }
        .filter { doneOn(it).isNotEmpty() }
        .sorted()

    /** 가장 길게 이어서 운동한 날 수. */
    fun bestStreak(): Int {
        var best = 0
        var run = 0
        var prev: LocalDate? = null
        for (d in activeDays()) {
            run = if (prev != null && prev.plusDays(1) == d) run + 1 else 1
            best = maxOf(best, run)
            prev = d
        }
        return best
    }

    /** 지금까지 끝낸 운동 수(같은 날 같은 운동은 한 번). */
    fun totalDone(): Int = activeDays().sumOf { doneOn(it).size }

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
