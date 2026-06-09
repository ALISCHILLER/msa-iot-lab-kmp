package com.msa.iotlab.core

/**
 * Abstraction for reading the current time from application code.
 * Injecting this provider keeps factories, use cases and tests deterministic.
 */
interface TimeProvider {
    fun nowMillis(): Long
}

/**
 * Abstraction for creating stable unique identifiers.
 * Production uses random UUIDs while tests can inject fixed or sequential IDs.
 */
interface IdProvider {
    fun newId(): String
}
