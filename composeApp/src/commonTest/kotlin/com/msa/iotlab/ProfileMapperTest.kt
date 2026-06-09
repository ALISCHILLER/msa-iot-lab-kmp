package com.msa.iotlab

import com.msa.iotlab.core.AppClock
import com.msa.iotlab.core.IdGenerator
import com.msa.iotlab.profile.toDomain
import com.msa.iotlab.profile.toEntity
import com.msa.iotlab.protocol.ConnectionProfile
import com.msa.iotlab.protocol.ProtocolType
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Unit tests for profile domain/entity mapping and protocol-specific JSON options.
 */
class ProfileMapperTest {
    @Test
    fun profileRoundTripPreservesProtocolAndHost() {
        val now = AppClock.nowMillis()
        val profile = ConnectionProfile(
            id = IdGenerator.newId(),
            name = "Test MQTT",
            protocol = ProtocolType.MQTT,
            host = "localhost",
            port = 1883,
            createdAt = now,
            updatedAt = now
        )
        val json = Json { ignoreUnknownKeys = true; prettyPrint = true }
        val roundTrip = profile.toEntity(json).toDomain(json)

        assertEquals(profile.protocol, roundTrip.protocol)
        assertEquals(profile.host, roundTrip.host)
        assertEquals(profile.port, roundTrip.port)
    }
}
