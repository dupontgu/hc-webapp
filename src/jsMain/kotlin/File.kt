import org.w3c.files.Blob

interface FileUpload {
    val name: String
    val data: Blob
}

interface FileProvider {
    fun getFile(): FileUpload?
}

interface FileUploader {
    suspend fun upload(fileUpload: FileUpload): UploadResult
}