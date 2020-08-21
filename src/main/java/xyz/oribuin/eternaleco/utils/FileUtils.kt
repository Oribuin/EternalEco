package xyz.oribuin.eternaleco.utils

import org.bukkit.plugin.Plugin
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

object FileUtils {
    /**
     * Creates a file on disk from a file located in the jar
     *
     * @param fileName The name of the file to create
     */
    @JvmStatic
    fun createFile(plugin: Plugin, fileName: String) {
        val file = File(plugin.dataFolder, fileName)

        if (!file.exists()) {
            plugin.getResource(fileName).use { inStream ->
                if (inStream == null) {
                    file.createNewFile()
                    return
                }

                Files.copy(inStream, Paths.get(file.absolutePath))
            }
        }
    }

}