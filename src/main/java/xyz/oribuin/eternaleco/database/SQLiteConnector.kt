package xyz.oribuin.eternaleco.database

import org.bukkit.plugin.Plugin
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

class SQLiteConnector(private val plugin: Plugin) : DatabaseConnector {
    private val connectionString: String = "jdbc:sqlite:${plugin.dataFolder}${File.separator}eternaleco.db"

    private var connection: Connection? = null

    override fun closeConnection() {
        try {
            if (connection != null) {
                connection?.close()
            }
        } catch (ex: SQLException) {
            plugin.logger.severe("An error occurred closing the SQLite database connection: " + ex.message)
        }
    }

    override fun connect(callback: (Connection) -> Unit) {
        if (connection == null) {
            try {
                connection = DriverManager.getConnection(connectionString)
            } catch (ex: SQLException) {
                plugin.logger.severe("An error occurred retrieving the SQLite database connection: " + ex.message)
            }
        }
        try {
            this.connection?.let { callback(it) }
        } catch (ex: Exception) {
            plugin.logger.severe("An error occurred executing an SQLite query: " + ex.message)
            ex.printStackTrace()
        }
    }

    init {
        try {
            Class.forName("org.sqlite.JDBC") // This is required to put here for Spigot 1.10 and below for some reason
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        }
    }
}