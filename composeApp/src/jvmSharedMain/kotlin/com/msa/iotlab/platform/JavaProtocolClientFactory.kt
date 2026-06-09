package com.msa.iotlab.platform

import com.msa.iotlab.protocol.ProtocolClient
import com.msa.iotlab.protocol.ProtocolClientFactory
import com.msa.iotlab.protocol.ProtocolType

/**
 * JVM-backed protocol factory shared by Android and Desktop targets.
 * WebSocket creation stays abstract because each target chooses a different Ktor engine.
 */
abstract class JavaProtocolClientFactory : ProtocolClientFactory {
    protected abstract fun createWebSocketClient(): ProtocolClient

    override fun create(type: ProtocolType): ProtocolClient = when (type) {
        ProtocolType.MQTT -> HiveMqttProtocolClient()
        ProtocolType.WEBSOCKET -> createWebSocketClient()
        ProtocolType.TCP -> TcpJavaProtocolClient()
        ProtocolType.UDP -> UdpJavaProtocolClient()
    }
}
