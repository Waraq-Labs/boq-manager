package com.waraqlabs.boq_manager.projects

import com.waraqlabs.boq_manager.plugins.Database
import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinInstant
import kotlinx.serialization.Serializable
import java.sql.ResultSet
import java.time.OffsetDateTime

val getCreatedOnField = { result: ResultSet ->
    result.getObject("created_on", OffsetDateTime::class.java).toInstant().toKotlinInstant()
}

data class Project(val id: Int, val name: String, val active: Boolean, val createdOn: Instant) {
    companion object {
        fun fromResultSetRow(resultSet: ResultSet): Project {
            return Project(
                id = resultSet.getInt("id"),
                name = resultSet.getString("name"),
                active = resultSet.getBoolean("active"),
                createdOn = getCreatedOnField(resultSet)
            )
        }
    }
}

data class Location(val id: Int, val name: String, val projectId: Int, val createdOn: Instant) {
    companion object {
        fun fromResultSetRow(resultSet: ResultSet): Location {
            return Location(
                id = resultSet.getInt("id"),
                name = resultSet.getString("name"),
                projectId = resultSet.getInt("project_id"),
                createdOn = getCreatedOnField(resultSet)
            )
        }
    }
}

@Serializable
data class Product(val id: Int, val name: String, val createdOn: Instant, val projectId: Int) {
    companion object {
        fun fromResultSet(resultSet: ResultSet): Product {
            return Product(
                id = resultSet.getInt("id"),
                name = resultSet.getString("product_name"),
                createdOn = getCreatedOnField(resultSet),
                projectId = resultSet.getInt("project_id")
            )
        }
    }
}

object ProjectsDAO {
    fun createProject(name: String, active: Boolean): Project {
        val st =
            Database.connection.prepareStatement("INSERT INTO projects (name, active) VALUES (?, ?) RETURNING id, name, active, created_on")
        st.setString(1, name)
        st.setBoolean(2, active)

        val result = st.executeQuery()
        if (!result.next()) {
            throw Exception("Unable to save data to DB.")
        }

        return Project.fromResultSetRow(result)
    }

    fun getProjectById(id: Int): Project? {
        val st = Database.connection.prepareStatement("SELECT * FROM projects WHERE id = ?")
        st.setInt(1, id)

        val result = st.executeQuery()
        if (!result.next()) {
            return null
        }

        return Project.fromResultSetRow(result)
    }

    fun getAllProjects(): List<Project> {
        val st = Database.connection.prepareStatement("SELECT * FROM projects")
        val result = st.executeQuery()

        val projects = mutableListOf<Project>()
        while (result.next()) {
            projects.add(Project.fromResultSetRow(result))
        }

        return projects
    }

    fun addProjectLocation(projectId: Int, name: String): Location {
        val st = Database.connection.prepareStatement(
            "INSERT INTO project_locations (project_id, name) " +
                    "VALUES (?, ?) RETURNING id, name, project_id, created_on"
        )
        st.setInt(1, projectId)
        st.setString(2, name)

        val result = st.executeQuery()
        if (!result.next()) {
            throw Exception("Unable to save project location to DB.")
        }

        return Location.fromResultSetRow(result)
    }

    fun getProjectLocations(projectId: Int): List<Location> {
        val st = Database.connection.prepareStatement(
            "SELECT id, name, project_id, created_on FROM project_locations WHERE project_id = ?"
        )
        st.setInt(1, projectId)

        val results = st.executeQuery()
        val foundLocations = mutableListOf<Location>()
        while (results.next()) {
            foundLocations.add(Location.fromResultSetRow(results))
        }

        return foundLocations.toList()
    }

    fun createProjectProduct(projectId: Int, productName: String): Product {
        val st = Database.connection.prepareStatement("INSERT INTO project_products (project_id, product_name) VALUES (?, ?) RETURNING *")
        st.setInt(1, projectId)
        st.setString(2, productName)

        val result = st.executeQuery()
        if (!result.next()) {
            throw Exception("Unable to save product to DB.")
        }

        return Product.fromResultSet(result)
    }

    fun getProductsForProject(projectId: Int): List<Product> {
        val st = Database.connection.prepareStatement("SELECT * from project_products WHERE project_id = ?")
        st.setInt(1, projectId)

        val result = st.executeQuery()
        val products = mutableListOf<Product>()
        while (result.next()) {
            products.add(
                Product.fromResultSet(result)
            )
        }

        return products
    }
}