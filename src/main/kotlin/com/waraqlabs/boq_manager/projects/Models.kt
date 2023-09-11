package com.waraqlabs.boq_manager.projects

import com.waraqlabs.boq_manager.plugins.Database
import java.sql.ResultSet
import java.time.OffsetDateTime
import java.time.ZonedDateTime

val getCreatedOnField = { result: ResultSet ->
    result.getObject("created_on", OffsetDateTime::class.java).toZonedDateTime()
}

data class Project(val id: Int, val name: String, val active: Boolean, val createdOn: ZonedDateTime) {
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

data class Location(val id: Int, val name: String, val projectId: Int, val createdOn: ZonedDateTime) {
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
}