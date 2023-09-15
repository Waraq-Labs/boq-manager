package com.waraqlabs.boq_manager.auth.test_utils

import com.waraqlabs.boq_manager.auth.generateLoginCode
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import java.time.ZoneId
import java.time.ZonedDateTime

suspend fun loginWithClient(email: String, client: HttpClient): HttpResponse {
    val expiryDateTime = ZonedDateTime.now(ZoneId.of("UTC")).plusMinutes(10)
    val loginCode = generateLoginCode(email, expiryDateTime)
    return client.get("/auth/login?code=$loginCode")
}

suspend fun loginAsAdmin(client: HttpClient) = loginWithClient("admin@jhuengineering.com", client)
suspend fun loginAsProjectManager(client: HttpClient) = loginWithClient("pm@jhuengineering.com", client)
suspend fun loginAsClient(client: HttpClient) = loginWithClient("john@client.com", client)