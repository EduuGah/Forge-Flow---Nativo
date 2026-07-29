package com.forgeflow.core.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object MigrationRegistry {
    private val migration1To2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            createRoutineTables(database)
            createWorkoutTables(database)
        }
    }

    val all: Array<Migration> = arrayOf(migration1To2)

    private fun createRoutineTables(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `routines` (
                `id` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                `description` TEXT NOT NULL,
                `created_at_epoch_millis` INTEGER NOT NULL,
                `updated_at_epoch_millis` INTEGER NOT NULL,
                `archived_at_epoch_millis` INTEGER,
                PRIMARY KEY(`id`)
            )
            """.trimIndent(),
        )
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_routines_name` ON `routines` (`name`)")
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `routine_exercises` (
                `id` TEXT NOT NULL,
                `routine_id` TEXT NOT NULL,
                `exercise_id` TEXT NOT NULL,
                `position` INTEGER NOT NULL,
                `notes` TEXT NOT NULL,
                `default_rest_seconds` INTEGER NOT NULL,
                `planned_repetitions_minimum` INTEGER,
                `planned_repetitions_maximum` INTEGER,
                `planned_sets` INTEGER NOT NULL,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`routine_id`) REFERENCES `routines`(`id`)
                    ON UPDATE NO ACTION ON DELETE CASCADE,
                FOREIGN KEY(`exercise_id`) REFERENCES `exercises`(`id`)
                    ON UPDATE NO ACTION ON DELETE NO ACTION
            )
            """.trimIndent(),
        )
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_routine_exercises_routine_id` " +
                "ON `routine_exercises` (`routine_id`)",
        )
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_routine_exercises_exercise_id` " +
                "ON `routine_exercises` (`exercise_id`)",
        )
        database.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_routine_exercises_routine_id_position` " +
                "ON `routine_exercises` (`routine_id`, `position`)",
        )
    }

    private fun createWorkoutTables(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `workout_sessions` (
                `id` TEXT NOT NULL,
                `routine_id` TEXT,
                `name` TEXT NOT NULL,
                `started_at_epoch_millis` INTEGER NOT NULL,
                `finished_at_epoch_millis` INTEGER,
                `status` TEXT NOT NULL,
                `notes` TEXT NOT NULL,
                `created_at_epoch_millis` INTEGER NOT NULL,
                `updated_at_epoch_millis` INTEGER NOT NULL,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`routine_id`) REFERENCES `routines`(`id`)
                    ON UPDATE NO ACTION ON DELETE SET NULL
            )
            """.trimIndent(),
        )
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_workout_sessions_routine_id` " +
                "ON `workout_sessions` (`routine_id`)",
        )
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_workout_sessions_status` " +
                "ON `workout_sessions` (`status`)",
        )
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_workout_sessions_finished_at_epoch_millis` " +
                "ON `workout_sessions` (`finished_at_epoch_millis`)",
        )
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `workout_session_exercises` (
                `id` TEXT NOT NULL,
                `session_id` TEXT NOT NULL,
                `exercise_id` TEXT,
                `exercise_name_snapshot` TEXT NOT NULL,
                `muscle_group_snapshot` TEXT NOT NULL,
                `position` INTEGER NOT NULL,
                `notes` TEXT NOT NULL,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`session_id`) REFERENCES `workout_sessions`(`id`)
                    ON UPDATE NO ACTION ON DELETE CASCADE,
                FOREIGN KEY(`exercise_id`) REFERENCES `exercises`(`id`)
                    ON UPDATE NO ACTION ON DELETE SET NULL
            )
            """.trimIndent(),
        )
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_workout_session_exercises_session_id` " +
                "ON `workout_session_exercises` (`session_id`)",
        )
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_workout_session_exercises_exercise_id` " +
                "ON `workout_session_exercises` (`exercise_id`)",
        )
        database.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS " +
                "`index_workout_session_exercises_session_id_position` " +
                "ON `workout_session_exercises` (`session_id`, `position`)",
        )
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `workout_sets` (
                `id` TEXT NOT NULL,
                `session_exercise_id` TEXT NOT NULL,
                `position` INTEGER NOT NULL,
                `set_type` TEXT NOT NULL,
                `weight_grams` INTEGER NOT NULL,
                `repetitions` INTEGER NOT NULL,
                `rpe` INTEGER,
                `is_completed` INTEGER NOT NULL,
                `completed_at_epoch_millis` INTEGER,
                `created_at_epoch_millis` INTEGER NOT NULL,
                `updated_at_epoch_millis` INTEGER NOT NULL,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`session_exercise_id`) REFERENCES `workout_session_exercises`(`id`)
                    ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent(),
        )
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_workout_sets_session_exercise_id` " +
                "ON `workout_sets` (`session_exercise_id`)",
        )
        database.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_workout_sets_session_exercise_id_position` " +
                "ON `workout_sets` (`session_exercise_id`, `position`)",
        )
    }
}
