package com.monohelper.ui.sync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monohelper.core.result.AppResult
import com.monohelper.domain.model.TaskInfo
import com.monohelper.domain.usecase.EnqueueSync
import com.monohelper.domain.usecase.PollTask
import com.monohelper.domain.usecase.SyncKind
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Lifecycle of a single sync run: enqueue, then poll until terminal. */
sealed interface SyncRunState {
    data object Idle : SyncRunState
    data class Enqueueing(val kind: SyncKind) : SyncRunState
    data class Polling(val kind: SyncKind, val task: TaskInfo?) : SyncRunState
    data class Finished(val kind: SyncKind, val task: TaskInfo) : SyncRunState
    data class Failed(val kind: SyncKind, val message: String) : SyncRunState
}

@HiltViewModel
class SyncViewModel @Inject constructor(
    private val enqueueSync: EnqueueSync,
    private val pollTask: PollTask,
) : ViewModel() {

    private val _state = MutableStateFlow<SyncRunState>(SyncRunState.Idle)
    val state: StateFlow<SyncRunState> = _state.asStateFlow()

    /** Starts a sync run. Ignored while another run is in flight (one run at a time). */
    fun start(kind: SyncKind) {
        val current = _state.value
        if (current is SyncRunState.Enqueueing || current is SyncRunState.Polling) return
        _state.value = SyncRunState.Enqueueing(kind)
        viewModelScope.launch {
            when (val enqueued = enqueueSync(kind)) {
                is AppResult.Failure -> _state.value = SyncRunState.Failed(kind, enqueued.message)
                is AppResult.Success -> {
                    _state.value = SyncRunState.Polling(kind, task = null)
                    pollTask(enqueued.value.taskId).collect { result ->
                        _state.value = when (result) {
                            is AppResult.Success ->
                                if (result.value.status.isTerminal) {
                                    SyncRunState.Finished(kind, result.value)
                                } else {
                                    SyncRunState.Polling(kind, result.value)
                                }
                            is AppResult.Failure -> SyncRunState.Failed(kind, result.message)
                        }
                    }
                }
            }
        }
    }

    /** Returns to [SyncRunState.Idle] after a finished or failed run. */
    fun reset() {
        _state.value = SyncRunState.Idle
    }
}
