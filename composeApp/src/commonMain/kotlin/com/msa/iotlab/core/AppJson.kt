package com.msa.iotlab.core

import kotlinx.serialization.json.Json

/**
 * Central JSON configuration used by repositories and import/export code.
 * Keeping this in one place avoids inconsistent parsing behavior across persistence boundaries.
 */
object AppJson {
    val pretty: Json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    val compact: Json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
    }
}
