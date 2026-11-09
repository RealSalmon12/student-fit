package com.realsalmon.studentfit.data

import android.content.Context
import androidx.core.content.edit

/** 앱 설정(음성 안내, 매일 알림). 기기 안에만 저장해요. */
class AppSettings(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("settings", Context.MODE_PRIVATE)

    var voiceGuide: Boolean
        get() = prefs.getBoolean("voice_guide", true)
        set(v) = prefs.edit { putBoolean("voice_guide", v) }

    var reminderOn: Boolean
        get() = prefs.getBoolean("reminder_on", false)
        set(v) = prefs.edit { putBoolean("reminder_on", v) }

    /** 하루 중 분(0~1439). 기본 저녁 8시. */
    var reminderMinute: Int
        get() = prefs.getInt("reminder_minute", 20 * 60)
        set(v) = prefs.edit { putInt("reminder_minute", v.coerceIn(0, 24 * 60 - 1)) }
}
