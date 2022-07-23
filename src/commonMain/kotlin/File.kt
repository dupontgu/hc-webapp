interface FileUpload<T> {
    val name: String
    val data: T
}

interface FileProvider<T> {
    fun getFile(): FileUpload<T>?
}

interface FileUploader<T> {
    suspend fun upload(fileUpload: FileUpload<T>): UploadResult
}