package com.realsalmon.studentfit.ui.bodytype

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.realsalmon.studentfit.domain.BodyConcern
import com.realsalmon.studentfit.domain.Gender
import com.realsalmon.studentfit.domain.PostureIssue
import com.realsalmon.studentfit.ui.components.BackTopBar
import com.realsalmon.studentfit.ui.components.SoftCard

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BodyTypeInputScreen(
    onBack: () -> Unit,
    vm: BodyTypeInputViewModel = viewModel(),
) {
    val s = vm.state

    Scaffold(
        topBar = { BackTopBar("내 체형 입력", onBack) },
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Column(Modifier.navigationBarsPadding().imePadding().padding(horizontal = 20.dp, vertical = 12.dp)) {
                s.error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 8.dp)) }
                Button(
                    onClick = { if (vm.save()) onBack() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                ) { Text("저장하고 맞춤 운동 보기", style = MaterialTheme.typography.titleMedium) }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            SoftCard {
                Text("기본 정보", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberField(s.height, vm::onHeight, "키 (cm)", Modifier.weight(1f))
                    NumberField(s.weight, vm::onWeight, "몸무게 (kg)", Modifier.weight(1f))
                    NumberField(s.age, vm::onAge, "나이", Modifier.weight(0.7f))
                }
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    Gender.entries.forEachIndexed { i, g ->
                        SegmentedButton(
                            selected = s.gender == g,
                            onClick = { vm.onGender(g) },
                            shape = SegmentedButtonDefaults.itemShape(i, Gender.entries.size),
                        ) { Text(g.label) }
                    }
                }
            }

            SoftCard {
                Text("자세 고민", style = MaterialTheme.typography.titleMedium)
                Text("해당하는 것을 모두 골라 주세요", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PostureIssue.entries.forEach { issue ->
                        Chip(issue.label, issue in s.postureIssues) { vm.togglePosture(issue) }
                    }
                }
                s.postureIssues.forEach { issue ->
                    Text("· ${issue.label}: ${issue.description}", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            SoftCard {
                Text("몸매·체력 고민", style = MaterialTheme.typography.titleMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BodyConcern.entries.forEach { c ->
                        Chip(c.label, c in s.concerns) { vm.toggleConcern(c) }
                    }
                }
                s.concerns.forEach { c ->
                    Text("· ${c.label}: ${c.description}", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun Chip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = if (selected) { { Icon(Icons.Filled.Check, null) } } else null,
        shape = MaterialTheme.shapes.small,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
    )
}

@Composable
private fun NumberField(value: String, onChange: (String) -> Unit, label: String, modifier: Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        shape = MaterialTheme.shapes.small,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier,
    )
}
