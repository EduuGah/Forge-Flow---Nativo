package com.forgeflow.core.data.importer

import com.forgeflow.core.model.BodyWeightEntry
import com.forgeflow.core.model.BodyWeightSource
import com.forgeflow.core.model.Weight
import com.forgeflow.core.model.WeightUnit
import java.io.Reader
import java.text.Normalizer
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Locale
import java.util.UUID
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVRecord

internal data class ParsedHevyWorkouts(
    val workouts: List<HevyWorkoutImport>,
    val skippedRows: Int,
)

internal data class HevyWorkoutImport(
    val id: String,
    val title: String,
    val startTime: Instant,
    val endTime: Instant,
    val description: String,
    val rows: List<HevySetImport>,
)

internal data class HevySetImport(
    val sourceRow: Long,
    val exerciseTitle: String,
    val exerciseNotes: String,
    val setType: String,
    val weightGrams: Long,
    val repetitions: Int,
    val rpe: Int?,
    val distanceKilometers: Double?,
    val durationSeconds: Long?,
)

internal data class ParsedHevyMeasurements(
    val entries: List<BodyWeightEntry>,
    val skippedRows: Int,
)

internal object HevyCsvParser {
    private val csvFormat = CSVFormat.DEFAULT.builder()
        .setHeader()
        .setSkipHeaderRecord(true)
        .setTrim(true)
        .get()

    fun parseWorkouts(reader: Reader): ParsedHevyWorkouts {
        val rowsByWorkout = linkedMapOf<String, MutableList<HevySetImport>>()
        val metadata = linkedMapOf<String, WorkoutMetadata>()
        var skippedRows = 0
        csvFormat.parse(reader).use { parser ->
            parser.forEach { record ->
                val start = parseHevyDate(record.value("start_time"))
                val end = parseHevyDate(record.value("end_time"))
                val title = record.value("title").trim()
                val exercise = record.value("exercise_title").trim()
                if (start == null || end == null || !end.isAfter(start) || title.isBlank() ||
                    exercise.isBlank()
                ) {
                    skippedRows += 1
                    return@forEach
                }
                val workoutId = stableId("hevy-workout|$title|${start.toEpochMilli()}|${end.toEpochMilli()}")
                metadata.putIfAbsent(
                    workoutId,
                    WorkoutMetadata(
                        title = title,
                        start = start,
                        end = end,
                        description = record.value("description"),
                    ),
                )
                rowsByWorkout.getOrPut(workoutId, ::mutableListOf) += HevySetImport(
                    sourceRow = record.recordNumber,
                    exerciseTitle = exercise,
                    exerciseNotes = record.value("exercise_notes"),
                    setType = record.value("set_type"),
                    weightGrams = record.value("weight_kg").toDoubleOrNull()
                        ?.takeIf { it >= 0.0 }
                        ?.let { Weight.from(it, WeightUnit.KILOGRAM).grams }
                        ?: 0L,
                    repetitions = record.value("reps").toDoubleOrNull()
                        ?.toInt()
                        ?.coerceAtLeast(0)
                        ?: 0,
                    rpe = record.value("rpe").toDoubleOrNull()
                        ?.toInt()
                        ?.coerceIn(1, 10),
                    distanceKilometers = record.value("distance_km").toDoubleOrNull(),
                    durationSeconds = record.value("duration_seconds").toDoubleOrNull()?.toLong(),
                )
            }
        }
        return ParsedHevyWorkouts(
            workouts = rowsByWorkout.mapNotNull { (id, rows) ->
                metadata[id]?.let { item ->
                    HevyWorkoutImport(
                        id = id,
                        title = item.title,
                        startTime = item.start,
                        endTime = item.end,
                        description = item.description,
                        rows = rows,
                    )
                }
            },
            skippedRows = skippedRows,
        )
    }

    fun parseMeasurements(reader: Reader): ParsedHevyMeasurements {
        val entries = mutableListOf<BodyWeightEntry>()
        var skippedRows = 0
        csvFormat.parse(reader).use { parser ->
            parser.forEach { record ->
                val measuredAt = parseHevyDate(record.value("date"))
                val kilograms = record.value("weight_kg").toDoubleOrNull()
                if (measuredAt == null || kilograms == null || kilograms <= 0.0) {
                    skippedRows += 1
                    return@forEach
                }
                val bodyFat = record.value("fat_percent").toDoubleOrNull()
                    ?.takeIf { it in 1.0..80.0 }
                val weight = Weight.from(kilograms, WeightUnit.KILOGRAM)
                entries += BodyWeightEntry(
                    id = stableId(
                        "hevy-measurement|${measuredAt.toEpochMilli()}|${weight.grams}|$bodyFat",
                    ),
                    weight = weight,
                    measuredAt = measuredAt,
                    bodyFatPercent = bodyFat,
                    source = BodyWeightSource.HEVY,
                )
            }
        }
        return ParsedHevyMeasurements(
            entries = entries.distinctBy(BodyWeightEntry::id),
            skippedRows = skippedRows,
        )
    }

    internal fun parseHevyDate(value: String): Instant? {
        val match = DATE_PATTERN.matchEntire(value.trim()) ?: return null
        val day = match.groupValues[1].toIntOrNull() ?: return null
        val month = MONTHS[match.groupValues[2].normalized()] ?: return null
        val year = match.groupValues[3].toIntOrNull() ?: return null
        val hour = match.groupValues[4].toIntOrNull() ?: return null
        val minute = match.groupValues[5].toIntOrNull() ?: return null
        return runCatching {
            LocalDateTime.of(year, month, day, hour, minute)
                .atZone(ZoneId.systemDefault())
                .toInstant()
        }.getOrNull()
    }

    private fun CSVRecord.value(header: String): String =
        if (isMapped(header) && isSet(header)) get(header).orEmpty() else ""

    private fun String.normalized(): String = Normalizer.normalize(this, Normalizer.Form.NFD)
        .replace("\\p{M}+".toRegex(), "")
        .lowercase(Locale.ROOT)

    private fun stableId(value: String): String = UUID.nameUUIDFromBytes(
        value.toByteArray(Charsets.UTF_8),
    ).toString()

    private data class WorkoutMetadata(
        val title: String,
        val start: Instant,
        val end: Instant,
        val description: String,
    )

    private val DATE_PATTERN = Regex(
        """(\d{1,2})\s+([\p{L}.]+)\s+(\d{4}),\s*(\d{1,2}):(\d{2})""",
    )
    private val MONTHS = mapOf(
        "jan" to 1,
        "fev" to 2,
        "feb" to 2,
        "mar" to 3,
        "abr" to 4,
        "apr" to 4,
        "mai" to 5,
        "may" to 5,
        "jun" to 6,
        "jul" to 7,
        "ago" to 8,
        "aug" to 8,
        "set" to 9,
        "sep" to 9,
        "out" to 10,
        "oct" to 10,
        "nov" to 11,
        "dez" to 12,
        "dec" to 12,
    )
}
