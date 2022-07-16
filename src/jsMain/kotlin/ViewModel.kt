import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface ViewModelState {
    object Waiting : ViewModelState
    object UploadSuccess : ViewModelState
    data class Uploading(val fileUpload: FileUpload) : ViewModelState
    data class UploadFailure(val failure: UploadResult.Failure) : ViewModelState
}

class ViewModel(
    private val fileProvider: FileProvider,
    private val fileUploader: FileUploader,

) {
    lateinit var viewModelScope: CoroutineScope

    private val _state = MutableStateFlow<ViewModelState>(ViewModelState.Waiting)
    val state: StateFlow<ViewModelState> = _state

    private val _enableButton = MutableStateFlow(false)
    val enableButton: StateFlow<Boolean> = _enableButton

    fun onFileInputChanged() {
        val file = fileProvider.getFile()
        if (file != null) {
            println("ready to upload")
            _enableButton.value = true
        } else {
            println("no file selected")
            _enableButton.value = false
        }
    }

    fun reset() {
        _enableButton.value = false
        _state.value = ViewModelState.Waiting
    }

    fun onUploadClicked() {
        println("upload clicked")
        val file = fileProvider.getFile() ?: return
        println("uploading file: ${file.name}")
        _state.value = ViewModelState.Uploading(file)
        viewModelScope.launch {
            val result = fileUploader.upload(file)
            println("upload result: $result")
            val updatedState = when(result) {
                is UploadResult.Failure -> ViewModelState.UploadFailure(result)
                UploadResult.Success -> ViewModelState.UploadSuccess
            }
            _state.value = updatedState
        }
    }
}