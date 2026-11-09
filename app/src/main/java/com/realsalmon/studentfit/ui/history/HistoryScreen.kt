package com.realsalmon.studentfit.ui.history

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.realsalmon.studentfit.data.AppSettings
import com.realsalmon.studentfit.domain.exercise.ExerciseCatalog
import com.realsalmon.studentfit.reminder.Reminder
import com.realsalmon.studentfit.ui.components.BackTopBar
import com.realsalmon.studentfit.ui.components.SectionHeader
import com.realsalmon.studentfit.ui.components.SoftCard
import com.realsalmon.studentfit.ui.rememberRoutineLog
import com.realsalmon.studentfit.ui.theme.NumberStyle
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

private val WEEK = listOf("월", "화", "수", "목", "금", "토", "일")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(onBack: () -> Unit, onExercise: (String) -> Unit) {
    val context = LocalContext.current
    val log = rememberRoutineLog()
    val today = remember { LocalDate.now() }
    var month by remember { mutableStateOf(YearMonth.from(today)) }
    var selected by remember { mutableStateOf(today) }
    val counts = remember(month) { log.counts(month.atDay(1), month.atEndOfMonth()) }
    val streak = remember { log.streak() }
    val best = remember { log.bestStreak() }
    val total = remember { log.totalDone() }

    Scaffold(topBar = { BackTopBar("운동 기록", onBack) }, containerColor = MaterialTheme.colorScheme.background) { p ->
        Column(
            Modifier.fillMaxSize().padding(p).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            SoftCard {
                Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                    Stat("$streak", "일 연속", Modifier.weight(1f))
                    VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Stat("$best", "최장 연속", Modifier.weight(1f))
                    VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Stat("$total", "끝낸 운동", Modifier.weight(1f))
                }
            }

            SoftCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { month = month.minusMonths(1) }) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "이전 달") }
                    Text(
                        "${month.year}년 ${month.monthValue}월",
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = { month = month.plusMonths(1) }, enabled = month < YearMonth.from(today)) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "다음 달")
                    }
                }
                Row(Modifier.fillMaxWidth()) {
                    WEEK.forEach {
                        Text(it, Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                val lead = month.atDay(1).dayOfWeek.value - DayOfWeek.MONDAY.value
                val cells = List(lead) { null } + (1..month.lengthOfMonth()).map { month.atDay(it) }
                cells.chunked(7).forEach { week ->
                    Row(Modifier.fillMaxWidth()) {
                        week.forEach { d -> DayCell(d, counts[d] ?: 0, d == today, d == selected, Modifier.weight(1f)) { if (d != null) selected = it } }
                        repeat(7 - week.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("적게", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    listOf(1, 2, 3).forEach { Box(Modifier.size(12.dp).clip(CircleShape).background(level(it))) }
                    Text("많이", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            val ids = remember(selected) { log.doneOn(selected) }
            SectionHeader("${selected.monthValue}월 ${selected.dayOfMonth}일에 한 운동")
            if (ids.isEmpty()) {
                Text(
                    if (selected == today) "아직 없어요. 오늘의 루틴에서 하나 골라 시작해 보세요." else "이날은 운동 기록이 없어요.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                SoftCard {
                    ids.mapNotNull { ExerciseCatalog.find(it) }.forEach { ex ->
                        Row(
                            Modifier.fillMaxWidth().clickable { onExercise(ex.id) }.padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                            Spacer(Modifier.size(10.dp))
                            Text(ex.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                            Text(ex.dose, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            SectionHeader("매일 운동 알림")
            ReminderCard(context)
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun level(count: Int): Color {
    val c = MaterialTheme.colorScheme.primary
    return when {
        count <= 0 -> Color.Transparent
        count == 1 -> c.copy(alpha = 0.35f)
        count == 2 -> c.copy(alpha = 0.65f)
        else -> c
    }
}

@Composable
private fun DayCell(day: LocalDate?, count: Int, isToday: Boolean, isSelected: Boolean, modifier: Modifier, onClick: (LocalDate) -> Unit) {
    Box(modifier.aspectRatio(1f).padding(3.dp), contentAlignment = Alignment.Center) {
        if (day == null) return@Box
        val fill = level(count)
        Box(
            Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(fill)
                .then(
                    when {
                        isSelected -> Modifier.border(BorderStroke(2.dp, MaterialTheme.colorScheme.onSurface), CircleShape)
                        isToday -> Modifier.border(BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary), CircleShape)
                        else -> Modifier
                    },
                )
                .clickable { onClick(day) },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "${day.dayOfMonth}",
                style = MaterialTheme.typography.bodyMedium,
                color = if (count >= 2) Color.White else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun Stat(value: String, label: String, modifier: Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = NumberStyle)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderCard(context: android.content.Context) {
    val settings = remember { AppSettings(context) }
    var on by remember { mutableStateOf(settings.reminderOn) }
    var minute by remember { mutableStateOf(settings.reminderMinute) }
    var picking by remember { mutableStateOf(false) }
    var denied by remember { mutableStateOf(false) }

    fun apply(enable: Boolean) {
        on = enable
        settings.reminderOn = enable
        Reminder.sync(context)
    }

    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        denied = !granted
        apply(granted)
    }

    SoftCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("매일 ${timeText(minute)}에 알려줘요", style = MaterialTheme.typography.titleMedium)
                Text(
                    "그날 운동을 아직 안 했으면 알림으로 알려줘요. 몇 분 늦게 올 수 있어요.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(checked = on, onCheckedChange = { want ->
                if (want && Build.VERSION.SDK_INT >= 33 &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                ) {
                    permission.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    apply(want)
                }
            })
        }
        OutlinedButton(onClick = { picking = true }) { Text("시간 바꾸기") }
        if (denied) {
            Text(
                "알림 권한이 꺼져 있어요. 휴대폰 설정 > 애플리케이션 > 스튜던트 핏 > 알림에서 켜 주세요.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }

    if (picking) {
        val state = rememberTimePickerState(initialHour = minute / 60, initialMinute = minute % 60, is24Hour = false)
        AlertDialog(
            onDismissRequest = { picking = false },
            confirmButton = {
                TextButton(onClick = {
                    minute = state.hour * 60 + state.minute
                    settings.reminderMinute = minute
                    Reminder.sync(context)
                    picking = false
                }) { Text("저장") }
            },
            dismissButton = { TextButton(onClick = { picking = false }) { Text("취소") } },
            text = { TimePicker(state = state) },
        )
    }
}

private fun timeText(minute: Int): String {
    val h = minute / 60
    val m = minute % 60
    val ampm = if (h < 12) "오전" else "오후"
    val h12 = if (h % 12 == 0) 12 else h % 12
    return if (m == 0) "$ampm ${h12}시" else "$ampm ${h12}시 ${m}분"
}
