package com.realsalmon.studentfit.ui.bodytype

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.realsalmon.studentfit.domain.Gender
import com.realsalmon.studentfit.domain.PostureIssue

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BodyTypeInputScreen(
    onBack: () -> Unit,
    vm: BodyTypeInputViewModel = viewModel(),
) {
    val s = vm.state

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("체형 직접 입력") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberField(s.height, vm::onHeight, "키 (cm)", Modifier.weight(1f))
                NumberField(s.weight, vm::onWeight, "몸무게 (kg)", Modifier.weight(1f))
            }
            NumberField(s.age, vm::onAge, "나이", Modifier.fillMaxWidth())

            Text("성별", style = MaterialTheme.typography.titleSmall)
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                Gender.entries.forEachIndexed { i, g ->
                    SegmentedButton(
                        selected = s.gender == g,
                        onClick = { vm.onGender(g) },
                        shape = SegmentedButtonDefaults.itemShape(i, Gender.entries.size),
                    ) { Text(g.label) }
                }
            }

            Text("자세 고민 (해당하는 것 모두 선택)", style = MaterialTheme.typography.titleSmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PostureIssue.entries.forEach { issue ->
                    FilterChip(
                        selected = issue in s.postureIssues,
                        onClick = { vm.togglePosture(issue) },
                        label = { Text(issue.label) },
                    )
                }
            }
            s.postureIssues.forEach { issue ->
                Text("· ${issue.label}: ${issue.description}", style = MaterialTheme.typography.bodySmall)
            }

            s.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Button(onClick = { vm.save() }, modifier = Modifier.fillMaxWidth()) {
                Text("저장하고 체형 분류하기")
            }

            s.result?.let { r ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("분류 결과", style = MaterialTheme.typography.titleMedium)
                        Text(r.summary)
                        Text(
                            "만 18세 이하는 성장 단계에 따라 기준이 달라서 참고용이에요.",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NumberField(value: String, onChange: (String) -> Unit, label: String, modifier: Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier,
    )
}
