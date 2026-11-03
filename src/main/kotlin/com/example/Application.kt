package com.example

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.staticResources
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.path
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.serialization.Serializable

@Serializable
data class Status(val status: String, val kotlin: String, val java: String, val server: String)

@Serializable
data class ErrorResponse(val error: String)

// The landing page is a resource, so the deployed image needs nothing beyond
// the installed application.
private val indexTemplate: String =
    Application::class.java.getResource("/templates/index.html")!!.readText()

fun main() {
    // Wodby routes traffic to port 8080. PORT and HOST allow other ports
    // locally and in development workspaces.
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080
    val host = System.getenv("HOST") ?: "0.0.0.0"
    // Ktor stops accepting connections and finishes running requests on
    // SIGTERM, which Kubernetes sends before it replaces a pod.
    embeddedServer(Netty, port = port, host = host, module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(CallLogging) {
        filter { call -> call.request.path() != "/healthz" }
    }
    install(ContentNegotiation) {
        json()
    }
    install(StatusPages) {
        status(HttpStatusCode.NotFound) { call, status ->
            call.respond(status, ErrorResponse("not found"))
        }
        exception<Throwable> { call, cause ->
            call.application.log.error("Unhandled error", cause)
            call.respond(HttpStatusCode.InternalServerError, ErrorResponse("internal server error"))
        }
    }

    routing {
        get("/") {
            val page = indexTemplate
                .replace("{{kotlin}}", KotlinVersion.CURRENT.toString())
                .replace("{{java}}", Runtime.version().toString())
            call.respondText(page, ContentType.Text.Html)
        }
        staticResources("/assets", "static")
        get("/api/status") {
            call.respond(
                Status(
                    status = "ok",
                    kotlin = KotlinVersion.CURRENT.toString(),
                    java = Runtime.version().feature().toString(),
                    server = "Ktor",
                ),
            )
        }
        // The deployment health check. It must not depend on optional
        // services such as a database, so it answers as soon as the server runs.
        get("/healthz") {
            call.respondText("ok\n", ContentType.Text.Plain)
        }
    }
}
