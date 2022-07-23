package view

import UPLOAD_FORM_ID
import ViewModel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.dom.*
import org.w3c.files.Blob

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
    viewModel: ViewModel<Blob>
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