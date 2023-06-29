package view

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.css.paddingLeft
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.*
import view.SetupStep.*

enum class SetupStep {
    PREPARE_AUDIO, PREPARE_ARTWORK, IDENTIFY_PARTS, REMOVE_CLIP, ATTACH_DEBUGGER, TRANSFER_AUDIO, REPLACE_CLIP, PLAY_AUDIO
}

val SetupStep.title: String
    get() = when (this) {
        PREPARE_AUDIO -> "Prepare your audio."
        PREPARE_ARTWORK -> "Print your artwork."
        IDENTIFY_PARTS -> "Identify the components."
        REMOVE_CLIP -> "Remove the circuit board from the case."
        ATTACH_DEBUGGER -> "Attach the circuit board to the debugger."
        TRANSFER_AUDIO -> "Transfer your audio via USB."
        REPLACE_CLIP -> "Replace the circuit board in the case."
        PLAY_AUDIO -> "Rock out!"
    }


@Composable
fun SetupView() {
    H1 { Text("Setup") }
    Div {
        SetupStep.values().forEach {
            SetupStep(it)
        }
    }
}

@Composable
fun SetupStep(step: SetupStep) {
    Div {
        H3 {
            Text("${step.ordinal + 1}. ${step.title}")
        }
        Div({ style { paddingLeft(20.px) } }) {
            return@Div when (step) {
                PREPARE_AUDIO -> PrepareAudioStep()
                PREPARE_ARTWORK -> PrepareArtworkStep()
                IDENTIFY_PARTS -> IdentifyComponentsStep()
                REMOVE_CLIP -> RemoveClipStep()
                ATTACH_DEBUGGER -> AttachDebuggerStep()
                TRANSFER_AUDIO -> TransferStep()
                REPLACE_CLIP -> ReplaceStep()
                PLAY_AUDIO ->  { }
            }
        }
    }
}

@Composable
fun PrepareAudioStep() {
    Text("- Visit the ")
    A("/upload") { Text("upload") }
    Text(" page to convert your audio files to the custom .htclp format.")
    Line("You can create as many files as you want, but keep in mind that you'll only have ~12MB of storage.")
}

@Composable
fun PrepareArtworkStep() {
    Text("- Visit the ")
    A("/artwork") { Text("artwork") }
    Text(" page to convert an image to the exact size of the HotClasps case and prepare for printing.")
    Line("You can do this on your own if you want! The window of the case is exactly 20mm X 20mm.")
}

@Composable
fun IdentifyComponentsStep() {
    Line("Identify all of the components using the image below.")
    Line("Your circuit board may already be inside the case.")
    B { Line("Not pictured - you will also need a micro-USB cable to transfer data from your computer.") }
    InlineImage("/static/img/setup_identify.jpg")
}

@Composable
fun RemoveClipStep() {
    Line("There are two small tabs on the circuit board that clip into the side of the case.")
    Line("You'll want to remove one tab at a time.")
    Line("Hold the case as demonstrated below.")
    Line("Wedge your right index finger under the circuit board - you're going to pull it up towards you.")
    Line("Press your left thumb against the case wall that's furthest from you - you're going to push it away.")
    InlineImage("/static/img/setup_remove.jpg")
    MultBr(1)
    Line("Gently (but firmly) push the case wall and pull the circuit board towards you until the tab is out of the case.")
    Line("Once it is out, slide out the other tab remove the circuit board completely.")
    InlineImage("/static/img/setup_remove_2.jpg")
}

@Composable
fun AttachDebuggerStep() {
    Line("You're going to attach the circuit board to the debugger.")
    B { Line("It's best to do this with the power disconnected. Do not plug in the USB cable yet!") }
    Line("Line up the edges of the circuit board with the \"FlexiPins\" on the debugger.")
    Line("One side of the circuit board has 5 metallic notches, and the other has 4. ")
    Line("Line up the 5-notch side of the circuit board with the debugger's row of 5 pins.")
    InlineImage("/static/img/setup_attach_1.jpg")
    MultBr(1)
    Line("Gently pull back one of the rows of pins, and push the circuit board towards the debugger until it is lying flat.")
    Line("All of the metallic notches on the circuit board should be making solid contact with the debugger's pins.")
    InlineImage("/static/img/setup_attach_2.jpg")
}

@Composable
fun TransferStep() {
    Line("Plug your micro-USB cable into the deugger, and connect the other end to your computer's USB port.")
    Line("After about 1 second, you should see the red light on the debugger blink once.")
    Line("After this, you should see a new disk named \"CIRCUITPY\" available on your computer.")
    B { Line("Be careful with the files on this drive - these contain the code that makes the cartridge work!") }
    Line("Transfer all of the .htclip files you downloaded in step 1 into the directory named \"sound\".")
    B { Line ( "Only files in the \"sound\" directory will be played!")}
    Line("This transfer might be slow! Give it a few minutes.")
    Line("Note - files will be played in alpha-numeric order. You can rename the files if they don't play in the order you want.")
}

@Composable
fun InlineImage(src: String) {
    MultBr(1)
    Img(src) { style { width(500.px) } }
    MultBr(1)
}

@Composable
fun ReplaceStep() {
    Line("Once you're finished transferring your audio, eject the disk from your computer.")
    Line("Unplug the USB cable from either/both ends.")
    Line("Gently remove the circuit board from the debugger by pulling it up towards you.")
    Line("Line up the circuit board with the case so that the 6 large rectangular metallic pads are facing you. (See image)")
    Line("Slide one of the circuit board's tabs into the corresponding slot case slot.")
    Line("Gently push the other side of the circuit board down into the case until the second tab clicks into place.")
    Line("It may help to slightly bend the slotted case wall away to allow the circuit board to slide down.")
    InlineImage("/static/img/setup_replace.jpg")
}