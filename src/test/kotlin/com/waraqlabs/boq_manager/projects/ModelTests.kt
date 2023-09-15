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
}