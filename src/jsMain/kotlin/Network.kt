import dependencies.FileSaver
import io.ktor.client.fetch.*
import io.ktor.http.*
import io.ktor.http.HttpMethod.Companion.Post
import kotlinx.coroutines.await
import org.w3c.files.Blob

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
            method = Post.value
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
