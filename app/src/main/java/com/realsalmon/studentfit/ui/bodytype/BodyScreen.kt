package com.realsalmon.studentfit.ui.bodytype

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.realsalmon.studentfit.domain.BodyTypeResult
import com.realsalmon.studentfit.ui.components.ActionRow
import com.realsalmon.studentfit.ui.components.HeroCard
import com.realsalmon.studentfit.ui.components.SectionHeader
import com.realsalmon.studentfit.ui.components.SoftCard
import com.realsalmon.studentfit.ui.navigation.AppNavigator
import com.realsalmon.studentfit.ui.rememberProfile
import com.realsalmon.studentfit.ui.theme.Coral
import com.realsalmon.studentfit.ui.theme.Mint
import com.realsalmon.studentfit.ui.theme.Sky
import com.realsalmon.studentfit.ui.theme.Court
import com.realsalmon.studentfit.ui.theme.Plumb
import com.realsalmon.studentfit.domain.AlignmentDiagram
import com.realsalmon.studentfit.ui.components.AlignmentFigure
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment

@Composable
fun BodyScreen(go: AppNavigator) {
    val (profile, result) = rememberProfile()

    LazyColumn(
        Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item { Text("체형 분석", style = MaterialTheme.typography.headlineLarge) }

        if (result != null && profile != null) {
            item {
                HeroCard {
                    Text("${profile.heightCm.toInt()}cm, ${profile.weightKg}kg, ${profile.age}세", color = Color.White.copy(alpha = 0.7f))
                    Text(result.typeTitle, color = Color.White, style = MaterialTheme.typography.displaySmall)
                    Text("BMI ${result.bmiText} (${result.bmiCategory.label})", color = Color.White)
                    BmiGauge(result, Modifier.fillMaxWidth().height(28.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        listOf("저체중", "정상", "과체중", "비만").forEach {
                            Text(it, color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        item {
            val issues = result?.postureIssues ?: emptySet()
            val side = remember(issues) { AlignmentDiagram.side(issues) }
            val front = remember(issues) { AlignmentDiagram.front(issues) }
            SoftCard {
                Text("정렬선 진단", style = MaterialTheme.typography.titleLarge)
                Text(
                    if (issues.isEmpty()) "귀·어깨·골반·무릎·발목이 노란 선 위에 있으면 바른 자세예요."
                    else "빨간 점이 노란 정렬선에서 벗어난 곳이에요.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(Modifier.fillMaxWidth()) {
                    listOf("옆에서" to side, "앞에서" to front).forEach { (label, fig) ->
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            AlignmentFigure(
                                fig,
                                Modifier.fillMaxWidth().height(260.dp),
                                bodyColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                dotColor = MaterialTheme.colorScheme.onSurface,
                                alertColor = Coral,
                                showLabels = false,
                            )
                            Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        item { SectionHeader("분석하기") }
        item {
            ActionRow(Icons.Filled.CameraAlt, "카메라로 자세 분석", "정면·측면 사진으로 거북목, 오리엉덩이, O/X다리를 찾아요") { go.camera() }
        }
        item {
            ActionRow(
                Icons.Filled.EditNote,
                if (profile == null) "직접 입력하기" else "정보 수정하기",
                "키, 몸무게, 자세 고민, 허벅지 살 같은 몸매 고민을 입력해요",
                container = MaterialTheme.colorScheme.secondaryContainer,
                content = MaterialTheme.colorScheme.onSecondaryContainer,
            ) { go.bodyInput() }
        }

        if (result != null) {
            val posture = result.postureIssues.toList()
            val concerns = result.concerns.toList()
            if (posture.isNotEmpty()) {
                item { SectionHeader("자세 고민") }
                items(posture) { InfoRow(it.label, it.description, Coral) }
            }
            if (concerns.isNotEmpty()) {
                item { SectionHeader("몸매·체력 고민") }
                items(concerns) { InfoRow(it.label, it.description, Court) }
            }
            if (posture.isEmpty() && concerns.isEmpty()) {
                item {
                    SoftCard {
                        Text("체크한 고민이 없어요", style = MaterialTheme.typography.titleMedium)
                        Text("기본 자세 루틴을 추천해 드릴게요. 카메라 분석으로 숨은 자세 문제를 찾아보세요.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            item {
                Text(
                    "만 18세 이하는 성장 단계에 따라 BMI 기준이 달라서 참고용이에요. 통증이 있으면 운동 전에 전문가와 상담하세요.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun InfoRow(title: String, description: String, accent: Color) {
    SoftCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(4.dp).height(40.dp).background(accent))
            Column(Modifier.padding(start = 12.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/** BMI 15~30 구간에 현재 위치를 점으로 보여줘요. */
@Composable
private fun BmiGauge(result: BodyTypeResult, modifier: Modifier) {
    Canvas(modifier) {
        val min = 15f; val max = 30f
        val bands = listOf(15f to 18.5f, 18.5f to 23f, 23f to 25f, 25f to 30f)
        val colors = listOf(Sky, Mint, Color(0xFFFFC857), Coral)
        val barH = 10.dp.toPx()
        val y = size.height / 2f - barH / 2f
        bands.forEachIndexed { i, (a, b) ->
            val x0 = (a - min) / (max - min) * size.width
            val x1 = (b - min) / (max - min) * size.width
            drawRoundRect(colors[i], Offset(x0 + 2f, y), Size(x1 - x0 - 4f, barH), CornerRadius(barH / 2))
        }
        val x = ((result.bmi.coerceIn(min, max) - min) / (max - min)) * size.width
        drawCircle(Color.White, radius = 11.dp.toPx(), center = Offset(x, size.height / 2f))
        drawCircle(Plumb, radius = 6.dp.toPx(), center = Offset(x, size.height / 2f))
    }
}
