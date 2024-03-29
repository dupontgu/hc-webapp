package view

import DefaultFileProvider
import androidx.compose.runtime.*
import dependencies.*
import kotlinx.browser.document
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.accept
import org.jetbrains.compose.web.attributes.max
import org.jetbrains.compose.web.attributes.min
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.khronos.webgl.ArrayBuffer
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLElement
import org.w3c.files.Blob
import org.w3c.files.FileReader
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.js.Promise
import kotlin.math.ceil

@Composable
fun ArtworkView() {
    Div {
        H2 { Text("Generate Artwork") }

        H5 {
            Line("Upload an image, and choose a square crop using the upper left corner of the square.")
            Line(
                "When you click the 'Download' button, a PDF will be generated " +
                        "with your selection at the exact size of the HotClasps case."
            )
            Line("Print the PDF at full size.")
        }

        // The image itself, as a data URL
        var imageState: String? by remember {
            mutableStateOf(null)
        }
        var imageCopies: Int by remember {
            mutableStateOf(1)
        }
        val image = imageState

        if (image == null) {
            Input(InputType.File) {
                val inputId = "image_input"
                id(inputId)
                val provider = DefaultFileProvider(inputId)
                accept(".jpg, .jpeg, .png")
                onChange {
                    val blob = provider.getFile()?.data ?: return@onChange
                    val reader = FileReader()
                    reader.onload = {
                        imageState = reader.result?.toString()
                        Unit.asDynamic()
                    }
                    reader.readAsDataURL(blob)
                }
            }

        } else {
            var cropSpec: CropSpec? by remember { mutableStateOf(null) }

            Div {
                Text("Copies of image: ")
                Input(InputType.Number) {
                    defaultValue(1)
                    min("1")
                    max("54")
                    onChange { event ->
                        event.value?.toInt()?.let {
                            imageCopies = it
                        }
                    }
                }
            }

            Div(attrs = {
                id("image")
                style { width(CSSUnitValueTyped(20f, CSSUnit.percent)) }
                ref {
                    val cropper = createTinyCrop(it.id, image)
                    cropper.on("change") { region ->
                        cropSpec = CropSpec(region, cropper.imageLayer.image.source as HTMLElement)
                    }
                    onDispose { }
                }
            }) {
                val scope = rememberCoroutineScope()
                HcButton("Download PDF", enable = cropSpec != null) {
                    cropSpec?.let { scope.launch { onCropSelected(it, imageCopies) } }
                }
                Br { }
            }
        }
    }
}

private data class CropSpec(
    val region: Region,
    val sourceImage: HTMLElement
)

@NoLiveLiterals
private suspend fun onCropSelected(
    cropSpec: CropSpec,
    copies: Int
) {
    val region = cropSpec.region
    val resizeCanvas = document.createElement("canvas") as HTMLCanvasElement
    resizeCanvas.width = region.width
    resizeCanvas.height = region.height
    val context = resizeCanvas.getContext("2d")

    context.asDynamic().drawImage(
        cropSpec.sourceImage,
        region.x, region.y, region.width, region.height,
        0, 0, region.width, region.height
    )

    val imageData = resizeCanvas.toDataURL("image/jpeg")
    val document = PdfDocument(object {
        @JsName("format")
        val format: String = "letter"
    })

    val imgHeight = 20
    val imgSpacing = 10
    val cols = 6
    val rows = ceil(copies.toFloat() / cols.toFloat()).toInt()
    var xOff = 0
    var yOff = 0
    var count = 0
    for (i in 0 until rows) {
        for (j in 0 until cols) {
            if (++count > copies) break
            document.addImage(
                imageData, "JPEG",
                (imgSpacing * 2) + xOff, imgSpacing + yOff,
                imgHeight, imgHeight,
                "artwork", "NONE", 0
            )
            xOff += imgHeight + imgSpacing
        }
        xOff = 0
        yOff += imgHeight + imgSpacing
    }
    document.save()
}