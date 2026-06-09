package com.msa.iotlab.core

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Shared coroutine dispatcher contract used by controllers and services.
 * Injecting dispatchers keeps concurrency choices testable and prevents hard-coded threading in business logic.
 */
interface AppDispatchers {
    val default: CoroutineDispatcher
    val io: CoroutineDispatcher
    val main: CoroutineDispatcher
}

/**
 * Production dispatcher provider for shared Kotlin Multiplatform code.
 */
object DefaultAppDispatchers : AppDispatchers {
    override val default: CoroutineDispatcher = Dispatchers.Default
    override val io: CoroutineDispatcher = Dispatchers.IO
    override val main: CoroutineDispatcher = Dispatchers.Main
}
