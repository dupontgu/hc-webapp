interface FileUpload<T> {
    val name: String
    val data: T
}

interface FileProvider<T> {
    fun getFile(): FileUpload<T>?
}

interface FileConverter<T> {
    suspend fun initialize()
    suspend fun convert(fileUpload: FileUpload<T>): ConversionResult
}