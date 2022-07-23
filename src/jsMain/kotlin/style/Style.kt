package style

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div

object AppStylesheet : StyleSheet() {
    val container by style { // container is a class
        display(DisplayStyle.Grid)
        padding(20.px)

        // custom property (or not supported out of a box)
        property("font-family", "Open Sans, Helvetica, sans-serif")
    }
}

@Composable
fun Container(content: @Composable () -> Unit) {
    Div(
        attrs = { classes(AppStylesheet.container) }
    ) {
        content()
    }
}