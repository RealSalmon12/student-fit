package com.realsalmon.studentfit.ui.camera

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.realsalmon.studentfit.data.ProfileRepository
import com.realsalmon.studentfit.domain.PostureIssue
import com.realsalmon.studentfit.domain.ProfileSource
import com.realsalmon.studentfit.domain.pose.BodyPoint
import com.realsalmon.studentfit.domain.pose.PostureAnalyzer
import com.realsalmon.studentfit.domain.pose.ScanResult
import com.realsalmon.studentfit.domain.pose.ScanView
import com.realsalmon.studentfit.ui.components.SoftCard
import com.realsalmon.studentfit.ui.theme.Mint
import kotlinx.coroutines.delay

private const val FRAMES_NEEDED = 30

private val overlayBones = listOf(
    BodyPoint.LEFT_SHOULDER to BodyPoint.RIGHT_SHOULDER,
    BodyPoint.LEFT_HIP to BodyPoint.RIGHT_HIP,
    BodyPoint.LEFT_SHOULDER to BodyPoint.LEFT_HIP,
    BodyPoint.RIGHT_SHOULDER to BodyPoint.RIGHT_HIP,
    BodyPoint.LEFT_HIP to BodyPoint.LEFT_KNEE,
    BodyPoint.LEFT_KNEE to BodyPoint.LEFT_ANKLE,
    BodyPoint.RIGHT_HIP to BodyPoint.RIGHT_KNEE,
    BodyPoint.RIGHT_KNEE to BodyPoint.RIGHT_ANKLE,
    BodyPoint.LEFT_EAR to BodyPoint.LEFT_SHOULDER,
    BodyPoint.RIGHT_EAR to BodyPoint.RIGHT_SHOULDER,
)

private sealed interface Phase {
    data object Idle : Phase
    data class Countdown(val left: Int) : Phase
    data object Collecting : Phase
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScanScreen(onBack: () -> Unit, onSaved: (needsInput: Boolean) -> Unit) {
    val context = LocalContext.current
    var granted by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted = it }
    LaunchedEffect(Unit) { if (!granted) launcher.launch(Manifest.permission.CAMERA) }

    var view by remember { mutableStateOf(ScanView.FRONT) }
    var front by remember { mutableStateOf(true) }
    var latest by remember { mutableStateOf<PoseFrame?>(null) }
    val collected = remember { mutableStateListOf<PoseFrame>() }
    var phase by remember { mutableStateOf<Phase>(Phase.Idle) }
    var result by remember { mutableStateOf<ScanResult?>(null) }
    var countdownKey by remember { mutableIntStateOf(0) }

    LaunchedEffect(countdownKey) {
        if (countdownKey == 0) return@LaunchedEffect
        for (n in 3 downTo 1) { phase = Phase.Countdown(n); delay(1000) }
        collected.clear()
        phase = Phase.Collecting
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        if (granted) {
            PoseCamera(
                frontCamera = front,
                onFrame = { f ->
                    latest = f
                    if (phase == Phase.Collecting) {
                        collected += f
                        if (collected.size >= FRAMES_NEEDED) {
                            val avg = PostureAnalyzer.average(collected.map { it.sample })
                            result = PostureAnalyzer.analyze(view, avg)
                            phase = Phase.Idle
                        }
                    }
                },
                modifier = Modifier.fillMaxSize(),
            )
            latest?.let { PoseOverlay(it, mirror = front, Modifier.fillMaxSize()) }
        } else {
            Column(
                Modifier.align(Alignment.Center).padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(Icons.Filled.CameraAlt, null, tint = Color.White, modifier = Modifier.size(48.dp))
                Text("자세를 분석하려면 카메라 권한이 필요해요", color = Color.White, textAlign = TextAlign.Center)
                Text("영상은 저장하지 않고 폰 안에서만 분석해요.", color = Color.White.copy(alpha = 0.7f), textAlign = TextAlign.Center)
                Button(onClick = { launcher.launch(Manifest.permission.CAMERA) }) { Text("권한 허용") }
            }
        }

        // 상단 바
        Row(
            Modifier.fillMaxWidth().statusBarsPadding().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FilledTonalIconButton(onClick = onBack, colors = glassButton()) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "뒤로")
            }
            Spacer(Modifier.weight(1f))
            SingleChoiceSegmentedButtonRow(Modifier.width(180.dp)) {
                ScanView.entries.forEachIndexed { i, v ->
                    SegmentedButton(
                        selected = view == v,
                        onClick = { view = v },
                        shape = SegmentedButtonDefaults.itemShape(i, ScanView.entries.size),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = MaterialTheme.colorScheme.primary,
                            activeContentColor = Color.White,
                            inactiveContainerColor = Color.Black.copy(alpha = 0.4f),
                            inactiveContentColor = Color.White,
                        ),
                    ) { Text(v.label) }
                }
            }
            Spacer(Modifier.weight(1f))
            FilledTonalIconButton(onClick = { front = !front }, colors = glassButton()) {
                Icon(Icons.Filled.Cameraswitch, "카메라 전환")
            }
        }

        // 카운트다운
        (phase as? Phase.Countdown)?.let {
            Text(
                "${it.left}",
                color = Color.White,
                fontSize = 120.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.align(Alignment.Center),
            )
        }

        // 하단 안내 + 버튼
        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(16.dp)
                .clip(MaterialTheme.shapes.large)
                .background(Color.Black.copy(alpha = 0.55f))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            val visible = latest?.sample?.count { it.value.confidence >= PostureAnalyzer.MIN_CONFIDENCE } ?: 0
            Text(view.guide, color = Color.White)
            Text(
                when {
                    phase == Phase.Collecting -> "측정 중이에요. 움직이지 말고 그대로 서 있어 주세요."
                    visible < 9 -> "몸 전체(머리~발목)가 화면에 들어오게 2~3m 떨어져 주세요. (${visible}/11)"
                    else -> "좋아요! 몸이 잘 보여요."
                },
                color = if (visible >= 9) Mint else Color.White.copy(alpha = 0.75f),
                style = MaterialTheme.typography.bodySmall,
            )
            if (phase == Phase.Collecting) {
                LinearProgressIndicator(
                    progress = { collected.size / FRAMES_NEEDED.toFloat() },
                    modifier = Modifier.fillMaxWidth(),
                    strokeCap = StrokeCap.Round,
                )
            }
            Button(
                onClick = { countdownKey++ },
                enabled = granted && phase == Phase.Idle,
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) { Text("3초 후 측정 시작", style = MaterialTheme.typography.titleMedium) }
        }
    }

    result?.let { r ->
        ResultSheet(
            result = r,
            onDismiss = { result = null },
            onSave = { checked ->
                val repo = ProfileRepository(context)
                val measured = r.findings.map { it.issue }.toSet()
                val profile = repo.load()
                if (profile != null) {
                    val merged = profile.postureIssues - measured + checked
                    repo.save(profile.copy(postureIssues = merged, source = ProfileSource.CAMERA))
                    result = null
                    onSaved(false)
                } else {
                    repo.savePendingPosture(checked)
                    result = null
                    onSaved(true)
                }
            },
        )
    }
}

@Composable
private fun glassButton() = IconButtonDefaults.filledTonalIconButtonColors(
    containerColor = Color.Black.copy(alpha = 0.4f),
    contentColor = Color.White,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ResultSheet(result: ScanResult, onDismiss: () -> Unit, onSave: (Set<PostureIssue>) -> Unit) {
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val checked = remember(result) { mutableStateListOf<PostureIssue>().apply { addAll(result.detected.map { it.issue }) } }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = state) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("${result.view.label} 분석 결과", style = MaterialTheme.typography.headlineSmall)
            Text(
                if (result.detected.isEmpty()) "눈에 띄는 자세 문제가 없어요! 👍" else "${result.detected.size}가지가 의심돼요. 맞는 것만 체크해 저장하세요.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (result.findings.isEmpty()) {
                Text("몸이 잘 보이지 않아 분석하지 못했어요. 몸 전체가 나오게 다시 찍어 주세요.")
            }
            result.findings.forEach { f ->
                SoftCard(color = if (f.detected) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceContainer) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = f.issue in checked, onCheckedChange = { on ->
                            if (on) checked += f.issue else checked -= f.issue
                        })
                        Column(Modifier.weight(1f)) {
                            Text(
                                f.issue.label + if (f.detected) " 의심" else " 정상 범위",
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(f.measurement, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
            if (result.missing.isNotEmpty()) {
                Text("잘 안 보인 부위: ${result.missing.joinToString()}", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("카메라 분석은 참고용이에요. 통증이 있거나 걱정되면 병원에서 확인하세요.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("다시 찍기") }
                Button(onClick = { onSave(checked.toSet()) }, modifier = Modifier.weight(2f).height(52.dp)) { Text("저장하고 운동 추천받기") }
            }
        }
    }
}

/** 인식된 관절을 미리보기 위에 그려요. */
@Composable
private fun PoseOverlay(frame: PoseFrame, mirror: Boolean, modifier: Modifier) {
    val accent = Mint
    Canvas(modifier) {
        val scale = maxOf(size.width / frame.imageWidth, size.height / frame.imageHeight)
        val dx = (size.width - frame.imageWidth * scale) / 2f
        val dy = (size.height - frame.imageHeight * scale) / 2f
        fun map(bp: BodyPoint): Offset? {
            val p = frame.sample[bp] ?: return null
            if (p.confidence < PostureAnalyzer.MIN_CONFIDENCE) return null
            val x = p.x * scale + dx
            return Offset(if (mirror) size.width - x else x, p.y * scale + dy)
        }
        for ((a, b) in overlayBones) {
            val pa = map(a) ?: continue
            val pb = map(b) ?: continue
            drawLine(accent, pa, pb, strokeWidth = 6.dp.toPx(), cap = StrokeCap.Round)
        }
        for (bp in BodyPoint.entries) {
            val p = map(bp) ?: continue
            drawCircle(Color.White, 7.dp.toPx(), p)
            drawCircle(accent, 4.dp.toPx(), p)
        }
    }
}
