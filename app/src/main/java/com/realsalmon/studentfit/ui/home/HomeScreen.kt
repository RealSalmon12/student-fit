package com.realsalmon.studentfit.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.realsalmon.studentfit.data.ProfileRepository
import com.realsalmon.studentfit.domain.BodyTypeClassifier
import com.realsalmon.studentfit.ui.navigation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onNavigate: (String) -> Unit) {
    val context = LocalContext.current
    // 입력 화면에서 돌아올 때마다 홈이 다시 그려지면서 새 값을 읽어요.
    val profile = remember { ProfileRepository(context) }.load()
    val summary = profile?.let { BodyTypeClassifier.classify(it).summary }

    Scaffold(topBar = { TopAppBar(title = { Text("스튜던트 핏") }) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = summary?.let { "내 체형: $it" } ?: "먼저 내 체형을 입력해 주세요",
                style = MaterialTheme.typography.titleMedium,
            )
            MenuCard(Icons.Filled.Accessibility, "체형 직접 입력", "키, 몸무게, 자세 고민을 입력해요") {
                onNavigate(Routes.BODY_INPUT)
            }
            MenuCard(Icons.Filled.CameraAlt, "카메라로 체형 인식", "준비 중") {
                onNavigate(Routes.CAMERA_SCAN)
            }
            MenuCard(Icons.Filled.FitnessCenter, "교정 운동 (3D 예시)", "준비 중") {
                onNavigate(Routes.EXERCISES)
            }
            MenuCard(Icons.Filled.Watch, "갤럭시 워치 연동", "준비 중") {
                onNavigate(Routes.WATCH)
            }
        }
    }
}

@Composable
private fun MenuCard(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null)
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
