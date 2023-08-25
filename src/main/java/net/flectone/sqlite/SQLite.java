package net.flectone.sqlite;

import net.flectone.Main;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.logging.Level;

import static net.flectone.managers.FileManager.config;

public class SQLite extends Database {
    public static boolean isOldVersion = false;
    public final String SQLiteCreateTokensPlayers = "CREATE TABLE IF NOT EXISTS players (" +
            "'uuid' varchar(32) NOT NULL," +
            "'colors' varchar(32)," +
            "'ignore_list' text[]," +
            "'mails' text[]," +
            "'warns' text[]," +
            "'chat' varchar(32)," +
            "'enable_advancements' int(11)," +
            "'enable_deaths' int(11)," +
            "`enable_joins` int(11)," +
            "'enable_quits' int(11)," +
            "'enable_command_me' int(11)," +
            "'enable_command_try' int(11)," +
            "'enable_command_try-cube' int(11)," +
            "'enable_command_ball' int(11)," +
            "'enable_command_tempban' int(11)," +
            "'enable_command_mute' int(11)," +
            "'enable_command_warn' int(11)," +
            "'enable_command_msg' int(11)," +
            "'enable_command_reply' int(11)," +
            "'enable_command_mail' int(11)," +
            "'enable_command_tic-tac-toe' int(11)," +
            "PRIMARY KEY (`uuid`)" +
            ");";
    public final String SQLiteCreateTokensMails = "CREATE TABLE IF NOT EXISTS mails (" +
            "`uuid` varchar(32) NOT NULL," +
            "`sender` varchar(32) NOT NULL," +
            "`receiver` varchar(32) NOT NULL," +
            "`message` varchar(32) NOT NULL," +
            "PRIMARY KEY (`uuid`)" +
            ");";

    public final String SQLiteCreateTokensMutes = "CREATE TABLE IF NOT EXISTS mutes (" +
            "`player` varchar(32) NOT NULL," +
            "'time' int(11) NOT NULL," +
            "'reason' varchar(32) NOT NULL," +
            "`moderator` varchar(32)," +
            "PRIMARY KEY (`player`)" +
            ");";

    public final String SQLiteCreateTokensBans = "CREATE TABLE IF NOT EXISTS bans (" +
            "`player` varchar(32) NOT NULL," +
            "'time' int(11) NOT NULL," +
            "'reason' varchar(32) NOT NULL," +
            "`moderator` varchar(32)," +
            "PRIMARY KEY (`player`)" +
            ");";

    public final String SQLiteCreateTokensWarns = "CREATE TABLE IF NOT EXISTS warns (" +
            "`uuid` varchar(32) NOT NULL," +
            "`player` varchar(32) NOT NULL," +
            "'time' int(11) NOT NULL," +
            "'reason' varchar(32) NOT NULL," +
            "`moderator` varchar(32)," +
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
            s.executeUpdate(SQLiteCreateTokensMutes);
            s.executeUpdate(SQLiteCreateTokensBans);
            s.executeUpdate(SQLiteCreateTokensWarns);

            s.executeUpdate(SQLiteCreateTokensPlayers);
            s.close();

            DatabaseMetaData md = connection.getMetaData();

            ResultSet rs = md.getColumns(null, null, "players", "mute_time");
            if (rs.next() || !config.getString("version").equals(Main.getInstance().getDescription().getVersion())) {
                isOldVersion = true;
            }

            rs.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        initialize();
    }
}