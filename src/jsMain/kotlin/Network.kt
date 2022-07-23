import dependencies.FileSaver
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.js.*
import io.ktor.client.fetch.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders.ContentLength
import kotlinx.coroutines.await
import org.khronos.webgl.*
import org.khronos.webgl.ArrayBuffer
import org.w3c.files.Blob
import kotlin.js.Promise

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
suspend fun uploadFileForProcessing(
    fileUpload: FileUpload<Blob>
): UploadResult {
    runCatching {
        // Would have preferred to use a ktor client here, but:
        // A: converting from a kotlin ByteArray to a Blob for the FileSaver was annoying
        // B: It wasn't pulling the full response bodies - files were getting cut off?
        val formData = js("new FormData()") as FormData
        formData.append("file", fileUpload.data as io.ktor.client.fetch.Blob)
        val initObject = (js("{}") as RequestInit).apply {
            method = "POST"
            body = formData
        }
        val res = fetch(UPLOAD_ENDPOINT, initObject).await()
        if (res.status != HttpStatusCode.OK.value.toShort()) {
            return UploadResult.ServerFailure(res.text().await())
        }

        res.headers.get(HEADER_CONTENT_DISPOSITION)
            ?.takeIf { it.contains(HEADER_ATTACHMENT) }
            ?: return UploadResult.ServerFailure("Server did not return a file - did you upload anything?")

        FileSaver.saveAs(res.blob().await(), "${fileUpload.name}.$HTCLP_SUFFIX")
    }.exceptionOrNull()?.let {
        return UploadResult.UnknownFailure(it.message ?: "[Unknown Error]")
    }
    return UploadResult.Success
}
