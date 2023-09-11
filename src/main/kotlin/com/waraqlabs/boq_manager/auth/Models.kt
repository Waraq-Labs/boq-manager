package com.waraqlabs.boq_manager.auth

import com.waraqlabs.boq_manager.plugins.Database
import io.ktor.server.plugins.*
import java.io.InvalidClassException


enum class UserRole {
    ADMIN, PROJECT_MANAGER, CLIENT;

    companion object {
        fun fromDbRepresentation(dbRole: String) = when (dbRole) {
            "admin" -> ADMIN
            "project-manager" -> PROJECT_MANAGER
            "client" -> CLIENT
            else -> throw InvalidClassException("$dbRole is not valid.")
        }
    }

    fun toDbRepresentation() = when (this) {
        ADMIN -> "admin"
        PROJECT_MANAGER -> "project-manager"
        CLIENT -> "client"
    }
}

data class User(val id: Int, val email: String, val role: UserRole)

object AuthDAO {
    fun doesUserExist(email: String): Boolean {
        val st = Database.connection.prepareStatement("SELECT count(*) FROM users WHERE email = ?")
        st.setString(1, email)

        val result = st.executeQuery()
        result.next()
        val totalMatchedRows = result.getInt("count")

        return totalMatchedRows == 1
    }

    fun getUser(id: Int): User {
        val st = Database.connection.prepareStatement("SELECT id, email, role FROM users WHERE id = ?")
        st.setInt(1, id)
        val result = st.executeQuery()
        if (!result.next()) {
            throw NotFoundException("User not found")
        }

        return User(
            id = result.getInt("id"),
            email = result.getString("email"),
            role = UserRole.fromDbRepresentation(result.getString("role"))
        )
    }
}