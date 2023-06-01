import kotlinx.serialization.Serializable

@Serializable
sealed class ConversionResult {
    abstract val message: String
    @Serializable object Success : ConversionResult() { override val message: String = "File was converted." }
    @Serializable sealed class Failure : ConversionResult()
    @Serializable class UnknownFailure(override val message: String) : Failure()
    @Serializable class ServerFailure(override val message: String) : Failure()

    override fun toString(): String {
        return message
    }
}