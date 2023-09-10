package com.waraqlabs

import com.waraqlabs.auth.ParsedLoginCode
import com.waraqlabs.auth.generateLoginCode
import de.sharpmind.ktor.EnvConfig
import io.ktor.client.request.*
import io.ktor.client.statement.*
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
        val response = client.get("/auth/generate-login-code/?email=$ADMIN_EMAIL")
        assertEquals(HttpStatusCode.OK, response.status)
        assert(response.bodyAsText().startsWith("$ADMIN_EMAIL\$"))
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
        val response = client.get("/auth/login/?code=$loginCode")

        assertEquals(HttpStatusCode.Forbidden, response.status)
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

            expiry = ZonedDateTime.now(ZoneId.of("UTC")).minusMinutes(10)
            code = generateLoginCode(email, expiry)
            parsedCode = ParsedLoginCode.tryFromCode(code)!!
            assert(!parsedCode.isValid())
        }
    }
}