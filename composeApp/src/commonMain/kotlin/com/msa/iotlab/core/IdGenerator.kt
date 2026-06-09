package com.msa.iotlab.core

import com.benasher44.uuid.uuid4

/**
 * Shared UUID generator used for profiles, sessions, logs and templates.
 */
object IdGenerator : IdProvider {
    override fun newId(): String = uuid4().toString()
}
