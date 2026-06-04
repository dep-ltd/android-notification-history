package com.notificationhistory.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notificationhistory.data.entities.NotificationEvent
import com.notificationhistory.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    repository: NotificationRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val notificationIdFlow = MutableStateFlow(savedStateHandle.get<Long>("id"))

    val notification: StateFlow<NotificationEvent?> = notificationIdFlow
        .filterNotNull()
        .flatMapLatest { repository.observeNotification(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun bindNotificationId(id: Long) {
        if (notificationIdFlow.value != id) {
            notificationIdFlow.value = id
        }
    }
}
