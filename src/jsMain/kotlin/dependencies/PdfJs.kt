@file:JsModule("jspdf")
@file:JsNonModule

package dependencies

@JsName("jsPDF")
external class PdfDocument(options: dynamic) {
    @JsName("addImage")
    fun addImage(
        imageData: String,
        format: String,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        alias: String,
        compression: String,
        rotation: Int
    )

    @JsName("save")
    fun save()
}