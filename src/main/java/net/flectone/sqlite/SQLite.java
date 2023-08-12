package net.flectone.sqlite;

import net.flectone.Main;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

import static net.flectone.managers.FileManager.config;

public class SQLite extends Database {
    public static boolean isOldVersion = false;
    public final String SQLiteCreateTokensPlayers = "CREATE TABLE IF NOT EXISTS players (" +
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
            ");";
    public final String SQLiteCreateTokensMails = "CREATE TABLE IF NOT EXISTS mails (" +
            "`uuid` varchar(32) NOT NULL," +
            "`sender` varchar(32) NOT NULL," +
            "`receiver` varchar(32) NOT NULL," +
            "`message` varchar(32) NOT NULL," +
            "PRIMARY KEY (`uuid`)" +
            ");";
    final String dbname;


    public SQLite(@NotNull Main instance) {
        super(instance);
        dbname = plugin.getConfig().getString("SQLite.Filename", config.getString("database"));
    }

    @Nullable
    public Connection getSQLConnection() {
        File dataFolder = new File(plugin.getDataFolder(), dbname + ".db");
        if (!dataFolder.exists()) {
            try {
                if (!dataFolder.createNewFile()) Main.warning("Failed to create file " + dbname + ".db");
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
        if (connection == null) return;
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