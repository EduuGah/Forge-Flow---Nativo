package com.forgeflow.core.data.importer

import java.time.Instant

enum class HevyImportFileType {
    WORKOUTS,
    MEASUREMENTS,
}

data class HevyImportPreview(
    val fileName: String,
    val type: HevyImportFileType,
    val recordCount: Int,
    val skippedRowCount: Int,
    val earliestAt: Instant?,
    val latestAt: Instant?,
)

data class HevyImportResult(
    val workoutsImported: Int,
    val workoutsAlreadyImported: Int,
    val measurementsImported: Int,
    val measurementsAlreadyImported: Int,
    val skippedRows: Int,
)
