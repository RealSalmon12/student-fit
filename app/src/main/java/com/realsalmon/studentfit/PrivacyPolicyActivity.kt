package com.realsalmon.studentfit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.realsalmon.studentfit.ui.components.BackTopBar
import com.realsalmon.studentfit.ui.theme.StudentFitTheme

/** Health Connect 권한 화면에서 여는 개인정보 안내. */
class PrivacyPolicyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StudentFitTheme {
                Scaffold(topBar = { BackTopBar("개인정보 안내") { finish() } }) { padding ->
                    Column(
                        Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text("스튜던트 핏이 읽는 정보", style = MaterialTheme.typography.titleLarge)
                        Text("걸음 수, 심박수, 운동 기록, 활동 칼로리를 Health Connect에서 읽어와 워치 화면에 보여줘요.")
                        Text("어디에 쓰나요", style = MaterialTheme.typography.titleMedium)
                        Text("오늘의 활동량과 운동 중 심박수를 보여주고 운동 습관을 확인하는 데만 써요.")
                        Text("어디에 저장되나요", style = MaterialTheme.typography.titleMedium)
                        Text("모든 정보는 이 폰 안에서만 쓰고, 서버나 다른 사람에게 보내지 않아요. 카메라 영상도 저장하지 않고 폰 안에서 바로 분석해요.")
                        Text("권한 끄기", style = MaterialTheme.typography.titleMedium)
                        Text("설정 → 보안 및 개인정보 보호 → 개인정보 보호 → Health Connect 에서 언제든 끌 수 있어요.")
                    }
                }
            }
        }
    }
}
