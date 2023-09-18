package com.waraqlabs.boq_manager.projects

import com.waraqlabs.boq_manager.auth.User
import com.waraqlabs.boq_manager.commonTemplateContext
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.pebble.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString

suspend fun PipelineContext<Unit, ApplicationCall>.handleEditProjectPost() {
    @Serializable
    class ResponseObject (val error: String? = null)

    val projectId = call.parameters["projectId"]!!.toInt()
    val project = ProjectsDAO.getProjectById(projectId)
        ?: return call.respondText("Project not found", status = HttpStatusCode.NotFound)

    val locations = ProjectsDAO.getProjectLocations(project.id)
    val products = ProjectsDAO.getProductsForProject(project.id)

    val projectEditForm = ProjectEditForm(call.receiveParameters(), project, locations, products)
    val validity = projectEditForm.isValid()
    if (!validity.first) {
        call.respondText(
            DefaultJson.encodeToString(
                ResponseObject(validity.second)
            ),
            contentType = ContentType.Application.Json,
            status = HttpStatusCode.BadRequest
        )
    }

    call.respondText(
        DefaultJson.encodeToString(
            ResponseObject()
        ),
        contentType = ContentType.Application.Json
    )
}

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
                            return@post call.respondText(
                                "Empty location name(s).",
                                status = HttpStatusCode.BadRequest
                            )
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

            route("/{projectId}") {
                route("/edit") {
                    get {
                        val user = call.principal<User>()!!
                        if (!isAllowed(user.role, Permission.CreateProject)) {
                            return@get call.respondText(
                                "You do not have permission to create a new project.",
                                status = HttpStatusCode.Forbidden
                            )
                        }

                        val projectId = call.parameters["projectId"]!!.toInt()
                        val project = ProjectsDAO.getProjectById(projectId)
                            ?: return@get call.respondText("Project not found", status = HttpStatusCode.NotFound)

                        val locations = ProjectsDAO.getProjectLocations(project.id)
                        val products = ProjectsDAO.getProductsForProject(project.id)

                        val commonContext = commonTemplateContext()
                        return@get call.respond(
                            PebbleContent(
                                "projects/edit_project.peb",
                                commonContext + mapOf(
                                    "project" to project,
                                    "locations" to locations,
                                    "products" to products
                                )
                            )
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

                        return@post this.handleEditProjectPost()
                    }
                }

                route("/products") {
                    post {
                        val user = call.principal<User>()!!
                        if (!isAllowed(user.role, Permission.EditProject)) {
                            return@post call.respondText(
                                "You don't have permission to create products.",
                                status = HttpStatusCode.Forbidden
                            )
                        }

                        val formData = call.receiveParameters()
                        val productName = formData["new-product-name"]
                            ?: return@post call.respondText(
                                "Missing data.",
                                status = HttpStatusCode.BadRequest
                            )

                        val projectId = call.parameters["projectId"]!!
                        val product = ProjectsDAO.createProjectProduct(projectId.toInt(), productName)

                        call.respond(product)
                    }
                }
            }
        }
    }
}