package com.msa.iotlab.platform

import com.msa.iotlab.protocol.ProtocolClient
import com.msa.iotlab.protocol.ProtocolClientFactory
import com.msa.iotlab.protocol.ProtocolType
import com.msa.iotlab.protocol.UnsupportedProtocolClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * iOS protocol factory that provides WebSocket support and safe unsupported clients for native engines.
 */
class IosProtocolClientFactory : ProtocolClientFactory {
    override fun create(type: ProtocolType): ProtocolClient = when (type) {
        ProtocolType.WEBSOCKET -> KtorWebSocketProtocolClient(
            client = HttpClient(Darwin) {
                install(WebSockets)
                install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
            }
        )
        ProtocolType.MQTT -> UnsupportedProtocolClient(type, "MQTT native transport is disabled on iOS in this build; use Android/Desktop for MQTT device testing.")
        ProtocolType.TCP -> UnsupportedProtocolClient(type, "Raw TCP is disabled on iOS in this build; use Android/Desktop for raw socket device testing.")
        ProtocolType.UDP -> UnsupportedProtocolClient(type, "Raw UDP is disabled on iOS in this build; use Android/Desktop for broadcast/datagram device testing.")
    }
}
