import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import dependencies.FFmpeg
import dependencies.FFmpegRoot
import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.compose.web.css.Style
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.url.URLSearchParams
import org.w3c.files.Blob
import style.AppStylesheet
import style.Container
import view.Landing
import view.MessageWithButton
import view.SetupView
import view.WaitingScreen

const val UPLOAD_FORM_ID = "upload_form"

private class DefaultFileProvider(private val formId: String) : FileProvider<Blob> {
    override fun getFile(): FileUpload<Blob>? {
        val dynamicForm = document.getElementById(formId)?.asDynamic() ?: return null
        val file = dynamicForm.files[0] ?: return null
        return object : FileUpload<Blob> {
            override val name: String
                get() = file.name as? String ?: "[Unknown File]"
            override val data: dynamic
                get() = file
        }
    }
}

fun main() {
    val params = URLSearchParams(window.location.search)
    val startPageState = params.get(START_PAGE_PARAM)
        .let { StartPage.from(it) }
        .let {
            when (it) {
                StartPage.DEFAULT -> ViewModelState.Landing()
                StartPage.UPLOAD -> ViewModelState.Waiting()
                StartPage.SETUP -> ViewModelState.Setup()
            }
        }
    val fileProvider = DefaultFileProvider(UPLOAD_FORM_ID)
    val webNav = DefaultWebNav(ViewModelState.serializer(), ViewModelState.serializer())
    val viewModel = ViewModel(fileProvider, BrowserFileConverter, webNav, startPageState)
    renderComposable(rootElementId = "root") {
        viewModel.viewModelScope = rememberCoroutineScope()
        Style(AppStylesheet)
        Container {
            Root(viewModel)
        }
    }
}

@Composable
fun Root(viewModel: ViewModel<Blob>) {
    val state by viewModel.state.collectAsState()
    val resetCallback = { viewModel.reset() }
    return when (@Suppress("UnnecessaryVariable") val delegateState = state) {
        is ViewModelState.UploadFailure -> MessageWithButton(
            message = "Conversion failed!",
            subtitle = delegateState.failure.message,
            buttonText = "Try Again",
            onButtonClick = resetCallback
        )
        is ViewModelState.UploadSuccess -> MessageWithButton(
            message = "Conversion success - .$HTCLP_SUFFIX file downloaded!",
            buttonText = "Upload Another",
            onButtonClick = resetCallback
        )
        is ViewModelState.Uploading -> MessageWithButton(message = "Processing...")
        is ViewModelState.Waiting -> WaitingScreen(viewModel)
        is ViewModelState.Landing -> Landing()
        is ViewModelState.Setup -> SetupView()
    }
}