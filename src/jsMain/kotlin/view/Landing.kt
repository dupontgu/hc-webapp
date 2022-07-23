package view

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.dom.*

@Composable
fun Landing() {
    H1 {
        Text("HotClasps!")
    }
    Div {
        P {
            Text("Open source, HitClips compatible audio cartridges. Check out the intro video:")
        }
    }
    Div {
        Iframe({
            attr("width", "560")
            attr("height", "315")
            attr("src", "https://www.youtube.com/embed/T5bPNSPXJuc")
            attr("allow", "accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture")
        })
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

//<iframe width="560" height="315" src="https://www.youtube.com/embed/T5bPNSPXJuc" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>