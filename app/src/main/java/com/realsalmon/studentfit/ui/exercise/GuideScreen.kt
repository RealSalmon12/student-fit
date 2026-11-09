package com.realsalmon.studentfit.ui.exercise

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Button
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import com.realsalmon.studentfit.data.AppSettings
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.realsalmon.studentfit.domain.exercise.ExerciseCatalog
import com.realsalmon.studentfit.domain.exercise.GuidedPlan
import com.realsalmon.studentfit.domain.exercise.Segment
import com.realsalmon.studentfit.domain.exercise.Side
import com.realsalmon.studentfit.ui.rememberRoutineLog

/**
 * 따라하기: 준비 → 세트(한쪽씩이면 왼쪽 → 오른쪽) → 휴식 … → 완료.
 * 3D 동작 한 바퀴가 1회로, 화면의 횟수와 캐릭터 움직임이 맞물려요.
 */
@Composable
fun GuideScreen(id: String, onClose: () -> Unit) {
    val ex = remember(id) { ExerciseCatalog.find(id) }
    if (ex == null) {
        Text("운동을 찾을 수 없어요", Modifier.statusBarsPadding().padding(20.dp))
        return
    }
    val plan = remember(ex) { GuidedPlan(ex) }
    val log = rememberRoutineLog()
    var time by remember { mutableFloatStateOf(0f) }
    var paused by remember { mutableStateOf(false) }
    // 다시 시작할 때마다 바뀌어서 시계가 다시 돌아가요("한 번 더"가 멈춰 있던 문제)
    var runId by remember { mutableStateOf(0) }
    val context = LocalContext.current
    val settings = remember { AppSettings(context) }
    var voiceOn by remember { mutableStateOf(settings.voiceGuide) }
    val coach = rememberVoiceCoach()
    LaunchedEffect(voiceOn) {
        coach.enabled = voiceOn
        settings.voiceGuide = voiceOn
        if (!voiceOn) coach.stop()
    }

    // 따라하는 동안 화면이 꺼지지 않게
    val view = LocalView.current
    DisposableEffect(Unit) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }

    LaunchedEffect(paused, runId) {
        if (paused) { coach.stop(); return@LaunchedEffect }
        var last = withFrameNanos { it }
        while (time < plan.totalSec) {
            val now = withFrameNanos { it }
            time += (now - last) / 1_000_000_000f
            last = now
        }
    }

    val state = plan.stateAt(time)
    LaunchedEffect(state.finished) { if (state.finished) log.setDone(ex.id, true) }

    val seg = state.segment

    // 음성 안내 + 진동
    LaunchedEffect(state.index, state.finished, runId) {
        coach.buzz()
        when {
            state.finished -> coach.say("끝! 수고했어요")
            seg is Segment.Prepare -> coach.say("${ex.name}. 준비 자세를 잡으세요")
            seg is Segment.Work -> {
                val sideText = if (seg.side != Side.BOTH) " ${seg.side.label}" else ""
                coach.say(if (ex.rx.isHold) "${seg.set}세트$sideText. 버티세요" else "${seg.set}세트$sideText 시작")
            }
            seg is Segment.Switch -> coach.say("반대쪽으로 바꾸세요")
            seg is Segment.Rest -> coach.say("쉬는 시간 ${seg.durationSec.toInt()}초")
        }
    }
    LaunchedEffect(state.rep, state.index) {
        val rep = state.rep
        if (seg is Segment.Work && rep != null) coach.say(VoiceCoach.count(rep), flush = rep > 1)
    }
    val secLeft = plan.secondsLeft(state)
    LaunchedEffect(secLeft, state.index) {
        val countsDown = seg != null && !(seg is Segment.Work && state.rep != null)
        if (!paused && countsDown && secLeft in 1..3 && (seg?.durationSec ?: 0f) > 4f) coach.say(VoiceCoach.count(secLeft))
    }
    val side = (seg as? Segment.Work)?.side
        ?: (plan.segments.getOrNull(state.index + 1) as? Segment.Work)?.side
    val motion = if (side == Side.RIGHT) ex.mirroredMotion else ex.motion

    // 캐릭터가 보여줄 시각: 운동 중엔 횟수와 맞물려 움직이고, 쉬는 동안엔 첫 자세로 대기
    val viewerTime = when (seg) {
        is Segment.Work -> if (ex.rx.isHold) minOf(state.elapsedInSegment, ex.holdAtSec) else state.elapsedInSegment
        else -> 0f
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClose) { Icon(Icons.Filled.Close, "닫기") }
            Text(ex.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            Text(ex.dose, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            IconButton(onClick = { voiceOn = !voiceOn }) {
                Icon(
                    if (voiceOn) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                    if (voiceOn) "음성 안내 끄기" else "음성 안내 켜기",
                )
            }
        }
        LinearProgressIndicator(
            progress = { state.totalProgress },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            strokeCap = StrokeCap.Round,
        )

        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp)
                .clip(MaterialTheme.shapes.large)
                .background(
                    Brush.verticalGradient(
                        listOf(MaterialTheme.colorScheme.surfaceContainerLow, MaterialTheme.colorScheme.surfaceContainer),
                    ),
                ),
        ) {
            ModelViewer(
                motion = motion,
                modifier = Modifier.fillMaxSize(),
                view = ex.view,
                focusUpper = ex.focusUpper,
                time = viewerTime,
            )
            if (side != null && side != Side.BOTH) {
                Text(
                    "${side.label} 다리·팔",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.align(Alignment.TopStart).padding(12.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
        }

        // 상태 패널
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            AnimatedContent(targetState = headline(seg, ex.rx.sets, side), label = "headline") { text ->
                Text(text, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            }
            val big = when {
                state.finished -> "완료!"
                seg is Segment.Work && state.rep != null -> "${state.rep} / ${ex.rx.reps}"
                else -> "${plan.secondsLeft(state)}"
            }
            Text(big, fontSize = 64.sp, fontWeight = FontWeight.Black, style = MaterialTheme.typography.displayLarge)
            Text(
                subline(seg, plan.secondsLeft(state), ex.rx.holdSec, state.elapsedInSegment, plan.repSec, ex.steps.firstOrNull()),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Row(
            Modifier.fillMaxWidth().padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (state.finished) {
                OutlinedButton(onClick = { time = 0f; paused = false; runId++ }, modifier = Modifier.height(56.dp)) { Text("한 번 더") }
                Button(onClick = onClose, modifier = Modifier.height(56.dp).weight(1f)) { Text("완료하고 돌아가기") }
            } else {
                Spacer(Modifier.size(56.dp))
                FilledIconButton(onClick = { paused = !paused }, modifier = Modifier.size(72.dp)) {
                    Icon(if (paused) Icons.Filled.PlayArrow else Icons.Filled.Pause, if (paused) "계속" else "일시정지",
                        modifier = Modifier.size(36.dp))
                }
                FilledTonalIconButton(
                    onClick = { time = plan.startOf(state.index + 1) },
                    modifier = Modifier.size(56.dp),
                ) { Icon(Icons.Filled.SkipNext, "다음 구간으로") }
            }
        }
    }
}

private fun headline(seg: Segment?, sets: Int, side: Side?): String = when (seg) {
    null -> "오늘 운동 기록에 저장했어요"
    is Segment.Prepare -> "준비 자세를 잡아요"
    is Segment.Work -> buildString {
        append("${seg.set} / ${sets}세트")
        if (seg.side != Side.BOTH) append(" · ${seg.side.label}")
    }
    is Segment.Switch -> "${side?.label ?: "반대쪽"}으로 바꿔요"
    is Segment.Rest -> "쉬는 시간 · 다음은 ${seg.nextSet}세트"
}

private fun subline(seg: Segment?, secondsLeft: Int, holdSec: Int?, elapsed: Float, repSec: Float, firstStep: String?): String = when (seg) {
    null -> "수고했어요!"
    is Segment.Prepare -> firstStep ?: "곧 시작해요"
    is Segment.Work -> if (holdSec != null && seg.durationSec <= holdSec + 0.01f) "${secondsLeft}초 남았어요" else "캐릭터 속도에 맞춰 천천히"
    is Segment.Switch -> "${secondsLeft}초 뒤 시작해요"
    is Segment.Rest -> "물 한 모금 마시고 숨을 골라요"
}
