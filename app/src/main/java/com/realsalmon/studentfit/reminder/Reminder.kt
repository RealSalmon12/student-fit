package com.realsalmon.studentfit.reminder

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.realsalmon.studentfit.MainActivity
import com.realsalmon.studentfit.R
import com.realsalmon.studentfit.data.AppSettings
import com.realsalmon.studentfit.data.RoutineLog
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

/** 매일 정해진 시간에 "오늘 교정 운동" 알림을 보내요. */
object Reminder {
    private const val CHANNEL = "daily_reminder"
    private const val REQUEST = 7001

    private fun pending(context: Context): PendingIntent = PendingIntent.getBroadcast(
        context, REQUEST,
        Intent(context, ReminderReceiver::class.java).setAction(ACTION_FIRE),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    const val ACTION_FIRE = "com.realsalmon.studentfit.REMINDER"

    /** 설정에 맞게 다음 알림을 예약하거나 취소해요. */
    fun sync(context: Context) {
        val settings = AppSettings(context)
        val am = context.getSystemService(AlarmManager::class.java) ?: return
        val pi = pending(context)
        am.cancel(pi)
        if (!settings.reminderOn) return
        val m = settings.reminderMinute
        val now = LocalDateTime.now()
        var next = now.toLocalDate().atTime(m / 60, m % 60)
        if (!next.isAfter(now.plusSeconds(30))) next = next.plusDays(1)
        val at = next.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        // 정확한 알람 권한 없이도 되는 방식(몇 분 늦을 수 있어요)
        am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
    }

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= 26) {
            val nm = context.getSystemService(NotificationManager::class.java)
            nm?.createNotificationChannel(
                NotificationChannel(CHANNEL, "매일 운동 알림", NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "정한 시간에 오늘의 교정 운동을 알려줘요"
                },
            )
        }
    }

    fun show(context: Context) {
        ensureChannel(context)
        val nm = NotificationManagerCompat.from(context)
        if (!nm.areNotificationsEnabled()) return
        val log = RoutineLog(context)
        val doneToday = log.doneOn(LocalDate.now()).size
        val streak = log.streak()
        val text = when {
            doneToday > 0 -> "오늘 ${doneToday}개 했어요. 하나만 더 해볼까요?"
            streak > 0 -> "${streak}일 연속 기록이 끊기지 않게 오늘도 5분만!"
            else -> "자세 교정은 매일 조금씩이 제일 좋아요. 5분만 해볼까요?"
        }
        val open = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val n = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(R.drawable.ic_stat_fit)
            .setContentTitle("오늘의 교정 운동 시간이에요")
            .setContentText(text)
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        try {
            nm.notify(1, n)
        } catch (_: SecurityException) {
            // 알림 권한이 없으면 조용히 넘어가요
        }
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Reminder.ACTION_FIRE -> {
                Reminder.show(context)
                Reminder.sync(context) // 다음 날 예약
            }
            Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED, Intent.ACTION_TIMEZONE_CHANGED -> Reminder.sync(context)
        }
    }
}
