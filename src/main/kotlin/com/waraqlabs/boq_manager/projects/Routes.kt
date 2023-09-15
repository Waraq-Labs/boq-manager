package com.waraqlabs.boq_manager.projects

import com.waraqlabs.boq_manager.commonTemplateContext
import com.waraqlabs.boq_manager.auth.User
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.pebble.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.projectRoutes() {
    authenticate {
        route("/projects") {
            get {
                call.respondText("Projects list here.")
            }

            route("/create") {
                get {
                    val user = call.principal<User>()!!
                    if (!isAllowed(user.role, Permission.CreateProject)) {
                        return@get call.respondText(
                            "You do not have permission to create a new project.",
                            status = HttpStatusCode.Forbidden
                        )
                    }

                    call.respond(
                        PebbleContent("projects/create_project.peb", commonTemplateContext())
                    )
                }

                post {
                    val user = call.principal<User>()!!
                    if (!isAllowed(user.role, Permission.CreateProject)) {
                        return@post call.respondText(
                            "You do not have permission to create a new project.",
                            status = HttpStatusCode.Forbidden
                        )
                    }

                    val formParams = call.receiveParameters()
                    val projectName = formParams["project_name"]
                    val locationNames = formParams.getAll("location_name[]")

                    if (projectName.isNullOrEmpty() || locationNames.isNullOrEmpty()) {
                        return@post call.respondText("Data missing.", status = HttpStatusCode.BadRequest)
                    }

                    for (locationName in locationNames) {
                        if (locationName.isEmpty()) {
                            return@post call.respondText("Empty location name(s).", status = HttpStatusCode.BadRequest)
                        }
                    }

                    val project = ProjectsDAO.createProject(projectName, true)
                    for (locationName in locationNames) {
                        ProjectsDAO.addProjectLocation(project.id, locationName)
                    }

                    return@post call.respondRedirect(
                        "/projects"
                    )
                }
            }
        }
    }
}