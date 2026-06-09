package com.msa.iotlab.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.msa.iotlab.protocol.MessageDirection
import com.msa.iotlab.protocol.ProtocolEvent
import com.msa.iotlab.protocol.ProtocolMessage
import com.msa.iotlab.i18n.t
import com.msa.iotlab.i18n.localizedTitle

/**
 * Rich card renderer for one live or persisted console event.
 */
@Composable
internal fun EventCard(event: ProtocolEvent, modifier: Modifier = Modifier) {
    val title = event.title()
    val detail = event.detail()
    OutlinedCard(modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                StatusBadge(title)
                Text(event.timestampLabel(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(detail, style = MaterialTheme.typography.bodyMedium, fontFamily = FontFamily.Monospace)
            if (event is ProtocolEvent.MessageReceived || event is ProtocolEvent.MessageSent) {
                val message = if (event is ProtocolEvent.MessageReceived) event.message else (event as ProtocolEvent.MessageSent).message
                MessageMeta(message)
            }
        }
    }
}

@Composable
private fun MessageMeta(message: ProtocolMessage) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("${message.payloadSizeBytes} bytes", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
        Text(message.protocol.localizedTitle(), style = MaterialTheme.typography.labelSmall)
        Text(message.direction.name, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun ProtocolEvent.title(): String = when (this) {
    is ProtocolEvent.Connected -> t("CONNECTED", "متصل")
    is ProtocolEvent.Disconnected -> t("DISCONNECTED", "قطع شد")
    is ProtocolEvent.MessageReceived -> t("IN", "دریافتی")
    is ProtocolEvent.MessageSent -> t("OUT", "ارسالی")
    is ProtocolEvent.System -> t("SYSTEM", "سیستمی")
    is ProtocolEvent.Error -> t("ERROR", "خطا")
}

@Composable
private fun ProtocolEvent.detail(): String = when (this) {
    is ProtocolEvent.Connected -> t("Transport connected successfully.", "Transport با موفقیت متصل شد.")
    is ProtocolEvent.Disconnected -> reason ?: t("Transport disconnected.", "Transport قطع شد.")
    is ProtocolEvent.MessageReceived -> message.payloadText.take(1200)
    is ProtocolEvent.MessageSent -> message.payloadText.take(1200)
    is ProtocolEvent.System -> message
    is ProtocolEvent.Error -> message
}

private fun ProtocolEvent.timestampLabel(): String = when (this) {
    is ProtocolEvent.Connected -> timestampMillis.toString()
    is ProtocolEvent.Disconnected -> timestampMillis.toString()
    is ProtocolEvent.MessageReceived -> message.timestampMillis.toString()
    is ProtocolEvent.MessageSent -> message.timestampMillis.toString()
    is ProtocolEvent.System -> timestampMillis.toString()
    is ProtocolEvent.Error -> timestampMillis.toString()
}
