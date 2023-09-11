package com.waraqlabs.boq_manager.auth

import io.ktor.server.plugins.*
import io.ktor.server.testing.*
import org.junit.Test
import kotlin.test.assertEquals

class ModelTests {
    @Test
    fun `test get user`() = testApplication {
        application {
            val user = AuthDAO.getUser(1)
            assertEquals(1, user.id)
            assertEquals("admin@jhuengineering.com", user.email)
            assertEquals(UserRole.ADMIN, user.role)
        }
    }

    @Test
    fun `test get user throws exception on non-existent id`() = testApplication {
        application {
            try {
                AuthDAO.getUser(100)
                assert(false)
            } catch (e: NotFoundException) {
            }
        }
    }
}