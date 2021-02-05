package xyz.oribuin.eternaleco.database;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Oribuin, Esophose
 */
public interface DatabaseConnector {

    /**
     * Executes a callback with the Connection passed and automatically closes it when finished.
     *
     * @param callback The callback to execute once the connection is retrieved.
     */
    void connect(ConnectionCallback callback);

    /**
     * Closes all open connections to the database.
     */
    void closeConnection();

    /**
     * Wraps a connection in a callback which will automagically handle catching sql errors
     */
    interface ConnectionCallback {
        void accept(Connection connection) throws SQLException;
    }

}
