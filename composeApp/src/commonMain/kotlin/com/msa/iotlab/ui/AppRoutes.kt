package com.msa.iotlab.ui

import com.msa.iotlab.protocol.ConnectionProfile
import com.msa.iotlab.protocol.ProtocolType

/**
 * In-memory navigation model for the shared Compose application.
 */
sealed interface AppRoute {
    /** Landing page with protocol cards and quick navigation. */
    data object Dashboard : AppRoute

    /** Saved connection profile list screen. */
    data object Profiles : AppRoute

    /** Profile creation/editing screen with optional protocol preselection. */
    data class EditProfile(val profile: ConnectionProfile? = null, val protocol: ProtocolType? = null) : AppRoute

    /** Live console bound to a concrete saved profile. */
    data class Console(val profile: ConnectionProfile) : AppRoute

    /** Session and message history screen. */
    data object History : AppRoute

    /** Built-in operator guide for onboarding and troubleshooting. */
    data object Guide : AppRoute

    /** Payload template management screen. */
    data object Templates : AppRoute

    /** Import/export and app configuration screen. */
    data object Settings : AppRoute
}
