package net.flectone.sqlite;

import net.flectone.Main;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;


public class SQLite extends Database {
    public static boolean isOldVersion = false;
    public String SQLiteCreateTokensPlayers = "CREATE TABLE IF NOT EXISTS players (" +
            "'uuid' varchar(32) NOT NULL," +
            "'mute_time' int(11)," +
            "'mute_reason' varchar(32)," +
            "'tempban_time' int(11)," +
            "'tempban_reason' varchar(32)," +
            "'colors' varchar(32)," +
            "'ignore_list' text[]," +
            "'mails' text[]," +
            "'chat' varchar(32)," +
            "PRIMARY KEY (`uuid`)" +
            "FOREIGN KEY ('mails') REFERENCES mails (uuid)" +
            ");";
    public String SQLiteCreateTokensMails = "CREATE TABLE IF NOT EXISTS mails (" +
            "`uuid` varchar(32) NOT NULL," +
            "`sender` varchar(32) NOT NULL," +
            "`receiver` varchar(32) NOT NULL," +
            "`message` varchar(32) NOT NULL," +
            "PRIMARY KEY (`uuid`)" +
            ");";
    String dbname;


    public SQLite(Main instance) {
        super(instance);
        dbname = plugin.getConfig().getString("SQLite.Filename", Main.config.getString("database"));
    }

    // SQL creation stuff, You can leave the blow stuff untouched.
    public Connection getSQLConnection() {
        File dataFolder = new File(plugin.getDataFolder(), dbname + ".db");
        if (!dataFolder.exists()) {
            try {
                dataFolder.createNewFile();
                isOldVersion = true;
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "File write error: " + dbname + ".db");
            }
        }
        try {
            if (connection != null && !connection.isClosed()) {
                return connection;
            }
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dataFolder);
            return connection;
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, "SQLite exception on initialize", ex);
        } catch (ClassNotFoundException ex) {
            plugin.getLogger().log(Level.SEVERE, "You need the SQLite JBDC library. Google it. Put it in /lib folder.");
        }
        return null;
    }

    public void load() {
        connection = getSQLConnection();
        try {
            Statement s = connection.createStatement();

            s.executeUpdate(SQLiteCreateTokensMails);

            s.executeUpdate(SQLiteCreateTokensPlayers);
            s.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        initialize();
    }
}