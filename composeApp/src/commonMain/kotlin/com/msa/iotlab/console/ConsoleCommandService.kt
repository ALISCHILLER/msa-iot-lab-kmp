package com.msa.iotlab.console

import com.msa.iotlab.core.AppClock
import com.msa.iotlab.core.AppResult
import com.msa.iotlab.core.IdGenerator
import com.msa.iotlab.core.IdProvider
import com.msa.iotlab.core.TimeProvider
import com.msa.iotlab.payload.JsonFormatter
import com.msa.iotlab.payload.PayloadCodec
import com.msa.iotlab.payload.PayloadSizePolicy
import com.msa.iotlab.payload.PayloadVariables
import com.msa.iotlab.protocol.OutgoingPayload
import com.msa.iotlab.protocol.PayloadEncoding

/**
 * Builds and validates outgoing console commands before they reach a protocol client.
 * The service keeps payload validation, variable expansion and encoding checks outside Compose UI.
 */
object ConsoleCommandService {
    fun createOutgoingPayload(
        rawPayload: String,
        encoding: PayloadEncoding,
        counter: Long,
        timeProvider: TimeProvider = AppClock,
        idProvider: IdProvider = IdGenerator
    ): AppResult<OutgoingPayload> {
        val expanded = PayloadVariables.apply(rawPayload, counter, timeProvider, idProvider)
        return runCatching {
            val normalized = if (encoding == PayloadEncoding.JSON) JsonFormatter.minify(expanded) else expanded
            val bytes = PayloadCodec.encode(normalized, encoding)
            PayloadSizePolicy.validate(bytes.size)
            OutgoingPayload(normalized, encoding)
        }.fold(
            onSuccess = { AppResult.Success(it) },
            onFailure = { AppResult.Error("Invalid ${encoding.name} payload: ${it.message}", it) }
        )
    }
}
