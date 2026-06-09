package com.msa.iotlab.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.msa.iotlab.core.AppResult
import com.msa.iotlab.payload.DefaultPayloadProvider
import com.msa.iotlab.protocol.PayloadEncoding
import com.msa.iotlab.protocol.PayloadTemplate
import com.msa.iotlab.template.PayloadTemplateDraft
import com.msa.iotlab.template.PayloadTemplateRepository
import com.msa.iotlab.template.SavePayloadTemplateUseCase
import kotlinx.coroutines.launch
import com.msa.iotlab.i18n.t

/**
 * Adaptive payload template workspace with editor, validation feedback and command inventory.
 */
@Composable
fun TemplateScreen(repository: PayloadTemplateRepository, savePayloadTemplateUseCase: SavePayloadTemplateUseCase) {
    val templates by repository.observeAll().collectAsState(emptyList())
    val scope = rememberCoroutineScope()
    val defaultName = t("Device status", "وضعیت دستگاه")
    val readyLabel = t("Ready", "آماده")
    val savedLabel = t("Template saved", "قالب ذخیره شد")
    var name by remember { mutableStateOf(defaultName) }
    var payload by remember { mutableStateOf(DefaultPayloadProvider.forEncoding(PayloadEncoding.JSON)) }
    var status by remember { mutableStateOf(readyLabel) }
    AdaptiveTwoPane(
        primaryWeight = 0.95f,
        secondaryWeight = 1.15f,
        primary = { spec ->
            TemplateEditor(name, { name = it }, payload, { payload = it }, status, spec.singlePane) { draft ->
                scope.launch { save(savePayloadTemplateUseCase, draft, savedLabel, { status = it }, { name = defaultName; payload = DefaultPayloadProvider.forEncoding(PayloadEncoding.JSON) }) }
            }
        },
        secondary = { TemplateList(templates, repository) }
    )
}

@Composable
private fun TemplateEditor(
    name: String,
    onName: (String) -> Unit,
    payload: String,
    onPayload: (String) -> Unit,
    status: String,
    compact: Boolean,
    onSave: (PayloadTemplateDraft) -> Unit
) {
    SectionCard(t("Template editor", "ویرایشگر قالب"), t("Create reusable payloads for console testing", "ساخت payloadهای قابل استفاده مجدد برای تست کنسول")) {
        Text(status)
        OutlinedTextField(name, onName, label = { Text(t("Template name", "نام قالب")) }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(
            payload,
            onPayload,
            label = { Text(t("Payload", "Payload")) },
            modifier = Modifier.fillMaxWidth().heightIn(min = if (compact) 170.dp else 260.dp, max = 440.dp),
            minLines = if (compact) 6 else 10
        )
        Button(onClick = { onSave(PayloadTemplateDraft(name, null, PayloadEncoding.JSON, payload)) }, modifier = Modifier.fillMaxWidth()) { Text(t("Save template", "ذخیره قالب")) }
    }
}

@Composable
private fun TemplateList(templates: List<PayloadTemplate>, repository: PayloadTemplateRepository) {
    val scope = rememberCoroutineScope()
    SectionCard(t("Template library", "کتابخانه قالب"), t("Saved commands and heartbeat bodies", "دستورهای ذخیره‌شده و payloadهای heartbeat")) {
        MetricRow(
            listOf(
                WorkbenchMetric(t("Templates", "قالب‌ها"), templates.size.toString(), t("saved", "ذخیره‌شده")),
                WorkbenchMetric("JSON", templates.count { it.encoding == PayloadEncoding.JSON }.toString(), t("structured", "ساختاریافته")),
                WorkbenchMetric(t("Text", "متن"), templates.count { it.encoding == PayloadEncoding.TEXT }.toString(), t("plain", "ساده")),
                WorkbenchMetric(t("Binary", "باینری"), templates.count { it.encoding == PayloadEncoding.HEX || it.encoding == PayloadEncoding.BASE64 }.toString(), "hex/base64")
            )
        )
        if (templates.isEmpty()) EmptyState(t("No templates", "قالبی وجود ندارد"), t("Save a JSON, text, hex or base64 payload template.", "یک قالب payload از نوع JSON، متن، HEX یا Base64 ذخیره کنید."))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.heightIn(max = 700.dp)) {
            items(templates, key = { it.id }) { item ->
                OutlinedCard(Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ListItem(
                            headlineContent = { Text(item.name) },
                            supportingContent = { Text("${item.encoding} • ${item.payload.take(260)}", fontFamily = FontFamily.Monospace) }
                        )
                        ResponsiveActionRow { itemModifier ->
                            OutlinedButton(onClick = { scope.launch { repository.delete(item.id) } }, modifier = itemModifier) { Text(t("Delete", "حذف")) }
                        }
                    }
                }
            }
        }
    }
}

private suspend fun save(useCase: SavePayloadTemplateUseCase, draft: PayloadTemplateDraft, successMessage: String, setStatus: (String) -> Unit, reset: () -> Unit) {
    when (val result = useCase.save(draft)) {
        is AppResult.Success -> {
            setStatus(successMessage)
            reset()
        }
        is AppResult.Error -> setStatus(result.message)
    }
}
