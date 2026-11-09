package com.realsalmon.studentfit.ui.exercise

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SlowMotionVideo
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.realsalmon.studentfit.domain.exercise.Exercise
import com.realsalmon.studentfit.domain.exercise.ExerciseCatalog
import com.realsalmon.studentfit.domain.motion.ViewAngle
import com.realsalmon.studentfit.ui.components.BackTopBar
import com.realsalmon.studentfit.ui.components.Pill
import com.realsalmon.studentfit.ui.components.SoftCard
import com.realsalmon.studentfit.ui.rememberRoutineLog

/** 시점 버튼: 운동별 추천 시점 + 앞·옆·비스듬히 */
private fun viewChoices(ex: Exercise) = listOf(
    "추천" to ex.view,
    "앞" to ViewAngle(0f, 8f),
    "옆" to ViewAngle(90f, 8f),
    "비스듬히" to ViewAngle(40f, 22f),
    "뒤" to ViewAngle(180f, 10f),
)

@Composable
fun ExerciseViewerCard(
    ex: Exercise,
    modifier: Modifier = Modifier,
    height: Int = 360,
) {
    var playing by remember { mutableStateOf(true) }
    var slow by remember { mutableStateOf(false) }
    var viewIndex by remember(ex.id) { mutableStateOf(0) }
    var mirrored by remember(ex.id) { mutableStateOf(false) }
    val choices = remember(ex.id) { viewChoices(ex) }

    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(height.dp)
                .clip(MaterialTheme.shapes.large)
                .background(
                    Brush.verticalGradient(
                        listOf(MaterialTheme.colorScheme.surfaceContainerLow, MaterialTheme.colorScheme.surfaceContainer),
                    ),
                ),
        ) {
            ModelViewer(
                motion = if (mirrored) ex.mirroredMotion else ex.motion,
                modifier = Modifier.fillMaxSize(),
                view = choices[viewIndex].second,
                focusUpper = ex.focusUpper,
                playing = playing,
                speed = if (slow) 0.5f else 1f,
            )
            Pill(
                "끌어서 돌려보기",
                MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                MaterialTheme.colorScheme.onSurface,
                Modifier.align(Alignment.TopStart).padding(12.dp),
            )
            Row(
                Modifier.align(Alignment.BottomEnd).padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (ex.rx.perSide) {
                    FilledTonalIconButton(onClick = { mirrored = !mirrored }) {
                        Text(if (mirrored) "R" else "L", style = MaterialTheme.typography.labelLarge)
                    }
                }
                FilledTonalIconButton(
                    onClick = { slow = !slow },
                    colors = if (slow) IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    ) else IconButtonDefaults.filledTonalIconButtonColors(),
                ) { Icon(Icons.Filled.SlowMotionVideo, contentDescription = "느리게") }
                FilledTonalIconButton(onClick = { playing = !playing }) {
                    Icon(if (playing) Icons.Filled.Pause else Icons.Filled.PlayArrow, contentDescription = if (playing) "멈춤" else "재생")
                }
            }
        }
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            choices.forEachIndexed { i, (label, _) ->
                FilterChip(
                    selected = viewIndex == i,
                    onClick = { viewIndex = i },
                    label = { Text(label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExerciseDetailScreen(id: String, onBack: () -> Unit, onGuide: (String) -> Unit) {
    val ex = remember(id) { ExerciseCatalog.find(id) }
    if (ex == null) {
        Scaffold(topBar = { BackTopBar("운동", onBack) }) { p ->
            Text("운동을 찾을 수 없어요", Modifier.padding(p).padding(20.dp))
        }
        return
    }
    val log = rememberRoutineLog()
    var done by remember { mutableStateOf(ex.id in log.doneOn()) }

    Scaffold(
        topBar = { BackTopBar(ex.name, onBack) },
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Row(
                Modifier.navigationBarsPadding().padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedIconButton(
                    onClick = { done = !done; log.setDone(ex.id, done) },
                    modifier = Modifier.size(56.dp),
                ) {
                    Icon(
                        if (done) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                        contentDescription = if (done) "완료 취소" else "완료로 표시",
                        tint = if (done) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    )
                }
                Button(onClick = { onGuide(ex.id) }, modifier = Modifier.weight(1f).height(56.dp)) {
                    Icon(Icons.Filled.PlayArrow, null)
                    Spacer(Modifier.width(6.dp))
                    Text("따라하기 시작", style = MaterialTheme.typography.titleMedium)
                }
            }
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            ExerciseViewerCard(ex)

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(ex.name, style = MaterialTheme.typography.headlineSmall)
                Text(ex.summary, color = MaterialTheme.colorScheme.onSurfaceVariant)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Pill(ex.category.label, MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
                    Pill("난이도 ${ex.level.label}", MaterialTheme.colorScheme.surfaceContainerHigh, MaterialTheme.colorScheme.onSurface)
                    ex.targetLabels.forEach {
                        Pill(it, MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }
            }

            SoftCard {
                InfoLine("운동량", ex.dose)
                InfoLine("세트 사이 휴식", "${ex.rx.restSec}초")
                InfoLine("쓰는 근육", ex.muscles)
            }

            NumberedCard("준비 자세", ex.setup, MaterialTheme.colorScheme.secondary)
            NumberedCard("이렇게 움직여요", ex.steps, MaterialTheme.colorScheme.primary)

            SoftCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Air, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(10.dp))
                    Text("호흡", style = MaterialTheme.typography.titleMedium)
                }
                Text(ex.breathing)
            }

            SoftCard {
                Text("이것만은 피해요", style = MaterialTheme.typography.titleMedium)
                ex.mistakes.forEach { m ->
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Filled.Close, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(m)
                    }
                }
            }

            SoftCard(color = MaterialTheme.colorScheme.tertiaryContainer) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Filled.Lightbulb, null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
                    Spacer(Modifier.width(10.dp))
                    Text(ex.tips, color = MaterialTheme.colorScheme.onTertiaryContainer)
                }
            }
            Text(
                "통증이 느껴지면 바로 멈추고, 계속 아프면 병원에서 확인하세요.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row {
        Text(label, Modifier.width(96.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun NumberedCard(title: String, items: List<String>, accent: Color) {
    SoftCard {
        Text(title, style = MaterialTheme.typography.titleMedium)
        items.forEachIndexed { i, step ->
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    Modifier.size(26.dp).clip(CircleShape).background(accent),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("${i + 1}", color = Color.White, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
                }
                Spacer(Modifier.width(12.dp))
                Text(step, Modifier.padding(top = 2.dp))
            }
        }
    }
}

