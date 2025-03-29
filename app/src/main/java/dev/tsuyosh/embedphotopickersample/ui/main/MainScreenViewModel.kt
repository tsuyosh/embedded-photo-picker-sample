package dev.tsuyosh.embedphotopickersample.ui.main

import android.net.Uri
import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainScreenViewModel(
    private val takeScreenShotUseCase: TakeScreenShotUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(MainScreenUiState())
    val uiState: StateFlow<MainScreenUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<MainScreenUiEvent>()
    val uiEvent: SharedFlow<MainScreenUiEvent?> = _uiEvent.asSharedFlow()

    fun takeScreenShot(targetView: View) {
        viewModelScope.launch {
            takeScreenShotUseCase.execute(targetView)
            _uiEvent.emit(MainScreenUiEvent.TakeScreenShot)
        }
    }

    fun sendPhotoMessage(photoUris: List<Uri>) {
        _uiState.update { current ->
            current.copy(
                messages = current.messages + MessageUiState(photoUris = photoUris)
            )
        }
    }
}

data class MainScreenUiState(
    val messages: List<MessageUiState> = emptyList(),
    val isPhotoPickerOpened: Boolean = false,
    val isPhotoPickerExpanded: Boolean = false
)

sealed interface MainScreenUiEvent {
    data object TakeScreenShot : MainScreenUiEvent
}

data class MessageUiState(
    val photoUris: List<Uri> = emptyList()
)