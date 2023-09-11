package com.waraqlabs.boq_manager.auth

import de.sharpmind.ktor.EnvConfig
import io.ktor.client.plugins.cookies.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.test.Test
import kotlin.test.assertEquals

class AuthTests {
    val ADMIN_EMAIL = "admin@jhuengineering.com"

    @Test
    fun `test generate login code for valid user`() = testApplication {
        val response = client.post("/auth/start-login?email=$ADMIN_EMAIL") {
            header(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
            setBody(
                listOf("email" to ADMIN_EMAIL).formUrlEncode()
            )
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun `test login code generation function`() = testApplication {
        application {
            val expiryMinutes = EnvConfig.getInt("loginCodeExpiryMinutes").toLong()

            val expiryTime = ZonedDateTime.now(ZoneId.of("UTC")).plusMinutes(expiryMinutes)

            val secretKey = SecretKeySpec(EnvConfig.getString("secretKey").toByteArray(Charsets.UTF_8), "HmacSHA1")
            val mac = Mac.getInstance("HmacSHA1")
            mac.init(secretKey)

            val stringToSign = "$ADMIN_EMAIL\$${expiryTime.toEpochSecond()}"
            val rawHmac = mac.doFinal(stringToSign.toByteArray(Charsets.UTF_8))
            val hmacHex = rawHmac.toHexString()
            val expectedLoginCode = "$stringToSign\$$hmacHex"

            assertEquals(expectedLoginCode, generateLoginCode(ADMIN_EMAIL, expiryTime))
        }
    }

    @Test
    fun `test login attempt with expired code`() = testApplication {
        val expiryDateTime = ZonedDateTime.now(ZoneId.of("UTC")).minusMinutes(10)
        val loginCode = generateLoginCode(ADMIN_EMAIL, expiryDateTime)
        val response = client.get("/auth/login?code=$loginCode")

        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `test login with correct code`() = testApplication {
        val client = createClient {
            install(HttpCookies)
        }

        val expiryDateTime = ZonedDateTime.now(ZoneId.of("UTC")).plusMinutes(10)
        val loginCode = generateLoginCode(ADMIN_EMAIL, expiryDateTime)
        val response = client.get("/auth/login?code=$loginCode")

        assertEquals(HttpStatusCode.OK, response.status)
        assert(
            client.cookies("/auth/login").find { it.name == "SESSION" } != null
        )
    }

    @Test
    fun `test login with correct code but non-existent user account`() = testApplication {
        startApplication()  // Needed because the login route uses the DB which depends on EnvConf to be loaded

        val client = createClient {
            install(HttpCookies)
        }

        val expiryDateTime = ZonedDateTime.now(ZoneId.of("UTC")).plusMinutes(10)
        val loginCode = generateLoginCode("me@asadjb.com", expiryDateTime)
        val response = client.get("/auth/login?code=$loginCode")

        assertEquals(HttpStatusCode.NotFound, response.status)
        assert(
            client.cookies("/auth/login").find { it.name == "SESSION" } == null
        )
    }

    @Test
    fun `test login code parsing`() {
        val code = "me@asadjb.com\$12345\$ABCDE"
        val parsed = ParsedLoginCode.tryFromCode(code)

        assertEquals("me@asadjb.com", parsed?.email)
        assertEquals(
            ZonedDateTime.ofInstant(Instant.ofEpochSecond(12345), ZoneId.of("UTC")),
            parsed?.expiry
        )
        assertEquals(
            "ABCDE",
            parsed?.signature
        )

        assertNull(ParsedLoginCode.tryFromCode("Hello!"))
    }

    @Test
    fun `test login code validity check`() = testApplication {
        application {
            assertFalse(
                ParsedLoginCode.tryFromCode("me@asadjb.com\$12345\$ABCDE")!!.isValid()
            )

            val email = "me@asadjb.com"
            var expiry = ZonedDateTime.now(ZoneId.of("UTC")).plusMinutes(10)
            var code = generateLoginCode(email, expiry)
            var parsedCode = ParsedLoginCode.tryFromCode(code)!!
            assert(parsedCode.isValid())

            // Test that code is not valid with an expiry time in the past
            expiry = ZonedDateTime.now(ZoneId.of("UTC")).minusMinutes(10)
            code = generateLoginCode(email, expiry)
            parsedCode = ParsedLoginCode.tryFromCode(code)!!
            assertFalse(parsedCode.isValid())
        }
    }
}