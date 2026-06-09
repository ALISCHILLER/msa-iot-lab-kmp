package com.msa.iotlab.platform

import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient
import com.msa.iotlab.core.IdGenerator
import com.msa.iotlab.core.IdProvider
import com.msa.iotlab.payload.PayloadCodec
import com.msa.iotlab.protocol.BaseProtocolClient
import com.msa.iotlab.protocol.ConnectionProfile
import com.msa.iotlab.protocol.ConnectionState
import com.msa.iotlab.protocol.MessageDirection
import com.msa.iotlab.protocol.OutgoingPayload
import com.msa.iotlab.protocol.ProtocolEvent
import com.msa.iotlab.protocol.ProtocolMessageFactory
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * HiveMQ MQTT v3 implementation for Android/Desktop JVM targets.
 * The class is isolated behind ProtocolClient so the MQTT library can be replaced without UI changes.
 */
class HiveMqttProtocolClient(
    private val idProvider: IdProvider = IdGenerator
) : BaseProtocolClient("MQTT") {
    private var client: Mqtt3AsyncClient? = null

    override suspend fun connect(profile: ConnectionProfile) {
        activeProfile = profile
        updateState(ConnectionState.Connecting)
        runCatching {
            val builder = MqttClient.builder()
                .useMqttVersion3()
                .identifier(profile.mqtt.clientId.ifBlank { "msa-${idProvider.newId()}" })
                .serverHost(profile.host)
                .serverPort(profile.port)
            if (profile.tlsEnabled) builder.sslWithDefaultConfig()
            val mqtt = builder.buildAsync()
            client = mqtt

            val connectBuilder = mqtt.connectWith()
                .cleanSession(profile.mqtt.cleanSession)
                .keepAlive(profile.mqtt.keepAliveSeconds)
            profile.mqtt.username
                ?.takeIf { it.isNotBlank() }
                ?.let { username ->
                    connectBuilder.simpleAuth()
                        .username(username)
                        .password((profile.mqtt.password ?: "").encodeToByteArray())
                        .applySimpleAuth()
                }
            connectBuilder.send().get(profile.timeoutMillis, TimeUnit.MILLISECONDS)
            updateState(ConnectionState.Connected)
            emitConnected("MQTT connected to ${profile.host}:${profile.port}")
            subscribeIfNeeded(profile, mqtt)
        }.onFailure { error ->
            runCatching { client?.disconnect()?.get(1, TimeUnit.SECONDS) }
            client = null
            updateState(ConnectionState.Failed(error.message ?: "MQTT connection failed"))
            emitFailure("connect", error)
        }
    }

    private suspend fun subscribeIfNeeded(profile: ConnectionProfile, mqtt: Mqtt3AsyncClient) {
        val subTopic = profile.mqtt.subscribeTopic?.takeIf { it.isNotBlank() } ?: return
        mqtt.subscribeWith()
            .topicFilter(subTopic)
            .qos(profile.mqtt.qos.toMqttQos())
            .callback { publish ->
                scope.launch {
                    val bytes = publish.payloadAsBytes
                    emit(
                        ProtocolEvent.MessageReceived(
                            ProtocolMessageFactory.fromBytes(
                                profile = profile,
                                direction = MessageDirection.IN,
                                bytes = bytes,
                                metadata = mapOf("topic" to publish.topic.toString())
                            )
                        )
                    )
                }
            }
            .send()
            .await()
        emitSystem("Subscribed to $subTopic")
    }

    override suspend fun send(payload: OutgoingPayload) {
        val profile = requireActiveProfile("publish") ?: return
        val mqtt = client ?: return emitFailure("publish", IllegalStateException("MQTT client is not connected"))
        val topic = profile.mqtt.publishTopic?.takeIf { it.isNotBlank() }
            ?: return emitFailure("publish", IllegalArgumentException("Publish topic is empty"))
        val bytes = PayloadCodec.encode(payload.text, payload.encoding)
        runCatching {
            mqtt.publishWith()
                .topic(topic)
                .qos(profile.mqtt.qos.toMqttQos())
                .retain(profile.mqtt.retain)
                .payload(bytes)
                .send()
                .await()
            emit(
                ProtocolEvent.MessageSent(
                    ProtocolMessageFactory.fromBytes(
                        profile = profile,
                        direction = MessageDirection.OUT,
                        bytes = bytes,
                        metadata = mapOf("topic" to topic)
                    )
                )
            )
        }.onFailure { error ->
            emitFailure("publish", error)
        }
    }

    override suspend fun disconnect() {
        updateState(ConnectionState.Disconnecting("User requested"))
        runCatching { client?.disconnect()?.get(1, TimeUnit.SECONDS) }
        client = null
        updateState(ConnectionState.Disconnected("Closed"))
        emitDisconnected("Closed")
    }
}

private fun Int.toMqttQos(): MqttQos = when (this) {
    1 -> MqttQos.AT_LEAST_ONCE
    2 -> MqttQos.EXACTLY_ONCE
    else -> MqttQos.AT_MOST_ONCE
}
