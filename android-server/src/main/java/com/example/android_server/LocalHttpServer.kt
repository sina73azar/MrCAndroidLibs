package com.example.android_server

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LocalHttpServer(private val port: Int = 8080) {
    private var server: ApplicationEngine? = null

    fun start() {
        CoroutineScope(Dispatchers.IO).launch {
            server = embeddedServer(CIO, port = port, host = "0.0.0.0") {
                routing {
                    get("/") {
                        call.respondText("Hello from Android Server!")
                    }

                    get("/status") {
                        call.respondText(
                            text = """{"status": "running", "battery": "ok"}""",
                            contentType = io.ktor.http.ContentType.Application.Json
                        )
                    }

                    post("/api/action") {
                        // handle payload
                        call.respond(HttpStatusCode.OK, "Action processed")
                    }
                }
            }.start(wait = false)
        }
    }

    fun stop() {
        server?.stop(gracePeriodMillis = 1000, timeoutMillis = 2000)
    }
}
