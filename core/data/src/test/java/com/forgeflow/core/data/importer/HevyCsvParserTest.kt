package com.forgeflow.core.data.importer

import com.forgeflow.core.model.BodyWeightSource
import java.io.StringReader
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class HevyCsvParserTest {
    @Test
    fun measurements_supportPortugueseMonthsAndSkipInvalidRows() {
        val csv = """
            "date","weight_kg","fat_percent","neck_cm"
            "28 Jan 2026, 00:00",75,18.2,
            "3 Fev 2026, 00:00",76.2,,
            "data inválida",80,,
        """.trimIndent()

        val parsed = HevyCsvParser.parseMeasurements(StringReader(csv))

        assertEquals(2, parsed.entries.size)
        assertEquals(1, parsed.skippedRows)
        assertEquals(75_000L, parsed.entries.first().weight.grams)
        assertEquals(BodyWeightSource.HEVY, parsed.entries.first().source)
        assertNotNull(parsed.entries.first().measuredAt)
    }

    @Test
    fun workouts_groupRowsAndPreserveQuotedNotes() {
        val csv = """
            "title","start_time","end_time","description","exercise_title","superset_id","exercise_notes","set_index","set_type","weight_kg","reps","distance_km","duration_seconds","rpe"
            "Upper B","30 Jul 2026, 19:37","30 Jul 2026, 21:21","Bom treino","Supino Inclinado (Halter)",,"Pesado, mas válido",0,"warmup",44,15,,,6
            "Upper B","30 Jul 2026, 19:37","30 Jul 2026, 21:21","Bom treino","Supino Inclinado (Halter)",,"Pesado, mas válido",1,"normal",50,8,,,8
            "Upper B","30 Jul 2026, 19:37","30 Jul 2026, 21:21","Bom treino","Remada (Barra)",,,0,"normal",60,10,,,7
        """.trimIndent()

        val parsed = HevyCsvParser.parseWorkouts(StringReader(csv))

        assertEquals(1, parsed.workouts.size)
        assertEquals(3, parsed.workouts.single().rows.size)
        assertEquals("warmup", parsed.workouts.single().rows.first().setType)
        assertEquals("Pesado, mas válido", parsed.workouts.single().rows.first().exerciseNotes)
        assertEquals(0, parsed.skippedRows)
    }
}
