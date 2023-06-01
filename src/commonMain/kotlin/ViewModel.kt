import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
sealed interface ViewModelState {
    @Serializable data class Landing(val void: Unit = Unit) : ViewModelState
    @Serializable data class Setup(val void: Unit = Unit) : ViewModelState
    @Serializable data class Waiting(val void: Unit = Unit) : ViewModelState
    @Serializable data class UploadSuccess(val void: Unit = Unit) : ViewModelState
    @Serializable data class Uploading(val void: Unit = Unit) : ViewModelState
    @Serializable data class UploadFailure(val failure: ConversionResult.Failure) : ViewModelState
}

class ViewModel<T>(
    private val fileProvider: FileProvider<T>,
    private val fileConverter: FileConverter<T>,
    private val webNav: StateNav<ViewModelState>,
    initialState: ViewModelState
) {
    private lateinit var _viewModelScope: CoroutineScope
    var viewModelScope: CoroutineScope
        get() = _viewModelScope
        set(value) {
            _viewModelScope = value
            monitorNav()
            value.launch { fileConverter.initialize() }
        }

    private var navJob: Job? = null

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<ViewModelState> = _state

    private val _enableButton = MutableStateFlow(false)
    val enableButton: StateFlow<Boolean> = _enableButton

    private fun monitorNav() {
        navJob?.cancel()
        webNav.replace(ViewModelState.Waiting(), "HotClasps")
        navJob = viewModelScope.launch {
            webNav.onPop.collect {
                _state.value = it
            }
        }
    }

    fun pushNav(state: ViewModelState) {
        webNav.push(state, "state")
        _state.value = state
    }

    fun onFileInputChanged() {
        _enableButton.value = fileProvider.getFile() != null
    }

    fun reset() {
        _enableButton.value = false
        _state.value = ViewModelState.Waiting()
        webNav.back()
    }

    fun onUploadClicked() {
        val file = fileProvider.getFile() ?: return
        _state.value = ViewModelState.Uploading()
        viewModelScope.launch {
            val updatedState = when(val result = fileConverter.convert(file)) {
                is ConversionResult.Failure -> ViewModelState.UploadFailure(result)
                ConversionResult.Success -> ViewModelState.UploadSuccess()
            }
            pushNav(updatedState)
        }
    }
}