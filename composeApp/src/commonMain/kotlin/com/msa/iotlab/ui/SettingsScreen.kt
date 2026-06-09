package com.msa.iotlab.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.msa.iotlab.core.AppResult
import com.msa.iotlab.export.ExportWorkspaceUseCase
import com.msa.iotlab.export.ImportWorkspaceUseCase
import kotlinx.coroutines.launch
import com.msa.iotlab.i18n.t

/**
 * Responsive import/export workspace screen for portable JSON bundles and backup workflows.
 */
@Composable
fun SettingsScreen(exportWorkspaceUseCase: ExportWorkspaceUseCase, importWorkspaceUseCase: ImportWorkspaceUseCase) {
    val scope = rememberCoroutineScope()
    val readyLabel = t("Ready", "آماده")
    val exportGeneratedLabel = t("Export generated", "خروجی تولید شد")
    val importCompletedLabel = t("Import completed", "ورود اطلاعات کامل شد")
    var exportText by remember { mutableStateOf("") }
    var importText by remember { mutableStateOf("") }
    var status by remember { mutableStateOf(readyLabel) }
    AdaptiveTwoPane(
        primaryWeight = 1f,
        secondaryWeight = 1f,
        primary = { spec ->
            ExportPanel(
                status = status,
                exportText = exportText,
                onExportText = { exportText = it },
                compact = spec.singlePane,
                onExport = { scope.launch { exportWorkspace(exportWorkspaceUseCase, exportGeneratedLabel, { exportText = it }, { status = it }) } }
            )
        },
        secondary = { spec ->
            ImportPanel(
                importText = importText,
                onImportText = { importText = it },
                compact = spec.singlePane,
                onImport = { scope.launch { importWorkspace(importWorkspaceUseCase, importText, importCompletedLabel) { status = it } } }
            )
        }
    )
}

@Composable
private fun ExportPanel(status: String, exportText: String, onExportText: (String) -> Unit, compact: Boolean, onExport: () -> Unit) {
    SectionCard(t("Export workspace", "خروجی گرفتن از Workspace"), t("Profiles and templates are serialized as masked JSON", "پروفایل‌ها و قالب‌ها به‌صورت JSON با اطلاعات حساس mask شده ذخیره می‌شوند")) {
        Text(status)
        Button(onClick = onExport, modifier = Modifier.fillMaxWidth()) { Text(t("Generate export JSON", "تولید JSON خروجی")) }
        OutlinedTextField(
            exportText,
            onExportText,
            label = { Text(t("Export output", "خروجی Export")) },
            modifier = Modifier.fillMaxWidth().heightIn(min = if (compact) 240.dp else 390.dp, max = 620.dp),
            minLines = if (compact) 8 else 14,
            textStyle = TextStyle(fontFamily = FontFamily.Monospace)
        )
    }
}

@Composable
private fun ImportPanel(importText: String, onImportText: (String) -> Unit, compact: Boolean, onImport: () -> Unit) {
    SectionCard(t("Import workspace", "ورود اطلاعات Workspace"), t("Paste a validated export bundle to restore local data", "برای بازیابی داده محلی، یک bundle معتبر export شده را وارد کنید")) {
        OutlinedButton(onClick = onImport, modifier = Modifier.fillMaxWidth()) { Text(t("Import JSON", "ورود JSON")) }
        OutlinedTextField(
            importText,
            onImportText,
            label = { Text(t("Import input", "ورودی Import")) },
            modifier = Modifier.fillMaxWidth().heightIn(min = if (compact) 260.dp else 440.dp, max = 700.dp),
            minLines = if (compact) 9 else 16,
            textStyle = TextStyle(fontFamily = FontFamily.Monospace)
        )
    }
}

private suspend fun exportWorkspace(useCase: ExportWorkspaceUseCase, successMessage: String, setExport: (String) -> Unit, setStatus: (String) -> Unit) {
    when (val result = useCase.exportJson()) {
        is AppResult.Success -> {
            setExport(result.data)
            setStatus(successMessage)
        }
        is AppResult.Error -> setStatus(result.message)
    }
}

private suspend fun importWorkspace(useCase: ImportWorkspaceUseCase, importText: String, successMessage: String, setStatus: (String) -> Unit) {
    when (val result = useCase.importJson(importText)) {
        is AppResult.Success -> setStatus(successMessage)
        is AppResult.Error -> setStatus(result.message)
    }
}
