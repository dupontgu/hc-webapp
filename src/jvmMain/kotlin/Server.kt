import convert.FFMpegConverter
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.network.tls.certificates.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
import wrapper.FFMpegWrapper.ffmpegPath
import java.io.File
import kotlin.io.path.Path
import kotlin.text.toCharArray

fun main() {
    val keyStoreFile = File("build/keystore.jks")
    val keystore = generateCertificate(
        file = keyStoreFile,
        keyAlias = "sampleAlias",
        keyPassword = "foobar",
        jksPassword = "foobar"
    )

    val environment = applicationEngineEnvironment {
        log = LoggerFactory.getLogger("ktor.application")
        connector {
            port = 8080
        }
        sslConnector(
            keyStore = keystore,
            keyAlias = "sampleAlias",
            keyStorePassword = { "foobar".toCharArray() },
            privateKeyPassword = { "foobar".toCharArray() }) {
            port = 8443
            keyStorePath = keyStoreFile
        }
        module(Application::module)
    }
    embeddedServer(Netty, environment).start(wait = true)
}

fun Application.module() {


    routing {
        static("/static") { resources() }

        get("/") { call.respondRedirect { set(path = "static/index.html") } }

        post(UPLOAD_ENDPOINT) {
            val converter = FFMpegConverter(Path(this::class.java.classLoader.getResource(ffmpegPath)!!.path))
            val multipart = call.receiveMultipart()
            var responded = false
            delay(1500)
            multipart.forEachPart { part ->
                if (part is PartData.FileItem) {
                    // only process a single file
                    if (responded) return@forEachPart
                    val outputFile = converter.convertToHcFile(
                        inputStream = part.streamProvider(),
                        filename = part.originalFileName ?: "output"
                    )

                    outputFile.exceptionOrNull()?.let {
                        println("error processing file: ${it.stackTraceToString()}")
                        call.respond(HttpStatusCode.UnsupportedMediaType, it.localizedMessage)
                        responded = true
                        return@forEachPart
                    }

                    outputFile.getOrNull()?.let {
                        call.response.header(HEADER_CONTENT_DISPOSITION, "$HEADER_ATTACHMENT; filename=\"${it.name}\"")
                        call.respondFile(it)
                        responded = true
                        it.delete()
                    }
                    println("File ${part.originalFileName} processed.")
                }
                part.dispose()
            }
            if (!responded) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    "Something went wrong. Did you actually submit a file?"
                )
            }
        }
    }
}