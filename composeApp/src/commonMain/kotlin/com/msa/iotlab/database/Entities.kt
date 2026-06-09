package com.msa.iotlab.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for saved protocol connection profiles.
 * Protocol-specific settings are stored as JSON to keep schema evolution manageable.
 */
@Entity(
    tableName = "connection_profiles",
    indices = [Index("protocol"), Index("host"), Index("updatedAt")]
)
data class ProfileEntity(
    @PrimaryKey val id: String,
    val name: String,
    val protocol: String,
    val host: String,
    val port: Int,
    val tlsEnabled: Boolean,
    val timeoutMillis: Long,
    val autoReconnect: Boolean,
    val payloadEncoding: String,
    val createdAt: Long,
    val updatedAt: Long,
    val optionsJson: String
)

/**
 * Room entity representing one console testing session.
 */
@Entity(
    tableName = "sessions",
    indices = [Index("profileId"), Index("protocol"), Index("startedAt"), Index("status")]
)
data class SessionEntity(
    @PrimaryKey val id: String,
    val profileId: String?,
    val protocol: String,
    val name: String,
    val startedAt: Long,
    val endedAt: Long?,
    val status: String
)

/**
 * Room entity for persisted protocol traffic and system/error console messages.
 */
@Entity(
    tableName = "message_logs",
    indices = [Index("sessionId"), Index("profileId"), Index("timestamp"), Index("direction"), Index("protocol")]
)
data class MessageLogEntity(
    @PrimaryKey val id: String,
    val sessionId: String?,
    val profileId: String?,
    val protocol: String,
    val direction: String,
    val payloadText: String,
    val payloadHex: String,
    val payloadSizeBytes: Int,
    val timestamp: Long,
    val metadataJson: String
)

/**
 * Room entity for reusable payload snippets shown in the live console.
 */
@Entity(
    tableName = "payload_templates",
    indices = [Index("protocol"), Index("updatedAt"), Index("name")]
)
data class PayloadTemplateEntity(
    @PrimaryKey val id: String,
    val name: String,
    val protocol: String?,
    val encoding: String,
    val payload: String,
    val createdAt: Long,
    val updatedAt: Long
)

/**
 * Room entity for lightweight key-value application settings.
 */
@Entity(tableName = "app_settings")
data class AppSettingEntity(
    @PrimaryKey val key: String,
    val value: String,
    val updatedAt: Long
)
