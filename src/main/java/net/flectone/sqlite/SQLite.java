package net.flectone.sqlite;

import net.flectone.Main;
import net.flectone.managers.FileManager;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.logging.Level;

import static net.flectone.managers.FileManager.config;

public class SQLite extends Database {

    private final String dbname;

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

            s.executeUpdate("CREATE TABLE IF NOT EXISTS mails (" +
                    "`uuid` varchar(32) NOT NULL," +
                    "`sender` varchar(32) NOT NULL," +
                    "`receiver` varchar(32) NOT NULL," +
                    "`message` varchar(32) NOT NULL," +
                    "PRIMARY KEY (`uuid`)" +
                    ");");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS mutes (" +
                    "`player` varchar(32) NOT NULL," +
                    "'time' int(11) NOT NULL," +
                    "'reason' varchar(32) NOT NULL," +
                    "`moderator` varchar(32)," +
                    "PRIMARY KEY (`player`)" +
                    ");");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS bans (" +
                    "`player` varchar(32) NOT NULL," +
                    "'time' int(11) NOT NULL," +
                    "'reason' varchar(32) NOT NULL," +
                    "`moderator` varchar(32)," +
                    "PRIMARY KEY (`player`)" +
                    ");");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS warns (" +
                    "`uuid` varchar(32) NOT NULL," +
                    "`player` varchar(32) NOT NULL," +
                    "'time' int(11) NOT NULL," +
                    "'reason' varchar(32) NOT NULL," +
                    "`moderator` varchar(32)," +
                    "PRIMARY KEY (`uuid`)" +
                    ");");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS players (" +
                    "'uuid' varchar(32) NOT NULL," +
                    "'colors' varchar(32)," +
                    "'ignore_list' text[]," +
                    "'mails' text[]," +
                    "'warns' text[]," +
                    "'chat' varchar(32)," +
                    "'stream' varchar(32)," +
                    "'spy' varchar(32)," +
                    "'enable_advancements' int(11)," +
                    "'enable_deaths' int(11)," +
                    "`enable_joins` int(11)," +
                    "'enable_quits' int(11)," +
                    "'enable_auto_message' int(11)," +
                    "'enable_command_me' int(11)," +
                    "'enable_command_try' int(11)," +
                    "'enable_command_try_cube' int(11)," +
                    "'enable_command_ball' int(11)," +
                    "'enable_command_tempban' int(11)," +
                    "'enable_command_mute' int(11)," +
                    "'enable_command_warn' int(11)," +
                    "'enable_command_msg' int(11)," +
                    "'enable_command_reply' int(11)," +
                    "'enable_command_mail' int(11)," +
                    "'enable_command_tic_tac_toe' int(11)," +
                    "'enable_command_kick' int(11)," +
                    "PRIMARY KEY (`uuid`)" +
                    ");");

            s.executeUpdate("PRAGMA JOURNAL_MODE=WAL");
            s.executeUpdate("PRAGMA OPTIMIZE");
            s.executeUpdate("PRAGMA LOCKING_MODE=EXCLUSIVE");
            s.executeUpdate("PRAGMA SYNCHRONOUS=EXTRA");
            s.executeUpdate("PRAGMA WAL_CHECKPOINT(TRUNCATE)");
            s.executeUpdate("PRAGMA WAL_AUTOCHECKPOINT=100");
            s.close();

            if (FileManager.getLastVersion().isEmpty()) {
                DatabaseMetaData md = connection.getMetaData();
                ResultSet rs = md.getColumns(null, null, "players", "mute_time");
                if (!rs.next()) return;
            }

            if (FileManager.compareVersions(FileManager.getLastVersion(), "3.10.0") == -1) {
                setMigrate3_9_0(true);

                File oldFile = new File(plugin.getDataFolder(), dbname + ".db");
                File newFile = new File(plugin.getDataFolder(), dbname + "-old.db");
                newFile.createNewFile();

                FileUtils.copyFile(oldFile, newFile);

                config.set("tab.update.rate", 40);
                config.save();
            }

            if (FileManager.compareVersions(FileManager.getLastVersion(), "3.10.2") == -1) {
                setMigrate3_10_1(true);
            }

            if (FileManager.compareVersions(FileManager.getLastVersion(), "3.11.0") == -1) {
                setMigrate3_10_3(true);
            }

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
        initialize();
    }
}