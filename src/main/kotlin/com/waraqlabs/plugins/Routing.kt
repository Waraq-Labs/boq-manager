package com.waraqlabs.plugins

import com.waraqlabs.auth.authRoutes
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        authRoutes()

        staticResources("/static", "static_files")
    }
}
