package com.waraqlabs.boq_manager.auth

import com.waraqlabs.boq_manager.plugins.SessionData
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.mustache.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.util.*

fun Route.authRoutes() {
    route("/auth") {
        route("start-login") {
            get {
                call.respond(
                    MustacheContent(
                        "login.hbs",
                        emptyMap<String, String>()
                    )
                )
            }

            post {
                val userEmail = call.receiveParameters().getOrFail("email")

                if (!UserModel.doesUserExist(userEmail)) {
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

            if (!UserModel.doesUserExist(parsedCode.email)) {
                return@get call.respondText(
                    "User not found.",
                    status = HttpStatusCode.NotFound
                )
            }

            call.sessions.set(SessionData(loggedInUserId = 5))
            call.respondText("Success")
        }
    }
}