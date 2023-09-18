package com.waraqlabs.boq_manager.plugins

import com.waraqlabs.boq_manager.auth.authRoutes
import com.waraqlabs.boq_manager.projects.projectRoutes
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.*
import java.io.File

fun Application.configureRouting() {
    routing {
        authRoutes()
        projectRoutes()

        staticFiles("/static", File("src/main/resources/static_files"))
    }
}
