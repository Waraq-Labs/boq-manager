package com.waraqlabs.boq_manager.plugins

import de.sharpmind.ktor.EnvConfig
import io.ktor.server.application.*
import io.ktor.server.sessions.*
import kotlin.time.DurationUnit
import kotlin.time.toDuration

data class SessionData(val loggedInUserId: Int? = null)

fun Application.configureSecurity() {
    val cookieSigningKey = EnvConfig.getString("secretKey")

    install(Sessions) {
        cookie<SessionData>("SESSION") {
            cookie.maxAge = (3).toDuration(DurationUnit.DAYS)
            cookie.path = "/"
            cookie.extensions["SameSite"] = "lax"

            transform(SessionTransportTransformerMessageAuthentication(cookieSigningKey.toByteArray(Charsets.UTF_8)))
        }
    }
}
