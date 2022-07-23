import convert.FFMpegConverter
import io.ktor.http.*
import io.ktor.http.HttpHeaders.Connection
import io.ktor.http.HttpHeaders.ContentLength
import io.ktor.http.content.*
import io.ktor.network.tls.certificates.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.servlet.*
import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
import java.io.File
import kotlin.io.path.Path
import kotlin.text.toCharArray

// for running locally only. Runs https (kinda)
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

        get("/") {
            call.respondRedirect { set(path = "static/index.html") }
        }

        get("/$START_PAGE_UPLOAD") {
            call.respondRedirect {
                set(path = "static/index.html") {
                    parameters.append(START_PAGE_PARAM, START_PAGE_UPLOAD)
                }
            }
        }

        post(UPLOAD_ENDPOINT) {
            // This is annoying, and will probably only work on GCloud or similar
            // ffmpeg binaries are bundled as a part of the WAR, _NOT_ as jvm resources.
            // could not figure out how to execute them when bundling in a jar.
            val ffmpegPath = this@module.attributes[ServletContextAttribute]
                .getResource("/WEB-INF/${FFMpegWrapper.ffmpegPath}").path
            val converter = FFMpegConverter(Path(ffmpegPath))
            val multipart = call.receiveMultipart()
            var responded = false
            multipart.forEachPart { part ->
                if (part is PartData.FileItem) {
                    // only process a single file
                    if (responded) return@forEachPart
                    val outputFile = converter.convertToHcFile(
                        inputStream = part.streamProvider(),
                        filename = part.originalFileName ?: "output"
                    )

                    suspend fun invalidFile(message: String) {
                        call.respond(HttpStatusCode.UnsupportedMediaType, message)
                        responded = true
                    }

                    outputFile.exceptionOrNull()?.let {
                        println("error processing file: ${it.stackTraceToString()}")
                        return@forEachPart invalidFile(it.localizedMessage)
                    }

                    outputFile.getOrNull()?.let {
                        if (it.length() == 0L) return@forEachPart invalidFile("Output file was empty.")
                        with (call.response) {
                            header(HEADER_CONTENT_DISPOSITION, "$HEADER_ATTACHMENT; filename=\"${it.name}\"")
                            header(ContentLength, it.length())
                        }
                        call.respondFile(it)
                        responded = true
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