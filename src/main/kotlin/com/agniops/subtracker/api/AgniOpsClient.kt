package com.agniops.subtracker.api

import burp.api.montoya.MontoyaApi
import burp.api.montoya.http.HttpService
import burp.api.montoya.http.message.requests.HttpRequest
import com.google.gson.Gson

class AgniOpsClient(private val api: MontoyaApi) {

    private val gson = Gson()
    private val apiService = HttpService.httpService("app.agniops.in", 443, true)

    companion object {
        private const val API_PATH = "/api/v1/subdomains/scan"
        private const val USER_AGENT = "SubTracker-BurpSuite-Kotlin/1.0.0"
    }

    fun scanSubdomains(domain: String, apiKey: String): ScanResult {
        val payload = gson.toJson(mapOf("domain" to domain))

        val httpRequest = HttpRequest.httpRequest(
            apiService,
            "POST $API_PATH HTTP/1.1\r\n" +
            "Host: app.agniops.in\r\n" +
            "Content-Type: application/json\r\n" +
            "X-API-Key: $apiKey\r\n" +
            "User-Agent: $USER_AGENT\r\n" +
            "Connection: close\r\n\r\n" +
            payload
        )

        val responseReceived = api.http().sendRequest(httpRequest)
        val response = responseReceived.response() 
            ?: throw Exception("No response received from AgniOps API.")

        val statusCode = response.statusCode().toInt()
        if (statusCode != 200) {
            val errorMsg = when (statusCode) {
                400 -> "Bad request - check domain format."
                401 -> "Authentication failed - check your API key."
                403 -> "Access denied - check your API key permissions."
                408 -> "Request timed out."
                429 -> "Rate limit / Quota exceeded."
                500, 503 -> "AgniOps server error. Try again later."
                else -> "HTTP Error $statusCode"
            }
            throw Exception(errorMsg)
        }

        val bodyString = response.bodyToString()
        return try {
            gson.fromJson(bodyString, ScanResult::class.java)
                ?: throw Exception("Empty JSON response.")
        } catch (e: Exception) {
            throw Exception("Failed to parse API response: ${e.message}")
        }
    }
}
