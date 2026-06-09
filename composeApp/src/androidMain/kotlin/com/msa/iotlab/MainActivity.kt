package com.msa.iotlab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import com.msa.iotlab.platform.AndroidProtocolClientFactory
import com.msa.iotlab.platform.getAndroidDatabase

/**
 * Android entry point that provides Android-specific database and protocol engines to shared UI.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val database = remember { getAndroidDatabase(this) }
            val protocolFactory = remember { AndroidProtocolClientFactory() }
            MsaIoTLabApp(database, protocolFactory)
        }
    }
}
