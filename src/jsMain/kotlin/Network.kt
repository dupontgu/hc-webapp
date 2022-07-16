import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.js.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.Headers
import kotlinx.coroutines.await
import org.khronos.webgl.*
import org.khronos.webgl.ArrayBuffer
import org.w3c.files.Blob
import kotlin.js.Promise

sealed class UploadResult(val message: String) {
    object Success : UploadResult("File was converted.")
    sealed class Failure(message: String) : UploadResult(message)
    class UnknownFailure(message: String) : UploadResult.Failure(message)
    class ServerFailure(message: String) : UploadResult.Failure(message)
    object NoFileSelected : UploadResult.Failure("No File Selected!")

    override fun toString(): String {
        return message
    }
}

val client = HttpClient(Js)

private suspend fun Blob.asByteArray(): ByteArray {
    val ab = (asDynamic().arrayBuffer() as Promise<ArrayBuffer>).await()
    return Int8Array(ab).unsafeCast<ByteArray>()
}

suspend fun uploadFileForProcessing(
    fileUpload: FileUpload
): UploadResult {
    runCatching {
        val cb = fileUpload.data.asByteArray()
        val response = client.submitFormWithBinaryData(url = UPLOAD_ENDPOINT, formData {
            append("file", cb, Headers.build {
                append(HttpHeaders.ContentDisposition, "filename=\"ktor_logo.png\"")
                append(HttpHeaders.ContentLength, cb.size)
            })
        })
        if (response.status != HttpStatusCode.OK) {
            return UploadResult.ServerFailure(response.bodyAsText())
        }
        response.headers[HEADER_CONTENT_DISPOSITION]
            ?.takeIf { it.contains(HEADER_ATTACHMENT) }
            ?: return UploadResult.ServerFailure("Server did not return a file - did you upload anything?")
        val channel = response.body<ByteArray>()
        val file = Blob(arrayOf(channel))
        FileSaver.saveAs(file, "${fileUpload.name}.hitclip")
    }.exceptionOrNull()?.let {
        return UploadResult.UnknownFailure(it.message ?: "[Unknown Error]")
    }
    return UploadResult.Success
}
