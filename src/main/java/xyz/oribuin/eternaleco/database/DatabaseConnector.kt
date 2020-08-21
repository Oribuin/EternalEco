package xyz.oribuin.eternaleco.database

import java.sql.Connection
import java.sql.SQLException

interface DatabaseConnector {

    /**
     * Closes all open connections to the database
     */
    fun closeConnection()

    /**
     * Executes a callback with a Connection passed and automatically closes it when finished
     *
     * @param callback The callback to execute once the connection is retrieved
     */
    fun connect(callback: (Connection) -> Unit)

    /**
     * Wraps a connection in a callback which will automagically handle catching sql errors
     */
    interface ConnectionCallback {
        @Throws(SQLException::class)
        fun accept(connection: Connection?)
    }
}