package com.msa.iotlab

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import com.msa.iotlab.platform.DesktopProtocolClientFactory
import com.msa.iotlab.platform.getDesktopDatabase

/**
 * Desktop JVM entry point that starts the shared Compose app in a large workbench window.
 */
fun main() = application {
    val database = getDesktopDatabase()
    val protocolFactory = DesktopProtocolClientFactory()
    Window(
        onCloseRequest = ::exitApplication,
        title = "MSA IoT Lab",
        state = WindowState(size = DpSize(width = 1440.dp, height = 920.dp))
    ) {
        MsaIoTLabApp(database, protocolFactory)
    }
}
