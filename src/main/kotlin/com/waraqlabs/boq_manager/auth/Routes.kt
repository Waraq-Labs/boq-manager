package com.waraqlabs.boq_manager.auth

import com.waraqlabs.boq_manager.commonTemplateContext
import com.waraqlabs.boq_manager.plugins.SessionData
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.pebble.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.util.*

fun Route.authRoutes() {
    val successUrl = "/projects/create"

    authenticate {
        route("/me") {
            get {
                val user = call.principal<User>()!!
                call.respondText(user.email)
            }
        }
    }

    route("/auth") {
        route("start-login") {
            get {
                call.respond(
                    PebbleContent(
                        "auth/login.peb",
                        commonTemplateContext()
                    )
                )
            }

            post {
                val userEmail = call.receiveParameters().getOrFail("email")

                if (!AuthDAO.doesUserExist(userEmail)) {
                    return@post call.respondText(
                        "User not found.",
                        status = HttpStatusCode.NotFound
                    )
                }

                val loginUrl = URLBuilder(
                    host = call.request.local.serverHost,
                    port = call.request.local.serverPort,
                    pathSegments = listOf("auth", "login"),
                    parameters = Parameters.build {
                        append("code", generateLoginCodeWithDefaultExpiry(userEmail))
                    }
                )

                // TODO: This should send an email using Postmark
                call.respondText(loginUrl.buildString())
            }
        }

        get("login") {
            val loginCode = call.request.queryParameters.getOrFail("code")
            val parsedCode = ParsedLoginCode.tryFromCode(loginCode)

            if (parsedCode == null || !parsedCode.isValid()) {
                return@get call.respondText("Invalid login code.", status = HttpStatusCode.Forbidden)
            }

            try {
                val user = AuthDAO.getUserByEmail(parsedCode.email)
                call.sessions.set(SessionData(loggedInUserId = user.id))
                return@get call.respondRedirect(successUrl)
            } catch (e: NotFoundException) {
                call.respondText("User not found.", status = HttpStatusCode.NotFound)
            }
        }

        authenticate {
            get("/logout") {
                call.sessions.clear<SessionData>()
                call.respondRedirect("/auth/start-login")
            }
        }
    }
}