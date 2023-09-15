package com.waraqlabs.boq_manager.projects

import com.waraqlabs.boq_manager.auth.test_utils.loginAsAdmin
import com.waraqlabs.boq_manager.auth.test_utils.loginAsClient
import com.waraqlabs.boq_manager.auth.test_utils.loginAsProjectManager
import com.waraqlabs.boq_manager.projects.test_utils.clearDb
import io.ktor.client.plugins.cookies.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ViewTests {
    @Test
    fun `test create project required admin`() = testApplication {
        val client = createClient {
            install(HttpCookies)
            followRedirects = false
        }
        val response = client.get("/projects/create")

        assertEquals(
            HttpStatusCode.Found, response.status
        )

        for ((loginFun, expectedStatus) in listOf(
            ::loginAsClient to HttpStatusCode.Forbidden,
            ::loginAsProjectManager to HttpStatusCode.Forbidden,
            ::loginAsAdmin to HttpStatusCode.OK,
        )) {
            loginFun(client)
            assertEquals(
                expectedStatus, client.get("/projects/create").status
            )
        }
    }

    @Test
    fun `test create project with valid data`() = testApplication {
        val client = createClient {
            install(HttpCookies)
        }
        loginAsAdmin(client)

        val projectData = listOf(
            "project_name" to "Test Project",
            "location_name[]" to "Location 1",
            "location_name[]" to "Location 2",
            "location_name[]" to "Location 3",
        )

        client.post("/projects/create") {
            header(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
            setBody(projectData.formUrlEncode())
        }

        val allProjects = ProjectsDAO.getAllProjects()
        assertEquals(
            setOf("Test Project"),
            allProjects.map { it.name }.toSet()
        )

        clearDb()
    }

    @Test
    fun `test create project with invalid data`() = testApplication {
        val client = createClient {
            install(HttpCookies)
        }
        loginAsAdmin(client)

        for (sampleData in listOf(
            listOf("project_name" to "Test Project", "location_name[]" to "Location 1", "location_name[]" to ""),
            listOf("project_name" to "", "location_name[]" to "Location 1"),
            listOf("project_name" to "Test Project"),
        )) {
            val response = client.post("/projects/create") {
                header(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
                setBody(sampleData.formUrlEncode())
            }

            assertEquals(
                HttpStatusCode.BadRequest, response.status
            )

            val allProjects = ProjectsDAO.getAllProjects()
            assertEquals(
                setOf(),
                allProjects.map { it.name }.toSet()
            )

            clearDb()
        }
    }
}