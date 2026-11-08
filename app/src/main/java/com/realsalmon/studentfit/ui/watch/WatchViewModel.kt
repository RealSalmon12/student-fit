package com.realsalmon.studentfit.ui.watch

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.realsalmon.studentfit.data.HealthRepository
import com.realsalmon.studentfit.data.HealthStatus
import com.realsalmon.studentfit.data.WatchSummary
import kotlinx.coroutines.launch

sealed interface WatchUi {
    data object Loading : WatchUi
    data class Unavailable(val needsUpdate: Boolean) : WatchUi
    data object NeedsPermission : WatchUi
    data class Ready(val summary: WatchSummary) : WatchUi
    data class Error(val message: String) : WatchUi
}

class WatchViewModel(app: Application) : AndroidViewModel(app) {

    val repo = HealthRepository(app)

    var ui by mutableStateOf<WatchUi>(WatchUi.Loading)
        private set

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            ui = WatchUi.Loading
            ui = when (val s = repo.status()) {
                HealthStatus.AVAILABLE -> runCatching {
                    if (repo.hasPermissions()) WatchUi.Ready(repo.readSummary()) else WatchUi.NeedsPermission
                }.getOrElse { WatchUi.Error(it.message ?: "기록을 읽지 못했어요") }
                else -> WatchUi.Unavailable(needsUpdate = s == HealthStatus.NEEDS_UPDATE)
            }
        }
    }
}
