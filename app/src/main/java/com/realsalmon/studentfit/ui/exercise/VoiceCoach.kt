package com.realsalmon.studentfit.ui.exercise

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

/**
 * 따라하기 음성 안내: 구간이 바뀌면 말해 주고, 횟수를 세고, 끝나기 3초 전부터 셋·둘·하나를 세요.
 * 구간이 바뀔 때 짧게 진동도 해요.
 */
class VoiceCoach(context: Context) {
    private var ready = false
    private val tts = TextToSpeech(context.applicationContext) { status ->
        if (status == TextToSpeech.SUCCESS) ready = true
    }.also { it.language = Locale.KOREAN }

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= 31) {
        (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    var enabled = true

    fun say(text: String, flush: Boolean = true) {
        if (!enabled || !ready) return
        tts.language = Locale.KOREAN
        tts.speak(text, if (flush) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD, null, text.hashCode().toString())
    }

    fun buzz() {
        val v = vibrator ?: return
        if (!v.hasVibrator()) return
        if (Build.VERSION.SDK_INT >= 26) {
            v.vibrate(VibrationEffect.createOneShot(120, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            v.vibrate(120)
        }
    }

    fun stop() = tts.stop()

    fun release() {
        tts.stop()
        tts.shutdown()
    }

    companion object {
        private val NATIVE = listOf("하나", "둘", "셋", "넷", "다섯", "여섯", "일곱", "여덟", "아홉", "열")

        /** 1~10은 하나·둘로, 그 위는 숫자로 읽어요. */
        fun count(n: Int): String = NATIVE.getOrNull(n - 1) ?: n.toString()
    }
}

@Composable
fun rememberVoiceCoach(): VoiceCoach {
    val context = LocalContext.current
    val coach = remember { VoiceCoach(context) }
    DisposableEffect(coach) { onDispose { coach.release() } }
    return coach
}
