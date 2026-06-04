package com.depsoftware.notifhistory.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.depsoftware.notifhistory.data.entities.NotificationEvent
import com.depsoftware.notifhistory.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
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
