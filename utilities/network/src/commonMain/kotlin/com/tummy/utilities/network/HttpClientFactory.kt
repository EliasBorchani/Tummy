package com.tummy.utilities.network

import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

data class NetworkConfig(
    val baseUrl: String,
    val enableLogging: Boolean = true,
)

expect fun httpClientEngine(): HttpClientEngine

fun createHttpClient(
    config: NetworkConfig,
    extraConfig: HttpClientConfig<*>.() -> Unit = {},
): HttpClient = HttpClient(httpClientEngine()) {
    expectSuccess = true

    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
            },
        )
    }

    if (config.enableLogging) {
        install(Logging) {
            level = LogLevel.INFO
            logger = object : Logger {
                override fun log(message: String) {
                    Napier.d(tag = "Http", message = message)
                }
            }
        }
    }

    defaultRequest {
        url(config.baseUrl)
        contentType(ContentType.Application.Json)
    }

    extraConfig()
}
