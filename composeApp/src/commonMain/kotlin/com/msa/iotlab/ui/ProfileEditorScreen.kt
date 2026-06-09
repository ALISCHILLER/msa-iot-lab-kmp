package com.msa.iotlab.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.msa.iotlab.core.AppResult
import com.msa.iotlab.profile.ProfileDraft
import com.msa.iotlab.profile.ProfileDraftDefaults
import com.msa.iotlab.profile.SaveProfileUseCase
import com.msa.iotlab.protocol.ConnectionProfile
import com.msa.iotlab.protocol.ProtocolType
import kotlinx.coroutines.launch
import com.msa.iotlab.i18n.t
import com.msa.iotlab.i18n.localizedTitle

/**
 * Profile editor with desktop two-column sections and domain-driven save behavior.
 */
@Composable
fun ProfileEditorScreen(
    saveProfileUseCase: SaveProfileUseCase,
    initialProfile: ConnectionProfile?,
    initialProtocol: ProtocolType?,
    onCancel: () -> Unit,
    onSaved: (ConnectionProfile) -> Unit
) {
    val initialDraft = remember(initialProfile, initialProtocol) { ProfileDraftDefaults.from(initialProfile, initialProtocol) }
    val state = remember(initialDraft) { initialDraft.toEditorState() }
    val scope = rememberCoroutineScope()
    var validationMessage by remember { mutableStateOf<String?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
        EditorActions(
            status = validationMessage,
            onCancel = onCancel,
            onSave = {
                scope.launch {
                    when (val result = saveProfileUseCase.save(state.toDraft(initialProfile))) {
                        is AppResult.Success -> {
                            validationMessage = null
                            onSaved(result.data)
                        }
                        is AppResult.Error -> validationMessage = result.message
                    }
                }
            }
        )
        AdaptiveTwoPane(
            primaryWeight = 1f,
            secondaryWeight = 1f,
            primary = { CommonProfileFields(state) },
            secondary = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    ProtocolSpecificFields(state)
                    ProfileEditorHelp(state.protocol)
                }
            }
        )
    }
}

@Composable
private fun EditorActions(status: String?, onCancel: () -> Unit, onSave: () -> Unit) {
    SectionCard(t("Profile setup", "تنظیم پروفایل"), t("Configure once, reuse across Android and desktop runs", "یک‌بار تنظیم کنید و در Android و Desktop دوباره استفاده کنید")) {
        ResponsiveActionRow { itemModifier ->
            Button(onClick = onSave, modifier = itemModifier) { Text(t("Save & open console", "ذخیره و باز کردن کنسول")) }
            OutlinedButton(onClick = onCancel, modifier = itemModifier) { Text(t("Cancel", "انصراف")) }
        }
        if (status != null) Text(status)
    }
}

@Composable
private fun ProfileEditorHelp(protocol: ProtocolType) {
    val message = when (protocol) {
        ProtocolType.MQTT -> t("Use publish and subscribe topics for broker-driven IoT testing. Passwords are masked on export by default.", "برای تست IoT مبتنی بر بروکر از topicهای publish و subscribe استفاده کنید. رمزها در export به‌صورت پیش‌فرض مخفی می‌شوند.")
        ProtocolType.WEBSOCKET -> t("Headers must be valid JSON. CR/LF header injection is blocked by domain validation.", "Headerها باید JSON معتبر باشند. تزریق CR/LF توسط اعتبارسنجی دامنه مسدود می‌شود.")
        ProtocolType.TCP -> t("TCP is best for raw device sockets, line endings and request/response payload checks.", "TCP برای socket خام دستگاه، line ending و بررسی payloadهای request/response مناسب است.")
        ProtocolType.UDP -> t("Enable broadcast for discovery packets or listen mode for local datagram telemetry.", "برای packetهای discovery حالت broadcast و برای telemetry محلی حالت listen را فعال کنید.")
    }
    SectionCard(t("Operational note", "نکته عملیاتی"), protocol.localizedTitle()) { Text(message) }
}

private fun ProfileDraft.toEditorState(): ProfileEditorFormState = ProfileEditorFormState(
    initialName = name,
    initialProtocol = protocol,
    initialHost = host,
    initialPort = port,
    initialTls = tlsEnabled,
    initialTimeout = timeoutMillis,
    initialAutoReconnect = autoReconnect,
    initialEncoding = payloadEncoding,
    initialMqttClientId = mqttClientId,
    initialMqttUsername = mqttUsername,
    initialMqttPassword = mqttPassword,
    initialMqttSubTopic = mqttSubscribeTopic,
    initialMqttPubTopic = mqttPublishTopic,
    initialMqttQos = mqttQos,
    initialMqttRetain = mqttRetain,
    initialMqttClean = mqttCleanSession,
    initialMqttKeepAlive = mqttKeepAliveSeconds,
    initialWsPath = wsPath,
    initialWsHeaders = wsHeadersJson,
    initialTcpLineEnding = tcpLineEnding,
    initialTcpBuffer = tcpReadBufferSize,
    initialUdpLocalPort = udpLocalPort,
    initialUdpBroadcast = udpBroadcastEnabled,
    initialUdpListen = udpListenEnabled,
    initialUdpBuffer = udpReadBufferSize
)

private fun ProfileEditorFormState.toDraft(initialProfile: ConnectionProfile?): ProfileDraft = ProfileDraft(
    initialProfile = initialProfile,
    name = name,
    protocol = protocol,
    host = host,
    port = port,
    tlsEnabled = tls,
    timeoutMillis = timeout,
    autoReconnect = autoReconnect,
    payloadEncoding = encoding,
    mqttClientId = mqttClientId,
    mqttUsername = mqttUsername,
    mqttPassword = mqttPassword,
    mqttSubscribeTopic = mqttSubTopic,
    mqttPublishTopic = mqttPubTopic,
    mqttQos = mqttQos,
    mqttRetain = mqttRetain,
    mqttCleanSession = mqttClean,
    mqttKeepAliveSeconds = mqttKeepAlive,
    wsPath = wsPath,
    wsHeadersJson = wsHeaders,
    tcpLineEnding = tcpLineEnding,
    tcpReadBufferSize = tcpBuffer,
    udpLocalPort = udpLocalPort,
    udpBroadcastEnabled = udpBroadcast,
    udpListenEnabled = udpListen,
    udpReadBufferSize = udpBuffer
)
