package com.forgeflow.core.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object MigrationRegistry {
    private val migration1To2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            createRoutineTables(db)
            createWorkoutTables(db)
        }
    }

    private val migration2To3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE `exercises` ADD COLUMN `media_uri` TEXT")
            db.execSQL("ALTER TABLE `exercises` ADD COLUMN `media_type` TEXT")
            db.execSQL("ALTER TABLE `exercises` ADD COLUMN `media_thumbnail_uri` TEXT")
            db.execSQL(
                "ALTER TABLE `workout_session_exercises` " +
                    "ADD COLUMN `media_uri_snapshot` TEXT",
            )
            db.execSQL(
                "ALTER TABLE `workout_session_exercises` " +
                    "ADD COLUMN `media_type_snapshot` TEXT",
            )
            db.execSQL(
                "ALTER TABLE `workout_session_exercises` " +
                    "ADD COLUMN `media_thumbnail_uri_snapshot` TEXT",
            )
        }
    }

    private val migration3To4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE `workout_sessions` ADD COLUMN `location_latitude` REAL",
            )
            db.execSQL(
                "ALTER TABLE `workout_sessions` ADD COLUMN `location_longitude` REAL",
            )
            db.execSQL(
                "ALTER TABLE `workout_sessions` ADD COLUMN `location_accuracy_meters` REAL",
            )
            db.execSQL(
                "ALTER TABLE `workout_sessions` " +
                    "ADD COLUMN `location_captured_at_epoch_millis` INTEGER",
            )
            db.execSQL(
                "ALTER TABLE `workout_sessions` ADD COLUMN `location_label` TEXT",
            )
        }
    }

    private val migration4To5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `routine_folders` (
                    `id` TEXT NOT NULL,
                    `name` TEXT NOT NULL,
                    `position` INTEGER NOT NULL,
                    `created_at_epoch_millis` INTEGER NOT NULL,
                    `updated_at_epoch_millis` INTEGER NOT NULL,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent(),
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_routine_folders_position` " +
                    "ON `routine_folders` (`position`)",
            )
            db.execSQL("ALTER TABLE `routines` ADD COLUMN `folder_id` TEXT")
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_routines_folder_id` " +
                    "ON `routines` (`folder_id`)",
            )
            updateDefaultExerciseMedia(db)
        }
    }

    private val migration5To6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE `routines` ADD COLUMN `position` INTEGER NOT NULL DEFAULT 0")
            db.execSQL(
                "ALTER TABLE `routines` ADD COLUMN " +
                    "`compare_history_within_folder` INTEGER NOT NULL DEFAULT 0",
            )
            db.execSQL(
                "ALTER TABLE `routine_exercises` ADD COLUMN " +
                    "`planned_warm_up_sets` INTEGER NOT NULL DEFAULT 0",
            )
        }
    }

    private val migration6To7 = object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE `routine_folders` ADD COLUMN " +
                    "`owner_user_id` TEXT NOT NULL DEFAULT ''",
            )
            db.execSQL(
                "ALTER TABLE `routines` ADD COLUMN " +
                    "`owner_user_id` TEXT NOT NULL DEFAULT ''",
            )
            db.execSQL(
                "ALTER TABLE `workout_sessions` ADD COLUMN " +
                    "`owner_user_id` TEXT NOT NULL DEFAULT ''",
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_routine_folders_owner_user_id` " +
                    "ON `routine_folders` (`owner_user_id`)",
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_routines_owner_user_id` " +
                    "ON `routines` (`owner_user_id`)",
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_workout_sessions_owner_user_id` " +
                    "ON `workout_sessions` (`owner_user_id`)",
            )
        }
    }

    val all: Array<Migration> = arrayOf(
        migration1To2,
        migration2To3,
        migration3To4,
        migration4To5,
        migration5To6,
        migration6To7,
    )

    private fun updateDefaultExerciseMedia(database: SupportSQLiteDatabase) {
        val mediaById = mapOf(
            "04f35c8f-e525-469e-8238-25e31087e07a" to "forgeflow://exercise/bench-press",
            "42e026cb-c0a2-442c-8d46-a5c2ae5e4293" to "forgeflow://exercise/back-squat",
            "234590ba-5ae9-407f-afaa-3bfaf62a1c42" to "forgeflow://exercise/deadlift",
            "eae8a5fc-acb1-465a-8162-246bcbafd7b1" to "forgeflow://exercise/barbell-row",
            "ca3218ec-516b-455b-af27-51dc846065e0" to "forgeflow://exercise/overhead-press",
        )
        mediaById.forEach { (id, uri) ->
            database.execSQL(
                "UPDATE `exercises` SET `media_uri` = ?, `media_type` = 'IMAGE' WHERE `id` = ?",
                arrayOf(uri, id),
            )
        }
    }

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
