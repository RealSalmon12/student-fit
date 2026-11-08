package com.realsalmon.studentfit.ui.watch

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.realsalmon.studentfit.data.WatchSummary
import com.realsalmon.studentfit.ui.components.HeroCard
import com.realsalmon.studentfit.ui.components.SectionHeader
import com.realsalmon.studentfit.ui.components.SoftCard
import com.realsalmon.studentfit.ui.theme.Coral
import com.realsalmon.studentfit.ui.theme.Mint
import com.realsalmon.studentfit.ui.theme.NumberStyle
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private const val STEP_GOAL = 8000

@Composable
fun WatchScreen(vm: WatchViewModel = viewModel()) {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(vm.repo.permissionContract()) { vm.refresh() }

    LazyColumn(
        Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("갤럭시 워치", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
                IconButton(onClick = vm::refresh) { Icon(Icons.Filled.Refresh, "새로고침") }
            }
        }

        when (val ui = vm.ui) {
            WatchUi.Loading -> item {
                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            }

            is WatchUi.Unavailable -> item {
                SoftCard {
                    Text("Health Connect가 필요해요", style = MaterialTheme.typography.titleMedium)
                    Text(
                        if (ui.needsUpdate) "Health Connect를 업데이트하면 워치 기록을 가져올 수 있어요."
                        else "이 폰에서는 Health Connect를 쓸 수 없어요. 갤럭시 S25에는 기본으로 들어 있어요.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (ui.needsUpdate) {
                        Button(onClick = { context.startActivity(vm.repo.installIntent()) }) { Text("업데이트하기") }
                    }
                }
            }

            WatchUi.NeedsPermission -> {
                item {
                    HeroCard {
                        Icon(Icons.Filled.Watch, null, tint = Color.White, modifier = Modifier.size(36.dp))
                        Text("워치 기록 연결하기", color = Color.White, style = MaterialTheme.typography.headlineSmall)
                        Text(
                            "갤럭시 워치 8의 걸음 수, 심박수, 운동 기록을 보여줘요. 기록은 폰 안에서만 써요.",
                            color = Color.White.copy(alpha = 0.9f),
                        )
                        Spacer(Modifier.height(6.dp))
                        Button(
                            onClick = { permissionLauncher.launch(vm.repo.permissions) },
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = Color.White, contentColor = MaterialTheme.colorScheme.primary,
                            ),
                        ) { Text("권한 허용하고 연결") }
                    }
                }
                item { SyncGuide(onOpenSamsungHealth = vm.repo.samsungHealthIntent()?.let { i -> { context.startActivity(i) } }) }
            }

            is WatchUi.Ready -> {
                val s = ui.summary
                item { StepsHero(s) }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Stat(Icons.Filled.Favorite, (s.latestHeart ?: s.heartAvg)?.toString() ?: "-", "현재 bpm", Coral, Modifier.weight(1f))
                        Stat(Icons.Filled.LocalFireDepartment, s.activeKcal.toInt().toString(), "활동 kcal", MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                    }
                }
                if (s.heartMin != null && s.heartMax != null) {
                    item {
                        SoftCard {
                            Text("오늘 심박수", style = MaterialTheme.typography.titleMedium)
                            Row {
                                Column(Modifier.weight(1f)) { Label("최저"); Text("${s.heartMin}", style = NumberStyle) }
                                Column(Modifier.weight(1f)) { Label("평균"); Text("${s.heartAvg ?: "-"}", style = NumberStyle) }
                                Column(Modifier.weight(1f)) { Label("최고"); Text("${s.heartMax}", style = NumberStyle) }
                            }
                        }
                    }
                }
                item { SectionHeader("이번 주 걸음 수") }
                item { WeekChart(s) }
                item { SectionHeader("최근 운동") }
                if (s.workouts.isEmpty()) {
                    item {
                        SoftCard {
                            Text("최근 7일 운동 기록이 없어요", style = MaterialTheme.typography.titleMedium)
                            Text("워치에서 운동을 시작하면 여기에 나와요.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    items(s.workouts) { w ->
                        val zone = ZoneId.systemDefault()
                        val t = w.start.atZone(zone)
                        SoftCard {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer),
                                    contentAlignment = Alignment.Center,
                                ) { Icon(Icons.AutoMirrored.Filled.DirectionsRun, null, tint = MaterialTheme.colorScheme.onSecondaryContainer) }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(w.title, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        t.format(DateTimeFormatter.ofPattern("M월 d일 (E) a h:mm", Locale.KOREAN)),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                Text("${w.minutes}분", style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }
                item { SyncGuide(onOpenSamsungHealth = vm.repo.samsungHealthIntent()?.let { i -> { context.startActivity(i) } }) }
            }

            is WatchUi.Error -> item {
                SoftCard {
                    Text("기록을 읽지 못했어요", style = MaterialTheme.typography.titleMedium)
                    Text(ui.message, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedButton(onClick = vm::refresh) { Text("다시 시도") }
                }
            }
        }
    }
}

@Composable
private fun Label(text: String) =
    Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

@Composable
private fun StepsHero(s: WatchSummary) {
    val progress = (s.stepsToday / STEP_GOAL.toFloat()).coerceIn(0f, 1f)
    HeroCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("오늘 걸음", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelLarge)
                Text("%,d".format(s.stepsToday), color = Color.White, style = MaterialTheme.typography.displaySmall)
                Text("목표 %,d보".format(STEP_GOAL), color = Color.White.copy(alpha = 0.8f))
            }
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(84.dp),
                    color = Mint,
                    trackColor = Color.White.copy(alpha = 0.2f),
                    strokeWidth = 9.dp,
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
                )
                Icon(Icons.AutoMirrored.Filled.DirectionsWalk, null, tint = Color.White, modifier = Modifier.size(30.dp))
            }
        }
    }
}

@Composable
private fun Stat(icon: ImageVector, value: String, label: String, tint: Color, modifier: Modifier) {
    SoftCard(modifier) {
        Box(Modifier.size(36.dp).clip(CircleShape).background(tint.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp))
        }
        Text(value, style = NumberStyle)
        Label(label)
    }
}

@Composable
private fun WeekChart(s: WatchSummary) {
    val max = (s.week.maxOfOrNull { it.steps } ?: 0L).coerceAtLeast(STEP_GOAL.toLong()).toFloat()
    val bar = MaterialTheme.colorScheme.primary
    val todayBar = MaterialTheme.colorScheme.tertiary
    val goalLine = MaterialTheme.colorScheme.outline
    SoftCard {
        Canvas(Modifier.fillMaxWidth().height(140.dp)) {
            val n = s.week.size
            val slot = size.width / n
            val w = slot * 0.5f
            val goalY = size.height * (1 - STEP_GOAL / max)
            drawLine(goalLine, Offset(0f, goalY), Offset(size.width, goalY), strokeWidth = 2f,
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(12f, 10f)))
            s.week.forEachIndexed { i, d ->
                val h = (d.steps / max * size.height).coerceAtLeast(6f)
                drawRoundRect(
                    if (i == n - 1) todayBar else bar,
                    topLeft = Offset(i * slot + (slot - w) / 2f, size.height - h),
                    size = Size(w, h),
                    cornerRadius = CornerRadius(w / 2f),
                )
            }
        }
        Row(Modifier.fillMaxWidth()) {
            s.week.forEach { d ->
                Text(
                    d.date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN),
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Text("점선은 하루 목표 %,d보".format(STEP_GOAL), style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp))
    }
}

@Composable
private fun SyncGuide(onOpenSamsungHealth: (() -> Unit)?) {
    SoftCard(color = MaterialTheme.colorScheme.surfaceContainer) {
        Text("기록이 안 보이나요?", style = MaterialTheme.typography.titleMedium)
        Text(
            "갤럭시 워치 기록은 삼성 헬스를 거쳐 들어와요.\n" +
                "1. 삼성 헬스 앱 → 오른쪽 위 ⋮ → 설정\n" +
                "2. Health Connect → 데이터 공유를 켜 주세요\n" +
                "3. 걸음 수, 심박수, 운동 항목을 허용해 주세요",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (onOpenSamsungHealth != null) {
            OutlinedButton(onClick = onOpenSamsungHealth) { Text("삼성 헬스 열기") }
        }
    }
}
