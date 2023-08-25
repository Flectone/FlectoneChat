package net.flectone.sqlite;

import net.flectone.Main;
import net.flectone.commands.CommandChatcolor;
import net.flectone.misc.actions.Mail;
import net.flectone.misc.entity.DatabasePlayer;
import net.flectone.misc.entity.FPlayer;
import net.flectone.misc.entity.PlayerChatInfo;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

public abstract class Database {
    final Main plugin;
    Connection connection;

    public Database(@NotNull Main instance) {
        plugin = instance;
    }

    public abstract Connection getSQLConnection();

    public abstract void load();

    public void initialize() {
        Main.info("\uD83D\uDCCA Database connecting");
        connection = getSQLConnection();
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM players WHERE uuid = ?");
            ResultSet rs = ps.executeQuery();
            close(ps, rs);

            if (SQLite.isOldVersion) migrateDatabase3_9_0();

        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, "Unable to retrieve connection", ex);
        }
    }

    private void migrateDatabase3_9_0() {
        try (Connection conn = getSQLConnection();
             PreparedStatement playerStatement = conn.prepareStatement("SELECT * FROM players")) {

            ResultSet playerResultSet = playerStatement.executeQuery();

            while (playerResultSet.next()) {

                String playerUUID = playerResultSet.getString("uuid");

                int muteTime = playerResultSet.getInt("mute_time");
                String muteReason = playerResultSet.getString("mute_reason");
                migrateModColumn(conn, "mutes", playerUUID, muteTime, muteReason);

                int banTime = playerResultSet.getInt("tempban_time");
                String banReason = playerResultSet.getString("tempban_reason");
                migrateModColumn(conn, "bans", playerUUID, banTime, banReason);
            }

            close(playerStatement, playerResultSet);

            Statement statement = conn.createStatement();

            addColumn(statement, "players", "warns", "varchar(32)");
            addColumn(statement, "players", "enable_advancements",  "varchar(11)");
            addColumn(statement, "players", "enable_deaths",  "varchar(11)");
            addColumn(statement, "players", "enable_joins",  "varchar(11)");
            addColumn(statement, "players", "enable_quits",  "varchar(11)");
            addColumn(statement, "players", "enable_command_me",  "varchar(11)");
            addColumn(statement, "players", "enable_command_try",  "varchar(11)");
            addColumn(statement, "players", "enable_command_try_cube",  "varchar(11)");
            addColumn(statement, "players", "enable_command_ball",  "varchar(11)");
            addColumn(statement, "players", "enable_command_tempban",  "varchar(11)");
            addColumn(statement, "players", "enable_command_mute",  "varchar(11)");
            addColumn(statement, "players", "enable_command_warn",  "varchar(11)");
            addColumn(statement, "players", "enable_command_msg",  "varchar(11)");
            addColumn(statement, "players", "enable_command_reply",  "varchar(11)");
            addColumn(statement, "players", "enable_command_mail",  "varchar(11)");
            addColumn(statement, "players", "enable_command_tic_tac_toe",  "varchar(11)");
            dropColumn(statement, "players", "mute_time");
            dropColumn(statement, "players", "mute_reason");
            dropColumn(statement, "players", "tempban_time");
            dropColumn(statement, "players", "tempban_reason");

            statement.close();

        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, "Couldn't execute MySQL statement: ", ex);
        }

    }

    private void dropColumn(Statement statement, String table, String column) throws SQLException {
        statement.executeUpdate("ALTER TABLE " + table + " DROP COLUMN " + column);
    }

    private void addColumn(Statement statement, String table, String column, String type) throws SQLException {
        statement.executeUpdate("ALTER TABLE " + table + " ADD COLUMN " + column + " " + type);
    }

    private void migrateModColumn(Connection conn, String table, String playerUUID, int time, String reason) throws SQLException{
        if (reason == null) return;

        PreparedStatement statement = conn.prepareStatement("INSERT OR REPLACE INTO "+ table + " (player, time, reason, moderator) VALUES(?,?,?,?)");

        statement.setString(1, playerUUID);
        statement.setInt(2, time);
        statement.setString(3, reason);
        statement.setString(4, null);

        statement.executeUpdate();
        statement.close();
    }

    public void insertPlayer(@NotNull UUID uuid) {
        try (Connection conn = getSQLConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT OR IGNORE INTO players (uuid) VALUES(?)")) {

            ps.setString(1, uuid.toString());

            ps.executeUpdate();
            ps.close();

        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, "Couldn't execute MySQL statement: ", ex);
        }
    }

    public ArrayList<DatabasePlayer> getPlayers(String table, int limit, int skip) {
        ArrayList<DatabasePlayer> databasePlayers = new ArrayList<>();

        try (Connection conn = getSQLConnection();
                PreparedStatement preparedStatement = conn.prepareStatement("SELECT * FROM " + table + " LIMIT " + limit + " OFFSET " + skip)) {

            ResultSet playerResult = preparedStatement.executeQuery();

            while (playerResult.next()) {

                String playerUUID = playerResult.getString(1);
                int time = playerResult.getInt(2);
                String reason = playerResult.getString(3);
                String moderator = playerResult.getString(4);

                databasePlayers.add(new DatabasePlayer(playerUUID, time, reason, moderator));
            }

            close(preparedStatement, playerResult);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return databasePlayers;
    }

    public DatabasePlayer getPlayer(String table, String playerUUID) {
        try (Connection conn = getSQLConnection()) {
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT * FROM " + table + " WHERE player = ?");

            preparedStatement.setString(1, playerUUID);
            ResultSet playerResult = preparedStatement.executeQuery();

            if (!playerResult.next()) return null;

            switch (table) {
                case "bans", "mutes" -> {
                    int time = playerResult.getInt(2);
                    String reason = playerResult.getString(3);
                    String moderator = playerResult.getString(4);
                    return new DatabasePlayer(playerUUID, time, reason, moderator);
                }
            }

            close(preparedStatement, playerResult);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void initFPlayer(@NotNull FPlayer fPlayer) {
        try {
            Connection connection = getSQLConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM players WHERE uuid = ?");
            preparedStatement.setString(1, fPlayer.getUUID().toString());

            ResultSet playerResult = preparedStatement.executeQuery();
            if (!(playerResult.next())) return;

            String color = playerResult.getString("colors");
            String[] colors = color == null ? CommandChatcolor.getDefaultColors() : color.split(",");
            fPlayer.setColors(colors[0], colors[1]);

            String ignoreList = playerResult.getString("ignore_list");
            ArrayList<UUID> arrayList = ignoreList == null
                    ? new ArrayList<>()
                    : new ArrayList<>(Arrays.stream(ignoreList.split(","))
                    .map(UUID::fromString).collect(Collectors.toList()));

            fPlayer.setIgnoreList(arrayList);

            String mail = playerResult.getString("mails");
            if (mail != null) {
                String[] mails = mail.split(",");

                for (String uuid : mails) {
                    PreparedStatement ps2 = connection.prepareStatement("SELECT * FROM mails WHERE uuid = ?");
                    ps2.setString(1, uuid);

                    ResultSet resultMail = ps2.executeQuery();

                    fPlayer.addMail(UUID.fromString(uuid), new Mail(UUID.fromString(resultMail.getString("sender")),
                            UUID.fromString(resultMail.getString("receiver")),
                            resultMail.getString("message")));
                }
            }

            PlayerChatInfo playerChatInfo = new PlayerChatInfo();

            String chat = playerResult.getString("chat");
            playerChatInfo.setChatType(chat == null ? "local" : chat);

            String option = playerResult.getString("enable_advancements");
            playerChatInfo.setOption("advancement", parseBoolean(option));

            option = playerResult.getString("enable_deaths");
            playerChatInfo.setOption("death", parseBoolean(option));

            option = playerResult.getString("enable_joins");
            playerChatInfo.setOption("join", parseBoolean(option));

            option = playerResult.getString("enable_quits");
            playerChatInfo.setOption("quit", parseBoolean(option));

            option = playerResult.getString("enable_command_me");
            playerChatInfo.setOption("me", parseBoolean(option));

            option = playerResult.getString("enable_command_try");
            playerChatInfo.setOption("try", parseBoolean(option));

            option = playerResult.getString("enable_command_try_cube");
            playerChatInfo.setOption("try-cube", parseBoolean(option));

            option = playerResult.getString("enable_command_ball");
            playerChatInfo.setOption("ball", parseBoolean(option));

            option = playerResult.getString("enable_command_tempban");
            playerChatInfo.setOption("tempban", parseBoolean(option));

            option = playerResult.getString("enable_command_mute");
            playerChatInfo.setOption("mute", parseBoolean(option));

            option = playerResult.getString("enable_command_warn");
            playerChatInfo.setOption("warn", parseBoolean(option));

            option = playerResult.getString("enable_command_msg");
            playerChatInfo.setOption("msg", parseBoolean(option));

            option = playerResult.getString("enable_command_reply");
            playerChatInfo.setOption("reply", parseBoolean(option));

            option = playerResult.getString("enable_command_mail");
            playerChatInfo.setOption("mail", parseBoolean(option));

            option = playerResult.getString("enable_command_tic_tac_toe");
            playerChatInfo.setOption("tic-tac-toe", parseBoolean(option));

            fPlayer.setChatInfo(playerChatInfo);

            close(preparedStatement, playerResult);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean parseBoolean(String option) {
        return option == null || Boolean.parseBoolean(option);
    }

    public void saveModeratorAction(String table, DatabasePlayer databasePlayer) {
        try {
            Connection connection = getSQLConnection();

            PreparedStatement preparedStatement = connection.prepareStatement("REPLACE INTO "+ table +" (player, time, reason, moderator) VALUES(?,?,?,?)");

            preparedStatement.setString(1, databasePlayer.getPlayer());
            preparedStatement.setInt(2, databasePlayer.getTime());
            preparedStatement.setString(3, databasePlayer.getReason());
            preparedStatement.setString(4, databasePlayer.getModerator());

            preparedStatement.executeUpdate();
            preparedStatement.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteModeratorRow(String table, String uuid) {

        try {
            Connection connection = getSQLConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM " + table + " WHERE player = ?");

            preparedStatement.setString(1, uuid);
            preparedStatement.executeUpdate();
            preparedStatement.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getDatabaseInt(String table, String column, String player) {
        try {
            Connection conn = getSQLConnection();
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT " + column + " FROM " + table + " WHERE player = ?");
            preparedStatement.setString(1, player);
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();

            return resultSet.getInt(1);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    @Nullable
    public String getDatabaseString(String table, String column, String player) {
        try {
            Connection conn = getSQLConnection();
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT " + column + " FROM " + table + " WHERE player = ?");
            preparedStatement.setString(1, player);
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();

            return resultSet.getString(1);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Nullable
    public ResultSet getDatabaseResult(String table, String player) {
        try {
            Connection conn = getSQLConnection();
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT * FROM " + table + " WHERE player = ?");
            preparedStatement.setString(1, player);
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();

            return resultSet;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public int getCount(String table) {
        try {
            Connection conn = getSQLConnection();
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT COUNT(1) FROM " + table);
            ResultSet resultSet = preparedStatement.executeQuery();

            resultSet.next();

            return resultSet.getInt(1);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public ArrayList<String> getPlayersModeration(String table) {
        return getPlayers("player", table);
    }

    public ArrayList<String> getPlayers(String column, String table) {
        ArrayList<String> arrayList = new ArrayList<>();
        try {
            Connection conn = getSQLConnection();
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT " + column + " FROM " + table);

            ResultSet playerResult = preparedStatement.executeQuery();

            while (playerResult.next()) {

                String playerUUID = playerResult.getString(column);
                if (playerUUID == null) continue;

                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(playerUUID));
                String offlinePlayerName = offlinePlayer.getName();
                if (offlinePlayerName == null) continue;

                arrayList.add(offlinePlayerName);
            }

            close(preparedStatement, playerResult);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return arrayList;
    }

    public void saveColors(FPlayer fPlayer) {
        Bukkit.broadcastMessage("COLORS " + Thread.currentThread().toString());
        try (Connection conn = getSQLConnection();
             PreparedStatement preparedStatement = conn.prepareStatement("UPDATE players SET " +
                     "colors = ?" +
                     "WHERE uuid = ?")) {

            preparedStatement.setString(1, fPlayer.getColors()[0] + "," + fPlayer.getColors()[1]);
            preparedStatement.setString(2, fPlayer.getUUID().toString());

            preparedStatement.executeUpdate();
            preparedStatement.close();

        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, "Couldn't execute SQLite statement: ", ex);
        }
    }

    public void saveIgnoreList(FPlayer fPlayer) {
        Bukkit.broadcastMessage("IGNORELIST " + Thread.currentThread().toString());
        try (Connection conn = getSQLConnection();
             PreparedStatement preparedStatement = conn.prepareStatement("UPDATE players SET " +
                     "ignore_list = ?" +
                     "WHERE uuid = ?")) {

            StringBuilder ignoreListString = new StringBuilder();
            for (UUID ignoredPlayer : fPlayer.getIgnoreList())
                ignoreListString.append(ignoredPlayer).append(",");

            preparedStatement.setString(1, ignoreListString.length() == 0 ? null : ignoreListString.toString());
            preparedStatement.setString(2, fPlayer.getUUID().toString());

            preparedStatement.executeUpdate();
            preparedStatement.close();

        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, "Couldn't execute SQLite statement: ", ex);
        }
    }

    public void close(PreparedStatement ps, ResultSet rs) {
        try {
            if (ps != null)
                ps.close();
            if (rs != null)
                rs.close();
        } catch (SQLException ex) {
            Main.warning("Failed to close MySQL connection");
            ex.printStackTrace();
        }
    }
}
