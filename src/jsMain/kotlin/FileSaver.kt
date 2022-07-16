import org.w3c.files.Blob
import org.w3c.files.File

@JsModule("file-saver")
@JsNonModule
external object FileSaver {
    fun saveAs(file: Blob, filename:String): Any
    fun saveAs(file: File): Any
}