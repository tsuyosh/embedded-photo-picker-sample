package dev.tsuyosh.embedphotopickersample.ui.main

import android.net.Uri
import android.view.SurfaceView
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
import timber.log.Timber

class MainScreenViewModel(
    private val takeScreenShotUseCase: TakeScreenShotUseCase,
    private val saveImageUseCase: SaveImageUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MainScreenUiState())
    val uiState: StateFlow<MainScreenUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<MainScreenUiEvent>()
    val uiEvent: SharedFlow<MainScreenUiEvent?> = _uiEvent.asSharedFlow()

    fun takeScreenShot(targetView: SurfaceView) {
        viewModelScope.launch {
            takeScreenShotUseCase.execute(targetView)
            _uiEvent.emit(MainScreenUiEvent.TakeScreenShot)
        }
    }

    fun sendPhotoMessage(photoUris: List<Uri>, callback: () -> Unit) {
        Timber.d("sendPhotoMessage: photoUris=$photoUris")
        viewModelScope.launch {
            val savedImageUris = photoUris.map { uri ->
                saveImageUseCase.execute(uri)
            }
            Timber.d("sendPhotoMessage: savedImageUris=$savedImageUris")
            _uiState.update { current ->
                current.copy(
                    messages = current.messages + MessageUiState(photoUris = savedImageUris)
                )
            }
            callback.invoke()
        }
    }

    fun togglePhotoPicker() {
        _uiState.update { current ->
            current.copy(
                isPhotoPickerOpened = !current.isPhotoPickerOpened
            )
        }
    }

    fun togglePhotoPickerExpanded() {
        _uiState.update { current ->
            current.copy(
                isPhotoPickerExpanded = !current.isPhotoPickerExpanded
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