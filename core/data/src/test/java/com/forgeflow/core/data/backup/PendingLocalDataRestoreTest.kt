package com.forgeflow.core.data.backup

import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import org.junit.Assert.assertThrows
import org.junit.Test

class PendingLocalDataRestoreTest {
    @Test
    fun `accepts a complete version one backup`() {
        val archive = backupArchive(
            "forgeflow-export.txt" to "ForgeFlow local export\nformatVersion=1\n",
            "database/forgeflow.db" to "database",
            "datastore/forgeflow_settings.preferences_pb" to "settings",
            "files/profile/avatar.jpg" to "photo",
        )

        PendingLocalDataRestore.validateArchive(archive)
    }

    @Test
    fun `rejects paths outside known backup locations`() {
        val archive = backupArchive(
            "forgeflow-export.txt" to "ForgeFlow local export\nformatVersion=1\n",
            "database/forgeflow.db" to "database",
            "datastore/forgeflow_settings.preferences_pb" to "settings",
            "../outside.txt" to "unexpected",
        )

        assertThrows(IllegalArgumentException::class.java) {
            PendingLocalDataRestore.validateArchive(archive)
        }
    }

    @Test
    fun `rejects an unsupported backup version`() {
        val archive = backupArchive(
            "forgeflow-export.txt" to "ForgeFlow local export\nformatVersion=99\n",
            "database/forgeflow.db" to "database",
            "datastore/forgeflow_settings.preferences_pb" to "settings",
        )

        assertThrows(IllegalArgumentException::class.java) {
            PendingLocalDataRestore.validateArchive(archive)
        }
    }

    private fun backupArchive(vararg entries: Pair<String, String>): File {
        val file = kotlin.io.path.createTempFile("forgeflow-backup-", ".zip").toFile()
        file.deleteOnExit()
        ZipOutputStream(file.outputStream()).use { archive ->
            entries.forEach { (name, content) ->
                archive.putNextEntry(ZipEntry(name))
                archive.write(content.toByteArray())
                archive.closeEntry()
            }
        }
        return file
    }
}
