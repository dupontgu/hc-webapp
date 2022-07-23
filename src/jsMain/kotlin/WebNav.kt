import kotlinx.browser.window
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.w3c.dom.events.EventListener

class DefaultWebNav<T>(
    private val serializationStrategy: SerializationStrategy<T>,
    private val deserializationStrategy: DeserializationStrategy<T>
) : StateNav<T> {
    override fun push(state: T, title: String) {
        val stateString = Json.encodeToString(serializationStrategy, state)
        window.history.pushState(data = stateString, title = title)
    }

    override fun replace(state: T, title: String) {
        val stateString = Json.encodeToString(serializationStrategy, state)
        window.history.replaceState(data = stateString, title = title)
    }

    override fun back() {
        window.history.back()
    }

    override val onPop: Flow<T>
        get() = callbackFlow {
            val callback = EventListener { event ->
                val stateString = event.asDynamic().state as? String
                val state = stateString?.let { Json.decodeFromString(deserializationStrategy, it) }
                state?.let { trySend(it) }
            }
            window.addEventListener("popstate", callback)
            awaitClose {
                window.removeEventListener("popstate", callback)
            }
        }

}