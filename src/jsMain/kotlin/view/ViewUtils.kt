package view

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.dom.Br
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

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

@Composable
fun Line(text: String) {
    Text("- $text")
    Br()
}

@Composable
fun MultBr(times: Int) {
    repeat(times) { Br() }
}