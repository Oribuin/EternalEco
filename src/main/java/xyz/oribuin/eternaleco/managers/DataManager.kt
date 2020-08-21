package xyz.oribuin.eternaleco.managers

import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import xyz.oribuin.eternaleco.EternalEco
import xyz.oribuin.eternaleco.database.DatabaseConnector
import xyz.oribuin.eternaleco.database.MySQLConnector
import xyz.oribuin.eternaleco.database.SQLiteConnector
import xyz.oribuin.eternaleco.utils.FileUtils.createFile
import java.sql.Connection

class DataManager(plugin: EternalEco) : Manager(plugin) {
    var connector: DatabaseConnector? = null

    override fun reload() {

        try {
            if (ConfigManager.Setting.SQL_ENABLED.boolean) {
                val hostname = ConfigManager.Setting.SQL_HOSTNAME.string
                val port = ConfigManager.Setting.SQL_PORT.int
                val database = ConfigManager.Setting.SQL_DATABASENAME.string
                val username = ConfigManager.Setting.SQL_USERNAME.string
                val password = ConfigManager.Setting.SQL_PASSWORD.string
                val useSSL = ConfigManager.Setting.SQL_USE_SSL.boolean

                this.connector = MySQLConnector(this.plugin, hostname, port, database, username, password, useSSL)
                this.plugin.logger.info("Now using MySQL for the plugin Database.")
            } else {
                createFile(plugin, "eternaleco.db")

                this.connector = SQLiteConnector(this.plugin)
                this.plugin.logger.info("Now using SQLite for the Plugin Database.")
            }

            this.createTables()

        } catch (ex: Exception) {
            this.plugin.logger.severe("Fatal error connecting to Database, Plugin has disabled itself.")
            Bukkit.getPluginManager().disablePlugin(this.plugin)
            ex.printStackTrace()
        }

    }

    private fun createTables() {
        val queries = arrayOf(
                "CREATE TABLE IF NOT EXISTS ${tablePrefix}currency (sender TXT, amount INT, PRIMARY KEY(sender))"
        )
        async {
            connector?.connect { connection: Connection ->
                for (string in queries) {
                    connection.prepareStatement(string).use { statement -> statement.executeUpdate() }
                }
            }
        }
    }

    fun updateBalance(player: OfflinePlayer, balance: Int) {
        async {
            connector?.connect { connection: Connection ->
                val createReport = "REPLACE INTO ${this.tablePrefix}currency (sender, amount) VALUES (?, ?)"
                connection.prepareStatement(createReport).use { statement ->
                    statement.setString(1, player.uniqueId.toString())
                    statement.setInt(2, balance)
                    statement.executeUpdate()
                }

            }
        }
    }

    fun getBalance(player: OfflinePlayer): Int {
        var balance = ConfigManager.Setting.STARTING_BALANCE.int

        connector?.connect { connection ->
            val getBalance = "SELECT amount FROM ${tablePrefix}currency WHERE sender = ?"
            connection.prepareStatement(getBalance).use { statement ->
                statement.setString(1, player.uniqueId.toString())
                val result = statement.executeQuery()
                if (result.next()) {
                    balance = result.getInt(1)
                }
            }
        }

        return balance
    }


    /**
     * Asynchronizes the callback with it's own thread unless it is already not on the main thread
     *
     * @param asyncCallback The callback to run on a separate thread
     */
    private fun async(asyncCallback: Runnable) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, asyncCallback)
    }

    private val tablePrefix: String
        get() = plugin.description.name.toLowerCase() + '_'

    override fun disable() {
        // Unused
    }
}