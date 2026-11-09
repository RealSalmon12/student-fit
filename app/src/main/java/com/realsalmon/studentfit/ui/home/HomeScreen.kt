package com.realsalmon.studentfit.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.realsalmon.studentfit.data.HealthRepository
import com.realsalmon.studentfit.data.HealthStatus
import com.realsalmon.studentfit.data.WatchSummary
import com.realsalmon.studentfit.domain.AlignmentDiagram
import com.realsalmon.studentfit.domain.exercise.RoutineRecommender
import com.realsalmon.studentfit.ui.components.ActionRow
import com.realsalmon.studentfit.ui.components.AlignmentFigure
import com.realsalmon.studentfit.ui.components.HeroCard
import com.realsalmon.studentfit.ui.components.Pill
import com.realsalmon.studentfit.ui.components.SectionHeader
import com.realsalmon.studentfit.ui.components.SoftCard
import com.realsalmon.studentfit.ui.exercise.ExerciseThumbnail
import com.realsalmon.studentfit.ui.navigation.AppNavigator
import com.realsalmon.studentfit.ui.navigation.Routes
import com.realsalmon.studentfit.ui.rememberProfile
import com.realsalmon.studentfit.ui.rememberRoutineLog
import com.realsalmon.studentfit.ui.theme.Chalk
import com.realsalmon.studentfit.ui.theme.NumberStyle
import com.realsalmon.studentfit.ui.theme.Plumb
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** 남색 패널 위에서 쓰는 색 */
private val OnInk = Chalk
private val OnInkSoft = Chalk.copy(alpha = 0.68f)
private val FigureBody = Color(0xFF3A4760)
private val Alert = Color(0xFFFF7A7F)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(go: AppNavigator) {
    val context = LocalContext.current
    val (_, result) = rememberProfile()
    val log = rememberRoutineLog()
    val routine = remember(result) { RoutineRecommender.recommend(result) }
    var done by remember { mutableStateOf(log.doneOn()) }
    val streak = remember(done) { log.streak() }

    var watch by remember { mutableStateOf<WatchSummary?>(null) }
    LaunchedEffect(Unit) {
        val repo = HealthRepository(context)
        watch = runCatching {
            if (repo.status() == HealthStatus.AVAILABLE && repo.hasPermissions()) repo.readSummary() else null
        }.getOrNull()
    }

    val today = LocalDate.now().format(DateTimeFormatter.ofPattern("M월 d일 EEEE", Locale.KOREAN))
    val total = routine.size
    val count = routine.count { it.exercise.id in done }
    val left = total - count

    LazyColumn(
        Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(today, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                    if (streak > 0) {
                        Pill("${streak}일 연속", MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer)
                    }
                }
                Text(
                    when {
                        total == 0 -> "오늘도 바른 자세로"
                        left == 0 -> "오늘 루틴 끝!"
                        else -> "오늘 운동 ${left}개 남았어요"
                    },
                    style = MaterialTheme.typography.headlineMedium,
                )
            }
        }

        // 내 체형: 정렬선 그림
        item {
            val issues = result?.postureIssues ?: emptySet()
            val figure = remember(issues) { AlignmentDiagram.side(issues) }
            HeroCard(onClick = if (result != null) ({ go.tab(Routes.BODY) }) else null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (result != null) {
                            Text("내 체형", color = OnInkSoft, style = MaterialTheme.typography.labelLarge)
                            Text(result.typeTitle, color = OnInk, style = MaterialTheme.typography.headlineLarge)
                            Text("BMI ${result.bmiText} (${result.bmiCategory.label})", color = OnInkSoft, style = MaterialTheme.typography.bodyMedium)
                            val tags = result.postureIssues.map { it.label } + result.concerns.map { it.label }
                            if (tags.isNotEmpty()) {
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    tags.take(4).forEach { Pill(it, Color.White.copy(alpha = 0.1f), OnInk) }
                                    if (tags.size > 4) Pill("+${tags.size - 4}", Color.White.copy(alpha = 0.1f), OnInk)
                                }
                            } else {
                                Text("귀부터 발목까지 정렬선에 잘 맞아요.", color = OnInkSoft, style = MaterialTheme.typography.bodyMedium)
                            }
                        } else {
                            Text("내 자세는 정렬선에 맞을까?", color = OnInk, style = MaterialTheme.typography.headlineMedium)
                            Text(
                                "옆에서 본 귀·어깨·골반·발목이 노란 선 위에 있으면 바른 자세예요. 사진을 찍거나 직접 입력해서 확인해 보세요.",
                                color = OnInkSoft, style = MaterialTheme.typography.bodyMedium,
                            )
                            Spacer(Modifier.height(6.dp))
                            Button(
                                onClick = go::camera,
                                colors = ButtonDefaults.buttonColors(containerColor = Plumb, contentColor = Color(0xFF1B2433)),
                            ) {
                                Icon(Icons.Filled.CameraAlt, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("사진으로 분석")
                            }
                            OutlinedButton(onClick = go::bodyInput, colors = ButtonDefaults.outlinedButtonColors(contentColor = OnInk)) {
                                Icon(Icons.Filled.EditNote, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("직접 입력")
                            }
                        }
                    }
                    AlignmentFigure(
                        figure,
                        Modifier.width(96.dp).height(190.dp),
                        bodyColor = FigureBody,
                        dotColor = OnInk,
                        alertColor = Alert,
                        showLabels = false,
                    )
                }
            }
        }

        // 오늘의 루틴: 순서대로 하는 목록
        item {
            SectionHeader("오늘의 루틴") {
                Text("$count/$total", style = NumberStyle.copy(fontSize = NumberStyle.fontSize * 0.7f), color = MaterialTheme.colorScheme.primary)
            }
        }
        item {
            SoftCard {
                LinearProgressIndicator(
                    progress = { if (total == 0) 0f else count / total.toFloat() },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(MaterialTheme.shapes.extraSmall),
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    strokeCap = StrokeCap.Butt,
                    gapSize = 0.dp,
                    drawStopIndicator = {},
                )
                routine.forEachIndexed { i, rec ->
                    val isDone = rec.exercise.id in done
                    if (i > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Row(
                        Modifier.fillMaxWidth().clickable { go.exercise(rec.exercise.id) }.padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "${i + 1}",
                            style = NumberStyle.copy(fontSize = NumberStyle.fontSize * 0.75f),
                            color = if (isDone) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.width(28.dp),
                        )
                        Box(Modifier.size(52.dp).clip(MaterialTheme.shapes.medium).background(MaterialTheme.colorScheme.surfaceContainer)) {
                            ExerciseThumbnail(rec.exercise, Modifier.fillMaxSize())
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                rec.exercise.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (isDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1, overflow = TextOverflow.Ellipsis,
                            )
                            Text(rec.exercise.dose, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { log.setDone(rec.exercise.id, !isDone); done = log.doneOn() }) {
                            Icon(
                                if (isDone) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                                contentDescription = if (isDone) "완료 취소" else "완료로 표시",
                                tint = if (isDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            )
                        }
                    }
                }
                TextButton(onClick = { go.tab(Routes.EXERCISES) }, modifier = Modifier.align(Alignment.End)) { Text("운동 전체 보기") }
            }
        }

        // 워치: 점수판
        item { SectionHeader("갤럭시 워치") }
        item {
            val w = watch
            if (w != null) {
                SoftCard {
                    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                        Score("%,d".format(w.stepsToday), "걸음", Modifier.weight(1f))
                        VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Score((w.latestHeart ?: w.heartAvg)?.toString() ?: "-", "심박수 bpm", Modifier.weight(1f))
                        VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Score(w.activeKcal.toInt().toString(), "활동 kcal", Modifier.weight(1f))
                    }
                }
            } else {
                ActionRow(
                    Icons.Filled.Watch,
                    "워치 연결하기",
                    "걸음 수와 심박수를 갤럭시 워치에서 가져와요",
                    container = MaterialTheme.colorScheme.secondaryContainer,
                    content = MaterialTheme.colorScheme.onSecondaryContainer,
                ) { go.tab(Routes.WATCH) }
            }
        }
    }
}

@Composable
private fun Score(value: String, label: String, modifier: Modifier) {
    Column(modifier.padding(horizontal = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = NumberStyle.copy(fontSize = NumberStyle.fontSize * 0.85f), maxLines = 1)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
