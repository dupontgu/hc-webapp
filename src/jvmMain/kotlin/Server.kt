import io.ktor.http.*
import io.ktor.network.tls.certificates.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.LoggerFactory
import java.io.File

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
    install(DefaultHeaders) {
        header("Cross-Origin-Embedder-Policy", "require-corp")
        header("Cross-Origin-Opener-Policy", "same-origin")
    }
    routing {
        static("/static") { resources() }

        get("/") {
            call.respondRedirect { set(path = "static/index.html") }
        }

        StartPage.values().forEach { page ->
            get("/${page.key}") {
                call.respondRedirect {
                    set(path = "static/index.html") {
                        parameters.append(START_PAGE_PARAM, page.key)
                    }
                }
            }
        }
    }
}