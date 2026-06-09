package com.msa.iotlab.platform

import com.msa.iotlab.protocol.ProtocolClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Desktop protocol factory that combines JVM socket/MQTT clients with the CIO WebSocket engine.
 */
class DesktopProtocolClientFactory : JavaProtocolClientFactory() {
    override fun createWebSocketClient(): ProtocolClient = KtorWebSocketProtocolClient(
        client = HttpClient(CIO) {
            install(WebSockets)
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }
    )
}
