package com.waraqlabs.boq_manager.plugins

import com.waraqlabs.boq_manager.auth.authRoutes
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        authRoutes()

        staticResources("/static", "static_files")
    }
}
