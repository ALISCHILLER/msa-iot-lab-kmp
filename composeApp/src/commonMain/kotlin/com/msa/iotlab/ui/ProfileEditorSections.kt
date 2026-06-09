package com.msa.iotlab.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.msa.iotlab.protocol.PayloadEncoding
import com.msa.iotlab.protocol.ProtocolType
import com.msa.iotlab.i18n.t
import com.msa.iotlab.i18n.localizedTitle

/**
 * Mutable UI state for the profile editor form before domain validation.
 */
class ProfileEditorFormState(
    initialName: String,
    initialProtocol: ProtocolType,
    initialHost: String,
    initialPort: String,
    initialTls: Boolean,
    initialTimeout: String,
    initialAutoReconnect: Boolean,
    initialEncoding: PayloadEncoding,
    initialMqttClientId: String,
    initialMqttUsername: String,
    initialMqttPassword: String,
    initialMqttSubTopic: String,
    initialMqttPubTopic: String,
    initialMqttQos: String,
    initialMqttRetain: Boolean,
    initialMqttClean: Boolean,
    initialMqttKeepAlive: String,
    initialWsPath: String,
    initialWsHeaders: String,
    initialTcpLineEnding: String,
    initialTcpBuffer: String,
    initialUdpLocalPort: String,
    initialUdpBroadcast: Boolean,
    initialUdpListen: Boolean,
    initialUdpBuffer: String
) {
    var name by mutableStateOf(initialName)
    var protocol by mutableStateOf(initialProtocol)
    var host by mutableStateOf(initialHost)
    var port by mutableStateOf(initialPort)
    var tls by mutableStateOf(initialTls)
    var timeout by mutableStateOf(initialTimeout)
    var autoReconnect by mutableStateOf(initialAutoReconnect)
    var encoding by mutableStateOf(initialEncoding)
    var mqttClientId by mutableStateOf(initialMqttClientId)
    var mqttUsername by mutableStateOf(initialMqttUsername)
    var mqttPassword by mutableStateOf(initialMqttPassword)
    var mqttSubTopic by mutableStateOf(initialMqttSubTopic)
    var mqttPubTopic by mutableStateOf(initialMqttPubTopic)
    var mqttQos by mutableStateOf(initialMqttQos)
    var mqttRetain by mutableStateOf(initialMqttRetain)
    var mqttClean by mutableStateOf(initialMqttClean)
    var mqttKeepAlive by mutableStateOf(initialMqttKeepAlive)
    var wsPath by mutableStateOf(initialWsPath)
    var wsHeaders by mutableStateOf(initialWsHeaders)
    var tcpLineEnding by mutableStateOf(initialTcpLineEnding)
    var tcpBuffer by mutableStateOf(initialTcpBuffer)
    var udpLocalPort by mutableStateOf(initialUdpLocalPort)
    var udpBroadcast by mutableStateOf(initialUdpBroadcast)
    var udpListen by mutableStateOf(initialUdpListen)
    var udpBuffer by mutableStateOf(initialUdpBuffer)
}

/**
 * Generic connection settings shared by all protocols.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommonProfileFields(state: ProfileEditorFormState) {
    var protocolMenu by remember { mutableStateOf(false) }
    var encodingMenu by remember { mutableStateOf(false) }
    SectionCard(t("Connection", "اتصال"), t("Name, endpoint and shared transport behavior", "نام، endpoint و رفتار transport مشترک")) {
        OutlinedTextField(state.name, { state.name = it }, label = { Text(t("Profile name", "نام پروفایل")) }, modifier = Modifier.fillMaxWidth())
        ExposedDropdownMenuBox(expanded = protocolMenu, onExpandedChange = { protocolMenu = !protocolMenu }) {
            OutlinedTextField(
                value = state.protocol.localizedTitle(),
                onValueChange = {},
                readOnly = true,
                label = { Text(t("Protocol", "پروتکل")) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(protocolMenu) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = protocolMenu, onDismissRequest = { protocolMenu = false }) {
                ProtocolType.entries.forEach { item ->
                    DropdownMenuItem(text = { Text(item.localizedTitle()) }, onClick = {
                        state.protocol = item
                        state.port = item.defaultPort.toString()
                        protocolMenu = false
                    })
                }
            }
        }
        ResponsiveActionRow { itemModifier ->
            OutlinedTextField(state.host, { state.host = it }, label = { Text(t("Host / IP", "Host / IP")) }, modifier = itemModifier)
            OutlinedTextField(state.port, { state.port = it }, label = { Text(t("Port", "پورت")) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = itemModifier)
        }
        OutlinedTextField(state.timeout, { state.timeout = it }, label = { Text(t("Timeout millis", "Timeout بر حسب میلی‌ثانیه")) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
        ResponsiveActionRow {
            CheckRow(t("TLS / SSL", "TLS / SSL"), state.tls) { state.tls = it }
            CheckRow(t("Auto reconnect", "اتصال مجدد خودکار"), state.autoReconnect) { state.autoReconnect = it }
        }
        ExposedDropdownMenuBox(expanded = encodingMenu, onExpandedChange = { encodingMenu = !encodingMenu }) {
            OutlinedTextField(
                value = state.encoding.localizedTitle(),
                onValueChange = {},
                readOnly = true,
                label = { Text(t("Default payload encoding", "نوع پیش‌فرض Payload")) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(encodingMenu) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = encodingMenu, onDismissRequest = { encodingMenu = false }) {
                PayloadEncoding.entries.forEach { item -> DropdownMenuItem(text = { Text(item.localizedTitle()) }, onClick = { state.encoding = item; encodingMenu = false }) }
            }
        }
    }
}

/**
 * Protocol-specific settings for the currently selected protocol.
 */
@Composable
fun ProtocolSpecificFields(state: ProfileEditorFormState) {
    when (state.protocol) {
        ProtocolType.MQTT -> MqttFields(state)
        ProtocolType.WEBSOCKET -> WebSocketFields(state)
        ProtocolType.TCP -> TcpFields(state)
        ProtocolType.UDP -> UdpFields(state)
    }
}

@Composable
private fun MqttFields(state: ProfileEditorFormState) {
    SectionCard("MQTT", t("Broker authentication, publish/subscribe topics and session flags", "احراز هویت بروکر، topicهای publish/subscribe و تنظیمات session")) {
        OutlinedTextField(state.mqttClientId, { state.mqttClientId = it }, label = { Text(t("Client ID", "شناسه کلاینت")) }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(state.mqttUsername, { state.mqttUsername = it }, label = { Text(t("Username", "نام کاربری")) }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(state.mqttPassword, { state.mqttPassword = it }, label = { Text(t("Password", "رمز عبور")) }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        OutlinedTextField(state.mqttSubTopic, { state.mqttSubTopic = it }, label = { Text(t("Subscribe topic", "Topic دریافت")) }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(state.mqttPubTopic, { state.mqttPubTopic = it }, label = { Text(t("Publish topic", "Topic ارسال")) }, modifier = Modifier.fillMaxWidth())
        ResponsiveActionRow { itemModifier ->
            OutlinedTextField(state.mqttQos, { state.mqttQos = it }, label = { Text("QoS") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = itemModifier)
            OutlinedTextField(state.mqttKeepAlive, { state.mqttKeepAlive = it }, label = { Text(t("Keep alive", "Keep alive")) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = itemModifier)
        }
        ResponsiveActionRow {
            CheckRow(t("Retain", "Retain"), state.mqttRetain) { state.mqttRetain = it }
            CheckRow(t("Clean session", "Clean session"), state.mqttClean) { state.mqttClean = it }
        }
    }
}

@Composable
private fun WebSocketFields(state: ProfileEditorFormState) = SectionCard(t("WebSocket", "وب‌سوکت"), t("Path and optional JSON headers", "مسیر و headerهای اختیاری JSON")) {
    OutlinedTextField(state.wsPath, { state.wsPath = it }, label = { Text(t("Path, e.g. /ws", "مسیر، مثل /ws")) }, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(state.wsHeaders, { state.wsHeaders = it }, label = { Text(t("Headers JSON", "Headerهای JSON")) }, modifier = Modifier.fillMaxWidth())
}

@Composable
private fun TcpFields(state: ProfileEditorFormState) = SectionCard("TCP", t("Raw socket stream behavior", "رفتار stream خام socket")) {
    OutlinedTextField(state.tcpLineEnding, { state.tcpLineEnding = it }, label = { Text(t("Line ending: NONE, LF, CRLF", "پایان خط: NONE, LF, CRLF")) }, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(state.tcpBuffer, { state.tcpBuffer = it }, label = { Text(t("Read buffer size", "اندازه بافر خواندن")) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
}

@Composable
private fun UdpFields(state: ProfileEditorFormState) = SectionCard("UDP", t("Datagram binding, broadcast and listener options", "تنظیمات bind، broadcast و listener برای datagram")) {
    OutlinedTextField(state.udpLocalPort, { state.udpLocalPort = it }, label = { Text(t("Local bind port, optional", "پورت bind محلی، اختیاری")) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
    OutlinedTextField(state.udpBuffer, { state.udpBuffer = it }, label = { Text(t("Read buffer size", "اندازه بافر خواندن")) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
    ResponsiveActionRow {
        CheckRow(t("Broadcast", "Broadcast"), state.udpBroadcast) { state.udpBroadcast = it }
        CheckRow(t("Listen", "Listen"), state.udpListen) { state.udpListen = it }
    }
}

@Composable
private fun CheckRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Checkbox(checked, onCheckedChange)
        Text(label)
    }
}
