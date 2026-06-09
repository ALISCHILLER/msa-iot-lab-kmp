package com.msa.iotlab.export

/**
 * Declares how sensitive values should be handled when the workspace is exported.
 * The safe default redacts secrets so exported JSON can be shared in tickets and reviews.
 */
data class ExportSecurityPolicy(
    val includeSecrets: Boolean = false
)
