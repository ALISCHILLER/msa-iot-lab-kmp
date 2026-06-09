package com.msa.iotlab.core

/**
 * Converts blank user input into null for optional profile fields.
 */
fun String.emptyToNull(): String? = trim().takeIf { it.isNotEmpty() }

/**
 * Parses an integer with a deterministic fallback.
 */
fun String.safeInt(default: Int): Int = trim().toIntOrNull() ?: default

/**
 * Parses a long with a deterministic fallback.
 */
fun String.safeLong(default: Long): Long = trim().toLongOrNull() ?: default
