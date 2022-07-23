import kotlinx.coroutines.flow.Flow

interface StateNav<T> {
    fun push(state: T, title: String)
    fun replace(state: T, title: String)
    fun back()
    val onPop: Flow<T>
}