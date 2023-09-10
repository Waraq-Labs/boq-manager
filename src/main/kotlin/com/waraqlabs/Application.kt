package com.waraqlabs

import com.waraqlabs.plugins.*
import de.sharpmind.ktor.EnvConfig
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    EnvConfig.initConfig(environment.config)

    configureSecurity()
    configureTemplating()
    configureSerialization()
    configureDatabases()
    configureRouting()
}
