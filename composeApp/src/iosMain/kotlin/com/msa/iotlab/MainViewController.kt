package com.msa.iotlab

import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import com.msa.iotlab.platform.IosProtocolClientFactory
import com.msa.iotlab.platform.getIosDatabase

/**
 * iOS entry point that hosts the shared Compose UI inside a UIViewController.
 */
fun MainViewController() = ComposeUIViewController {
    val database = remember { getIosDatabase() }
    val protocolFactory = remember { IosProtocolClientFactory() }
    MsaIoTLabApp(database, protocolFactory)
}
