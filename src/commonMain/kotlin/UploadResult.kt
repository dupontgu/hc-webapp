import kotlinx.serialization.Serializable

@Serializable
sealed class UploadResult {
    abstract val message: String
    @Serializable object Success : UploadResult() { override val message: String = "File was converted." }
    @Serializable sealed class Failure : UploadResult()
    @Serializable class UnknownFailure(override val message: String) : Failure()
    @Serializable class ServerFailure(override val message: String) : Failure()

    override fun toString(): String {
        return message
    }
}