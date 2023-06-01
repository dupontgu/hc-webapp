package view

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.dom.*
import style.Container

@Composable
fun Landing() {
    H1 {
        Text("HotClasps!")
    }
    Div {
        P {
            Text("Open source, HitClips compatible audio cartridges - Coming Soon.")
            Br()
            Text("Check out the intro video on YouTube:")
        }
    }
    Div {
        A("https://youtu.be/T5bPNSPXJuc") {
            Img("https://img.youtube.com/vi/T5bPNSPXJuc/0.jpg")
        }
    }
    Div {
        H2 {
            Text("If you are interested in purchasing, please fill out the form ")
            A("https://forms.gle/3kkTPsjigv3yTMHY8") {
                Text("here.")
            }
        }
    }
}
