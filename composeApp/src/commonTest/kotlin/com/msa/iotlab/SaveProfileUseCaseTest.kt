package com.msa.iotlab

import com.msa.iotlab.core.AppResult
import com.msa.iotlab.profile.ProfileDraftDefaults
import com.msa.iotlab.profile.SaveProfileUseCase
import com.msa.iotlab.protocol.ProtocolType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * Unit tests for the profile-saving use case boundary used by the editor UI.
 */
class SaveProfileUseCaseTest {
    @Test
    fun validTcpDraftIsSavedToRepository() = runTest {
        val repository = InMemoryProfileRepository()
        val useCase = SaveProfileUseCase(repository)
        val draft = ProfileDraftDefaults.from(null, ProtocolType.TCP)
            .copy(name = "Local TCP", host = "127.0.0.1", port = "9000")

        val result = useCase.save(draft)

        assertIs<AppResult.Success<*>>(result)
        assertEquals("Local TCP", repository.observeProfiles().first().single().name)
    }

    @Test
    fun invalidDraftDoesNotTouchRepository() = runTest {
        val repository = InMemoryProfileRepository()
        val useCase = SaveProfileUseCase(repository)
        val draft = ProfileDraftDefaults.from(null, ProtocolType.TCP)
            .copy(name = "", host = "", port = "bad")

        val result = useCase.save(draft)

        assertIs<AppResult.Error>(result)
        assertEquals(emptyList(), repository.observeProfiles().first())
    }

    @Test
    fun validDraftUsesInjectedTimeAndIdProviders() = runTest {
        val repository = InMemoryProfileRepository()
        val useCase = SaveProfileUseCase(
            repository = repository,
            timeProvider = FixedTimeProvider(555L),
            idProvider = SequentialIdProvider(mutableListOf("profile-id", "mqtt-client-id"))
        )
        val draft = ProfileDraftDefaults.from(null, ProtocolType.MQTT)
            .copy(name = "MQTT Lab", host = "broker.local", mqttClientId = "")

        val result = useCase.save(draft)

        assertIs<AppResult.Success<*>>(result)
        val saved = repository.observeProfiles().first().single()
        assertEquals("profile-id", saved.id)
        assertEquals(555L, saved.createdAt)
        assertEquals(555L, saved.updatedAt)
        assertEquals("msa-mqtt-client-id", saved.mqtt.clientId)
    }

}
