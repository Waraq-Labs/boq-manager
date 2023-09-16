package com.waraqlabs.boq_manager.projects

import com.waraqlabs.boq_manager.projects.test_utils.clearDb
import io.ktor.server.testing.*
import org.junit.Test
import kotlin.test.assertEquals

class ModelTests {
    @Test
    fun `test create project`() = testApplication {
        application {
            val project = ProjectsDAO.createProject("Test Project", true)
            assertEquals("Test Project", project.name)
            assertEquals(true, project.active)

            clearDb()
        }
    }

    @Test
    fun `test create project location`() = testApplication {
        application {
            val project = ProjectsDAO.createProject("Test Project w/ Locations", true)
            val location1 = ProjectsDAO.addProjectLocation(project.id, "Location 1")
            val location2 = ProjectsDAO.addProjectLocation(project.id, "Location 2")

            for ((loc, expectedName) in listOf(location1 to "Location 1", location2 to "Location 2")) {
                assertEquals(expectedName, loc.name)
            }

            clearDb()
        }
    }

    @Test
    fun `test get project locations`() = testApplication {
        application {
            val project = ProjectsDAO.createProject("Test Project w/ Locations", true)
            ProjectsDAO.addProjectLocation(project.id, "Location 1")
            ProjectsDAO.addProjectLocation(project.id, "Location 2")

            val locations = ProjectsDAO.getProjectLocations(project.id)
            val locationNames = locations.map { it.name }.toSet()
            assertEquals(setOf("Location 1", "Location 2"), locationNames)

            clearDb()
        }
    }

    @Test
    fun `test get all projects`() = testApplication {
        application {
            ProjectsDAO.createProject("Test Project 1", true)
            ProjectsDAO.createProject("Test Project 2", false)

            assertEquals(
                setOf("Test Project 1" to true, "Test Project 2" to false),
                ProjectsDAO.getAllProjects().map {
                    it.name to it.active
                }.toSet()
            )
        }

        clearDb()
    }

    @Test
    fun `test create products`() = testApplication {
        application {
            val project = ProjectsDAO.createProject("Test Project", true)
            ProjectsDAO.createProjectProduct(project.id, "Test Product")
            ProjectsDAO.createProjectProduct(project.id, "Test Product 2")

            val projectProducts = ProjectsDAO.getProductsForProject(project.id)
            assertEquals(
                setOf("Test Product", "Test Product 2"),
                projectProducts.map { it.name }.toSet()
            )
        }

        clearDb()
    }
}