package com.waraqlabs.plugins

import io.ktor.server.application.*
import io.ktor.server.config.*
import java.sql.Connection
import java.sql.DriverManager

fun Application.configureDatabases() {
    Database.init(environment.config)
}

object Database {
    private var _connection: Connection? = null

    fun init(config: ApplicationConfig) {
        _connection = DriverManager.getConnection(config.property("ktor.database.connectionURI").getString())
    }

    val connection
        get() = _connection!!
}