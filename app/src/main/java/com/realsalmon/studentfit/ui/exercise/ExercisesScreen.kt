package com.realsalmon.studentfit.ui.exercise

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.realsalmon.studentfit.domain.exercise.Exercise
import com.realsalmon.studentfit.domain.exercise.ExerciseCatalog
import com.realsalmon.studentfit.domain.exercise.ExerciseCategory
import com.realsalmon.studentfit.domain.exercise.RoutineRecommender
import com.realsalmon.studentfit.ui.components.Pill
import com.realsalmon.studentfit.ui.components.SoftCard
import com.realsalmon.studentfit.ui.navigation.AppNavigator
import com.realsalmon.studentfit.ui.rememberProfile
import com.realsalmon.studentfit.ui.rememberRoutineLog

@Composable
fun skeletonColors() = SkeletonColors(
    shirt = MaterialTheme.colorScheme.primary,
    floor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
)

@Composable
fun ExercisesScreen(go: AppNavigator) {
    val (_, result) = rememberProfile()
    val log = rememberRoutineLog()
    var done by remember { mutableStateOf(log.doneOn()) }
    val recs = remember(result) { RoutineRecommender.recommend(result) }
    var mine by rememberSaveable { mutableStateOf(true) }
    var category by rememberSaveable { mutableStateOf<ExerciseCategory?>(null) }

    val list: List<Pair<Exercise, List<String>>> =
        if (mine) recs.map { it.exercise to it.reasons }
        else ExerciseCatalog.all.filter { category == null || it.category == category }.map { it to it.targetLabels }

    LazyColumn(
        Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Text("교정 운동", style = MaterialTheme.typography.headlineMedium) }
        item {
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                SegmentedButton(mine, { mine = true }, SegmentedButtonDefaults.itemShape(0, 2)) { Text("내 맞춤 루틴") }
                SegmentedButton(!mine, { mine = false }, SegmentedButtonDefaults.itemShape(1, 2)) { Text("전체 운동") }
            }
        }
        if (!mine) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { FilterChip(category == null, { category = null }, { Text("전체") }) }
                    items(ExerciseCategory.entries) { c ->
                        FilterChip(category == c, { category = c }, { Text(c.label) })
                    }
                }
            }
        } else {
            item {
                Text(
                    if (result == null) "체형을 입력하면 나에게 맞는 운동으로 바뀌어요." else "${result.typeTitle}에 맞춘 오늘의 운동이에요.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        items(list, key = { it.first.id }) { (ex, tags) ->
            val isDone = ex.id in done
            ExerciseCard(ex, tags, isDone, onClick = { go.exercise(ex.id) }, onToggle = {
                log.setDone(ex.id, !isDone); done = log.doneOn()
            })
        }
    }
}

@Composable
private fun ExerciseCard(ex: Exercise, tags: List<String>, done: Boolean, onClick: () -> Unit, onToggle: () -> Unit) {
    SoftCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(84.dp).clip(MaterialTheme.shapes.medium).background(MaterialTheme.colorScheme.surfaceContainer),
            ) {
                SkeletonViewer(
                    ex.motion, skeletonColors(), Modifier.fillMaxSize(),
                    view = ex.view, focusUpper = ex.focusUpper,
                    playing = false, interactive = false, time = ex.thumbnailTime,
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(ex.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${ex.category.label} · ${ex.level.label} · ${ex.dose}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    tags.take(2).forEach {
                        Pill(it, MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }
            }
            IconButton(onClick = onToggle) {
                Icon(
                    if (done) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                    contentDescription = if (done) "완료 취소" else "완료",
                    tint = if (done) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                )
            }
        }
    }
}
