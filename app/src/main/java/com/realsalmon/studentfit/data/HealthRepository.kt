package com.realsalmon.studentfit.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateGroupByPeriodRequest
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId

enum class HealthStatus { AVAILABLE, NEEDS_UPDATE, UNAVAILABLE }

data class DaySteps(val date: LocalDate, val steps: Long)

data class WorkoutInfo(val title: String, val start: Instant, val minutes: Long)

data class WatchSummary(
    val stepsToday: Long,
    val heartAvg: Long?,
    val heartMin: Long?,
    val heartMax: Long?,
    val latestHeart: Long?,
    val activeKcal: Double,
    val week: List<DaySteps>,
    val workouts: List<WorkoutInfo>,
)

/**
 * 갤럭시 워치 8 기록은 삼성 헬스 앱을 거쳐 Health Connect 에 저장돼요.
 * 이 앱은 Health Connect 에서 걸음 수, 심박수, 운동 기록을 읽어와요.
 */
class HealthRepository(private val context: Context) {

    val permissions: Set<String> = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(ExerciseSessionRecord::class),
        HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class),
    )

    fun status(): HealthStatus = when (HealthConnectClient.getSdkStatus(context, PROVIDER)) {
        HealthConnectClient.SDK_AVAILABLE -> HealthStatus.AVAILABLE
        HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> HealthStatus.NEEDS_UPDATE
        else -> HealthStatus.UNAVAILABLE
    }

    private val client by lazy { HealthConnectClient.getOrCreate(context) }

    fun permissionContract() = PermissionController.createRequestPermissionResultContract()

    suspend fun hasPermissions(): Boolean =
        client.permissionController.getGrantedPermissions().containsAll(permissions)

    suspend fun readSummary(): WatchSummary {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val startOfDay = today.atStartOfDay(zone).toInstant()
        val now = Instant.now()
        val todayRange = TimeRangeFilter.between(startOfDay, now)

        val agg = client.aggregate(
            AggregateRequest(
                metrics = setOf(
                    StepsRecord.COUNT_TOTAL,
                    HeartRateRecord.BPM_AVG, HeartRateRecord.BPM_MIN, HeartRateRecord.BPM_MAX,
                    ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL,
                ),
                timeRangeFilter = todayRange,
            ),
        )

        val weekStart = today.minusDays(6)
        val weekly = client.aggregateGroupByPeriod(
            AggregateGroupByPeriodRequest(
                metrics = setOf(StepsRecord.COUNT_TOTAL),
                timeRangeFilter = TimeRangeFilter.between(weekStart.atStartOfDay(), today.plusDays(1).atStartOfDay()),
                timeRangeSlicer = Period.ofDays(1),
            ),
        )
        val stepsByDay = weekly.associate { it.startTime.toLocalDate() to (it.result[StepsRecord.COUNT_TOTAL] ?: 0L) }
        val week = (0..6).map { weekStart.plusDays(it.toLong()) }.map { DaySteps(it, stepsByDay[it] ?: 0L) }

        val latestHeart = client.readRecords(
            ReadRecordsRequest(
                recordType = HeartRateRecord::class,
                timeRangeFilter = TimeRangeFilter.between(now.minus(Duration.ofHours(12)), now),
                ascendingOrder = false,
                pageSize = 1,
            ),
        ).records.firstOrNull()?.samples?.maxByOrNull { it.time }?.beatsPerMinute

        val sessions = client.readRecords(
            ReadRecordsRequest(
                recordType = ExerciseSessionRecord::class,
                timeRangeFilter = TimeRangeFilter.between(now.minus(Duration.ofDays(7)), now),
                ascendingOrder = false,
                pageSize = 10,
            ),
        ).records.map {
            WorkoutInfo(
                title = it.title?.takeIf { t -> t.isNotBlank() } ?: exerciseName(it.exerciseType),
                start = it.startTime,
                minutes = Duration.between(it.startTime, it.endTime).toMinutes(),
            )
        }

        return WatchSummary(
            stepsToday = agg[StepsRecord.COUNT_TOTAL] ?: 0L,
            heartAvg = agg[HeartRateRecord.BPM_AVG],
            heartMin = agg[HeartRateRecord.BPM_MIN],
            heartMax = agg[HeartRateRecord.BPM_MAX],
            latestHeart = latestHeart,
            activeKcal = agg[ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL]?.inKilocalories ?: 0.0,
            week = week,
            workouts = sessions,
        )
    }

    /** Health Connect 설치/업데이트 화면 (안드로이드 13 이하에서만 필요). */
    fun installIntent(): Intent = Intent(Intent.ACTION_VIEW).apply {
        setPackage("com.android.vending")
        data = Uri.parse("market://details?id=$PROVIDER&url=healthconnect%3A%2F%2Fonboarding")
        putExtra("overlay", true)
        putExtra("callerId", context.packageName)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    /** 삼성 헬스 앱 열기(동기화 설정 안내용). */
    fun samsungHealthIntent(): Intent? =
        context.packageManager.getLaunchIntentForPackage("com.sec.android.app.shealth")

    private fun exerciseName(type: Int): String = when (type) {
        ExerciseSessionRecord.EXERCISE_TYPE_WALKING -> "걷기"
        ExerciseSessionRecord.EXERCISE_TYPE_RUNNING -> "달리기"
        ExerciseSessionRecord.EXERCISE_TYPE_BIKING -> "자전거"
        ExerciseSessionRecord.EXERCISE_TYPE_SWIMMING_POOL -> "수영"
        ExerciseSessionRecord.EXERCISE_TYPE_STRENGTH_TRAINING -> "근력 운동"
        ExerciseSessionRecord.EXERCISE_TYPE_YOGA -> "요가"
        ExerciseSessionRecord.EXERCISE_TYPE_HIKING -> "등산"
        ExerciseSessionRecord.EXERCISE_TYPE_STRETCHING -> "스트레칭"
        ExerciseSessionRecord.EXERCISE_TYPE_BADMINTON -> "배드민턴"
        ExerciseSessionRecord.EXERCISE_TYPE_BASKETBALL -> "농구"
        ExerciseSessionRecord.EXERCISE_TYPE_SOCCER -> "축구"
        else -> "운동"
    }

    companion object {
        const val PROVIDER = "com.google.android.apps.healthdata"
    }
}
