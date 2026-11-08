package com.realsalmon.studentfit.ui.exercise

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.realsalmon.studentfit.domain.exercise.ExerciseCatalog
import com.realsalmon.studentfit.ui.components.BackTopBar
import com.realsalmon.studentfit.ui.components.Pill
import com.realsalmon.studentfit.ui.components.SoftCard
import com.realsalmon.studentfit.ui.rememberRoutineLog

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExerciseDetailScreen(id: String, onBack: () -> Unit) {
    val ex = remember(id) { ExerciseCatalog.find(id) }
    if (ex == null) {
        Scaffold(topBar = { BackTopBar("운동", onBack) }) { p ->
            Text("운동을 찾을 수 없어요", Modifier.padding(p).padding(20.dp))
        }
        return
    }
    val log = rememberRoutineLog()
    var done by remember { mutableStateOf(ex.id in log.doneOn()) }
    var playing by remember { mutableStateOf(true) }
    var speed by remember { mutableFloatStateOf(1f) }

    Scaffold(
        topBar = { BackTopBar(ex.name, onBack) },
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Box(Modifier.navigationBarsPadding().padding(horizontal = 20.dp, vertical = 12.dp)) {
                if (done) {
                    OutlinedButton(
                        onClick = { log.setDone(ex.id, false); done = false },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                    ) { Text("완료 취소") }
                } else {
                    Button(
                        onClick = { log.setDone(ex.id, true); done = true },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                    ) {
                        Icon(Icons.Filled.Check, null); Spacer(Modifier.width(8.dp))
                        Text("운동 완료", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .clip(MaterialTheme.shapes.large)
                    .background(
                        Brush.verticalGradient(
                            listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.surfaceContainerLow),
                        ),
                    ),
            ) {
                SkeletonViewer(ex.motion, skeletonColors(), Modifier.fillMaxSize(), playing = playing, speed = speed)
                Pill(
                    "3D · 끌어서 돌려보기",
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                    MaterialTheme.colorScheme.onSurface,
                    Modifier.align(Alignment.TopStart).padding(12.dp),
                )
                Row(
                    Modifier.align(Alignment.BottomEnd).padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilledTonalIconButton(onClick = { speed = if (speed == 1f) 0.5f else 1f }) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Speed, contentDescription = "속도")
                        }
                    }
                    FilledTonalIconButton(onClick = { playing = !playing }) {
                        Icon(if (playing) Icons.Filled.Pause else Icons.Filled.PlayArrow, contentDescription = if (playing) "멈춤" else "재생")
                    }
                }
                if (speed != 1f) {
                    Pill("느리게", MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer,
                        Modifier.align(Alignment.TopEnd).padding(12.dp))
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(ex.name, style = MaterialTheme.typography.headlineSmall)
                Text(ex.summary, color = MaterialTheme.colorScheme.onSurfaceVariant)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Pill(ex.category.label, MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
                    Pill(ex.dose, MaterialTheme.colorScheme.surfaceContainerHigh, MaterialTheme.colorScheme.onSurface)
                    ex.targetLabels.forEach {
                        Pill(it, MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }
            }

            SoftCard {
                Text("이렇게 해요", style = MaterialTheme.typography.titleMedium)
                ex.steps.forEachIndexed { i, step ->
                    Row(verticalAlignment = Alignment.Top) {
                        Box(
                            Modifier.size(26.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("${i + 1}", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.labelLarge,
                                textAlign = TextAlign.Center)
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(step, Modifier.padding(top = 2.dp))
                    }
                }
            }

            SoftCard(color = MaterialTheme.colorScheme.tertiaryContainer) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Lightbulb, null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
                    Spacer(Modifier.width(10.dp))
                    Text(ex.tips, color = MaterialTheme.colorScheme.onTertiaryContainer)
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
