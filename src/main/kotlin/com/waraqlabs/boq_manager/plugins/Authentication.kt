package com.waraqlabs.boq_manager.plugins

import com.waraqlabs.boq_manager.auth.AuthDAO
import io.ktor.server.application.*
import io.ktor.server.auth.*

fun Application.configureAuthentication() {
    install(Authentication) {
        session<SessionData> {
            validate {session ->
                if (session.loggedInUserId != null) {
                    return@validate AuthDAO.getUser(session.loggedInUserId)
                }

                return@validate null
            }

            challenge("/auth/start-login")
        }
    }
}