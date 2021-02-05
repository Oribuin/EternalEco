package xyz.oribuin.eternaleco.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.plugin.Plugin;

import java.sql.SQLException;

/**
 * @author Oribuin, Esophose
 */
public class MySQLConnector implements DatabaseConnector {

    private final Plugin plugin;
    private final HikariDataSource hikariDataSource;

    public MySQLConnector(Plugin plugin, String host, int port, String database, String username, String password, boolean ssl) {
        this.plugin = plugin;
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=" + ssl);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(10);
        hikariDataSource = new HikariDataSource(config);
    }

    @Override
    public void connect(ConnectionCallback callback) {
        try {
            callback.accept(hikariDataSource.getConnection());
        } catch (SQLException ex) {
            this.plugin.getLogger().severe("An error occurred executing a MySQL Query: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @Override
    public void closeConnection() {
        hikariDataSource.close();
    }
}
