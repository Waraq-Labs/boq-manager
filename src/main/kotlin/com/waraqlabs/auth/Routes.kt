package com.waraqlabs.auth

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*

fun Route.authRoutes() {
    route("/auth") {
        get("generate-login-code/") {
            val userEmail = call.request.queryParameters.getOrFail("email")

            if (!UserModel.doesUserExist(userEmail)) {
                return@get call.respondText(
                    "A user with this email does not exist.",
                    status = HttpStatusCode.NotFound
                )
            }

            call.respondText(generateLoginCodeWithDefaultExpiry(userEmail))
        }

        get("login/") {
            val loginCode = call.request.queryParameters.getOrFail("code")
            val parsedCode = ParsedLoginCode.tryFromCode(loginCode)

            if (parsedCode == null || !parsedCode.isValid()) {
                call.respondText("Invalid login code.", status = HttpStatusCode.Forbidden)
            }

            call.respondText("Success")
        }
    }
}