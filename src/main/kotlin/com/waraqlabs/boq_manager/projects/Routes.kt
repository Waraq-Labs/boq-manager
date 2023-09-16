package com.waraqlabs.boq_manager.projects

import com.waraqlabs.boq_manager.auth.User
import com.waraqlabs.boq_manager.commonTemplateContext
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
                val projects = ProjectsDAO.getAllProjects()
                val baseContext = commonTemplateContext()

                val user = call.principal<User>()!!

                return@get call.respond(
                    PebbleContent(
                        "projects/projects_list.peb",
                        baseContext + mapOf(
                            "projects" to projects,
                            "canCreateProjects" to isAllowed(user.role, Permission.CreateProject),
                            "canEditProjects" to isAllowed(user.role, Permission.EditProject),
                            "canRecordWork" to isAllowed(user.role, Permission.RecordWork),
                            "canViewProgress" to isAllowed(user.role, Permission.ViewProgressReport)
                        )
                    )
                )
            }

            route("/edit/{projectId}") {
                get {
                    val projectId = call.parameters["projectId"]!!.toInt()
                    val project = ProjectsDAO.getProjectById(projectId)
                        ?: return@get call.respondText("Project not found", status = HttpStatusCode.NotFound)

                    val locations = ProjectsDAO.getProjectLocations(project.id)

                    val commonContext = commonTemplateContext()
                    return@get call.respond(
                        PebbleContent(
                            "projects/edit_project.peb",
                            commonContext + mapOf(
                                "project" to project,
                                "locations" to locations
                            )
                        )
                    )
                }
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