package com.msa.iotlab.protocol

import kotlinx.serialization.Serializable

/**
 * Supported transport protocols and their default ports for quick profile creation.
 */
@Serializable
enum class ProtocolType(val title: String, val defaultPort: Int) {
    MQTT("MQTT", 1883),
    WEBSOCKET("WebSocket", 80),
    TCP("TCP Socket", 9000),
    UDP("UDP Socket", 9000)
}

/**
 * User-facing payload encodings supported by the console and protocol clients.
 */
@Serializable
enum class PayloadEncoding { TEXT, JSON, HEX, BASE64 }

/**
 * Direction or category assigned to a persisted protocol message.
 */
@Serializable
enum class MessageDirection { IN, OUT, SYSTEM, ERROR }

/**
 * Lifecycle status for a console test session.
 */
@Serializable
enum class SessionStatus { RUNNING, FINISHED, FAILED, CANCELLED }
