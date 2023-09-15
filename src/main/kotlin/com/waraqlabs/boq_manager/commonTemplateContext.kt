package com.waraqlabs.boq_manager

import com.waraqlabs.boq_manager.auth.UnauthenticatedUser
import com.waraqlabs.boq_manager.auth.User
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.util.pipeline.*

fun PipelineContext<Unit, ApplicationCall>.commonTemplateContext(): Map<String, Any> {
    val user = call.principal<User>()
    return mapOf("user" to (user ?: UnauthenticatedUser))
}