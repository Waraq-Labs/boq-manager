package com.waraqlabs.boq_manager.plugins

import io.ktor.server.application.*
import io.ktor.server.pebble.*
import io.pebbletemplates.pebble.loader.FileLoader

fun Application.configureTemplating() {
    // TODO: Install cache/no-cache version based on config/environment
    install(Pebble) {
        loader(FileLoader().apply {
            prefix = "./src/main/resources/templates/"
            cacheActive(false)
        })
    }
}
