import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.browser.document
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.renderComposable

private const val UPLOAD_FORM_ID = "upload_form"

private class DefaultFileProvider(private val formId: String) : FileProvider {
    override fun getFile(): FileUpload? {
        val dynamicForm = document.getElementById(formId)?.asDynamic() ?: return null
        val file = dynamicForm.files[0] ?: return null
        return object : FileUpload {
            override val name: String
                get() = file.name as? String ?: "[Unknown File]"
            override val data: dynamic
                get() = file
        }
    }
}

private object DefaultFileUploader : FileUploader {
    override suspend fun upload(fileUpload: FileUpload) = uploadFileForProcessing(fileUpload)
}

fun main() {
    val fileProvider = DefaultFileProvider(UPLOAD_FORM_ID)
    val viewModel = ViewModel(fileProvider, DefaultFileUploader)
    renderComposable(rootElementId = "root") {
        viewModel.viewModelScope = rememberCoroutineScope()
        val state by viewModel.state.collectAsState()
        val resetCallback = { viewModel.reset() }
        return@renderComposable when(@Suppress("UnnecessaryVariable") val delegateState = state) {
            is ViewModelState.UploadFailure -> MessageWithButton(
                message = "Conversion failed!",
                subtitle = delegateState.failure.message,
                buttonText = "Try Again",
                onButtonClick = resetCallback
            )
            ViewModelState.UploadSuccess -> MessageWithButton(
                message = "Conversion success - .$HTCLP_SUFFIX file downloaded!",
                buttonText = "Upload Another",
                onButtonClick = resetCallback
            )
            is ViewModelState.Uploading -> MessageWithButton(message = "Uploading...")
            ViewModelState.Waiting -> WaitingScreen(viewModel)
        }
    }
}

@Composable
fun MessageWithButton(
    message: String,
    subtitle: String? = null,
    buttonText: String? = null,
    onButtonClick: () -> Unit = {}
) {
    Div {
        H2 {
            Text(message)
        }
    }
    subtitle?.let {
        Div { Text(it) }
        Br()
    }
    Br()
    buttonText?.let {
        HcButton(it, onButtonClick = onButtonClick)
    }
}

@Composable
fun WaitingScreen(
    viewModel: ViewModel
) {
    Div { H2 { Text("Select Audio File") } }
    Div {
        Form(action = null, {
            method(FormMethod.Dialog)
        }) {
            Input(InputType.File) {
                id(UPLOAD_FORM_ID)
                formEncType(InputFormEncType.MultipartFormData)
                onChange { viewModel.onFileInputChanged() }
            }
        }
    }
    val enableUploadButton by viewModel.enableButton.collectAsState()
    HcButton("Upload File", enableUploadButton) {
        viewModel.onUploadClicked()
    }
}

@Composable
fun HcButton(
    message: String,
    enable: Boolean = true,
    onButtonClick: () -> Unit
) {
    Div {
        Button(attrs = {
            if (!enable) disabled()
            onClick { onButtonClick() }
        }) {
            Text(message)
        }
    }
}