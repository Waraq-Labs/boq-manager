package com.waraqlabs.boq_manager

import com.waraqlabs.boq_manager.plugins.*
import de.sharpmind.ktor.EnvConfig
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

@Suppress("unused")
fun Application.module() {
    EnvConfig.initConfig(environment.config)

    configureSecurity()
    configureAuthentication()
    configureTemplating()
    configureSerialization()
    configureDatabases()
    configureRouting()
}
