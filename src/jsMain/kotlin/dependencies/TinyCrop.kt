package dependencies

fun createTinyCrop(
    parentElementId: String,
    rawImageData: String
): Cropper {
    return TinyCrop.create(object {
        @JsName("parent")
        val parent: String = "#${parentElementId}"

        @JsName("image")
        val image: String = rawImageData

        @JsName("selection")
        var selection = object {
            // force a square aspect ratio
            @JsName("aspectRatio")
            val aspectRatio: Float = 1f
            @JsName("activeColor")
            val activeColor: String = "blue"
            @JsName("color")
            val color: String = "red"
        }
    })
}

@JsModule("tinycrop")
@JsNonModule
external object TinyCrop {
    fun create(options: dynamic): Cropper
}

external class Region {
    @JsName("x") val x: Int
    @JsName("y") val y: Int
    @JsName("width") val width: Int
    @JsName("height") val height: Int
}

external class Cropper {
    @JsName("on")
    fun on(eventName: String, callback: (Region) -> Unit)

    @JsName("imageLayer")
    val imageLayer: dynamic
}