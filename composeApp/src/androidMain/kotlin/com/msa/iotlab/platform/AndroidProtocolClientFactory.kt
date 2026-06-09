package com.msa.iotlab.platform

import com.msa.iotlab.protocol.ProtocolClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Android protocol factory that combines JVM socket/MQTT clients with the OkHttp WebSocket engine.
 */
class AndroidProtocolClientFactory : JavaProtocolClientFactory() {
    override fun createWebSocketClient(): ProtocolClient = KtorWebSocketProtocolClient(
        client = HttpClient(OkHttp) {
            install(WebSockets)
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }
    )
}
