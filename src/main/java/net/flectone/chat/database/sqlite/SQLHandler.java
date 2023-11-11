package net.flectone.chat.database.sqlite;

import net.flectone.chat.FlectoneChat;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLHandler {

    public Connection connection = null;
    private final String filePath;
    private String connectionURL;

    public SQLHandler(final String filePath) {
        this.filePath = filePath;
    }

    public void connect(final String databaseName) {
        connection = null;
        try {

            Class.forName("org.sqlite.JDBC");
            connectionURL = "jdbc:sqlite:" + filePath + File.separator + databaseName + ".db";

            connection = DriverManager.getConnection(connectionURL);
            if (connection != null) {
                onConnect();
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @NotNull
    public Connection getConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                return connection;
            }
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(connectionURL);
            return connection;
        } catch (SQLException ex) {
            FlectoneChat.warning("SQLite exception on initialize");
            ex.printStackTrace();

        } catch (ClassNotFoundException ex) {
            FlectoneChat.warning("You need the SQLite JBDC library");
            ex.printStackTrace();
        }

        throw new RuntimeException();
    }

    public void onConnect() {

    }

    public void disconnect() {
        if (connection == null) return;
        try {
            connection.close();
            connection = null;
            onDisconnect();
        } catch (final SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void onDisconnect() {

    }

    public boolean isConnected() {
        return connection != null;
    }

}