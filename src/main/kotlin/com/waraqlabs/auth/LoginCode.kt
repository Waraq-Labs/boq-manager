package com.waraqlabs.auth

import de.sharpmind.ktor.EnvConfig
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

val UtcZoneId: ZoneId = ZoneId.of("UTC")
fun utcNow(): ZonedDateTime = ZonedDateTime.now(UtcZoneId)

data class ParsedLoginCode constructor(val email: String, val expiry: ZonedDateTime, val signature: String) {
    companion object {
        fun tryFromCode(code: String): ParsedLoginCode? {
            val parts = code.split("$")
            if (parts.count() != 3) {
                return null
            }

            val epoch = parts[1]
            val expiryDateTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(epoch.toLong()), UtcZoneId)
            return ParsedLoginCode(parts[0], expiryDateTime, parts[2])
        }
    }

    fun isValid(): Boolean {
        val stringToSign = "$email\$${expiry.toEpochSecond()}"
        val expectedSignature = generateSignatureFor(stringToSign)
        val now = utcNow()

        return now.isBefore(expiry) && expectedSignature == signature
    }
}

fun generateLoginCodeWithDefaultExpiry(email: String): String {
    val now = utcNow()
    val codeExpiryTime = now.plusMinutes(
        EnvConfig.getString("loginCodeExpiryMinutes").toLong()
    )

    return generateLoginCode(email, codeExpiryTime)
}

@OptIn(ExperimentalStdlibApi::class)
fun generateSignatureFor(s: String): String {
    val secretKey = SecretKeySpec(EnvConfig.getString("secretKey").toByteArray(Charsets.UTF_8), "HmacSHA1")
    val mac = Mac.getInstance("HmacSHA1")
    mac.init(secretKey)

    val rawHmac = mac.doFinal(s.toByteArray(Charsets.UTF_8))
    return rawHmac.toHexString()
}

fun generateLoginCode(email: String, expiryTime: ZonedDateTime): String {
    // Make sure we use UTC timezone when dealing with dates.
    assert(expiryTime.zone == UtcZoneId)

    val stringToSign = "$email\$${expiryTime.toEpochSecond()}"
    return "$stringToSign\$${generateSignatureFor(stringToSign)}"
}