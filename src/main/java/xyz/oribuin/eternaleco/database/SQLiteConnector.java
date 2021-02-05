package xyz.oribuin.eternaleco.database;

import org.bukkit.plugin.Plugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author Oribuin, Esophose
 */
public class SQLiteConnector implements DatabaseConnector {
    private final Plugin plugin;
    private final String dbName;

    private Connection connection = null;

    public SQLiteConnector(Plugin plugin, String dbName) {
        this.plugin = plugin;
        this.dbName = dbName;
    }

    @Override
    public void connect(ConnectionCallback callback) {
        if (connection == null) {
            try {
                connection = DriverManager.getConnection(this.getConnectionString());
            } catch (SQLException ex) {
                plugin.getLogger().severe("An error occurred closing the SQLite database connection: " + ex.getMessage());
            }
        }

        try {
            callback.accept(connection);
        } catch (SQLException ex) {
            plugin.getLogger().severe("An error occurred closing the SQLite query: " + ex.getMessage());
            ex.printStackTrace();
        }

    }

    @Override
    public void closeConnection() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException ex) {
            plugin.getLogger().severe("An error occurred closing the SQLite database connection: " + ex.getMessage());
        }
    }


    public String getConnectionString() {
        return "jdbc:sqlite:" + plugin.getDataFolder() + File.separator + dbName;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public String getDBName() {
        return dbName;
    }
}
