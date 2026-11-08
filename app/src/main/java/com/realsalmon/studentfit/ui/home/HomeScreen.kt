package com.realsalmon.studentfit.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import com.realsalmon.studentfit.data.HealthRepository
import com.realsalmon.studentfit.data.HealthStatus
import com.realsalmon.studentfit.data.WatchSummary
import com.realsalmon.studentfit.domain.exercise.RoutineRecommender
import com.realsalmon.studentfit.ui.components.ActionRow
import com.realsalmon.studentfit.ui.components.HeroCard
import com.realsalmon.studentfit.ui.components.Pill
import com.realsalmon.studentfit.ui.components.SectionHeader
import com.realsalmon.studentfit.ui.components.SoftCard
import com.realsalmon.studentfit.ui.navigation.AppNavigator
import com.realsalmon.studentfit.ui.navigation.Routes
import com.realsalmon.studentfit.ui.rememberProfile
import com.realsalmon.studentfit.ui.rememberRoutineLog
import com.realsalmon.studentfit.ui.theme.Coral
import com.realsalmon.studentfit.ui.theme.Mint
import com.realsalmon.studentfit.ui.theme.NumberStyle
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

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

    LazyColumn(
        Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(today, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("오늘도 바른 자세로", style = MaterialTheme.typography.headlineMedium)
                }
                if (streak > 0) {
                    Pill(
                        "🔥 ${streak}일 연속",
                        MaterialTheme.colorScheme.tertiaryContainer,
                        MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                }
            }
        }

        item {
            if (result != null) {
                HeroCard(onClick = { go.tab(Routes.BODY) }) {
                    Text("내 체형", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelLarge)
                    Text(result.typeTitle, color = Color.White, style = MaterialTheme.typography.displaySmall)
                    Text("BMI ${result.bmiText} · ${result.bmiCategory.label}", color = Color.White.copy(alpha = 0.9f))
                    val tags = result.postureIssues.map { it.label } + result.concerns.map { it.label }
                    if (tags.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            tags.take(3).forEach { Pill(it, Color.White.copy(alpha = 0.18f), Color.White) }
                            if (tags.size > 3) Pill("+${tags.size - 3}", Color.White.copy(alpha = 0.18f), Color.White)
                        }
                    }
                }
            } else {
                HeroCard {
                    Text("1분 만에", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelLarge)
                    Text("내 체형 알아보기", color = Color.White, style = MaterialTheme.typography.headlineMedium)
                    Text(
                        "카메라로 자세를 찍거나 직접 입력하면 맞춤 교정 운동을 추천해 줘요.",
                        color = Color.White.copy(alpha = 0.9f),
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilledTonalButton(onClick = go::camera) {
                            Icon(Icons.Filled.CameraAlt, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("카메라 분석")
                        }
                        FilledTonalButton(onClick = go::bodyInput) {
                            Icon(Icons.Filled.EditNote, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("직접 입력")
                        }
                    }
                }
            }
        }

        item {
            SectionHeader("오늘의 루틴") {
                TextButton(onClick = { go.tab(Routes.EXERCISES) }) { Text("전체 보기") }
            }
        }
        item {
            val total = routine.size
            val count = routine.count { it.exercise.id in done }
            SoftCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { if (total == 0) 0f else count / total.toFloat() },
                            modifier = Modifier.size(64.dp),
                            strokeWidth = 7.dp,
                            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                            strokeCap = StrokeCap.Round,
                        )
                        Text("$count/$total", style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            if (count == total && total > 0) "오늘 루틴 완료! 🎉" else "${total - count}개 남았어요",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            "운동을 누르면 3D 동작을 볼 수 있어요",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                routine.take(4).forEach { rec ->
                    val isDone = rec.exercise.id in done
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            TextButton(
                                onClick = { go.exercise(rec.exercise.id) },
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                            ) {
                                Text(rec.exercise.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Text(rec.exercise.dose, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        androidx.compose.material3.IconButton(onClick = {
                            log.setDone(rec.exercise.id, !isDone); done = log.doneOn()
                        }) {
                            Icon(
                                if (isDone) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                                contentDescription = if (isDone) "완료 취소" else "완료",
                                tint = if (isDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            )
                        }
                    }
                }
            }
        }

        item { SectionHeader("갤럭시 워치") }
        item {
            val w = watch
            if (w != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MiniStat(Icons.AutoMirrored.Filled.DirectionsWalk, "%,d".format(w.stepsToday), "걸음", Mint, Modifier.weight(1f))
                    MiniStat(Icons.Filled.Favorite, (w.latestHeart ?: w.heartAvg)?.toString() ?: "-", "bpm", Coral, Modifier.weight(1f))
                    MiniStat(Icons.Filled.LocalFireDepartment, w.activeKcal.toInt().toString(), "kcal", MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                }
            } else {
                ActionRow(
                    Icons.Filled.Favorite,
                    "워치 연결하기",
                    "걸음 수와 심박수를 갤럭시 워치에서 가져와요",
                    container = MaterialTheme.colorScheme.tertiaryContainer,
                    content = MaterialTheme.colorScheme.tertiary,
                ) { go.tab(Routes.WATCH) }
            }
        }
    }
}

@Composable
private fun MiniStat(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, unit: String, tint: Color, modifier: Modifier) {
    SoftCard(modifier) {
        Box(Modifier.size(32.dp).clip(CircleShape).background(tint.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(18.dp))
        }
        Text(value, style = NumberStyle.copy(fontSize = NumberStyle.fontSize * 0.75f))
        Text(unit, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
