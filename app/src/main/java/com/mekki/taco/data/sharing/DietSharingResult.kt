package com.mekki.taco.data.sharing

import com.mekki.taco.data.db.entity.Diet

/**
 * Result of an export operation.
 */
sealed class ExportResult {
    data class Success(val json: String, val fileName: String) : ExportResult()
    data class Error(val message: String, val exception: Throwable? = null) : ExportResult()
}

/**
 * Result of an import operation.
 */
sealed class ImportResult {
    data class Success(
        val diet: Diet,
        val itemsImported: Int,
        val customFoodsImported: Int,
        val customFoodsSkipped: Int  // Already existed locally
    ) : ImportResult()

    data class Error(val message: String, val exception: Throwable? = null) : ImportResult()
}

/**
 * Handles a VERY rare and highly unlikely case of conflict
 */
enum class ConflictResolution {
    /** Keep local version, ignore incoming */
    KEEP_LOCAL,

    /** Replace local with incoming */
    REPLACE_WITH_INCOMING,

    /** Keep both (incoming gets new UUID) */
    KEEP_BOTH,

    /** Ask user for each conflict */
    ASK_USER
}