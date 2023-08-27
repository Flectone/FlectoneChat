package net.flectone.sqlite;

import net.flectone.Main;
import net.flectone.commands.CommandChatcolor;
import net.flectone.managers.FileManager;
import net.flectone.misc.entity.info.Mail;
import net.flectone.misc.entity.FPlayer;
import net.flectone.misc.entity.info.ChatInfo;
import net.flectone.misc.entity.info.ModInfo;
import net.flectone.misc.files.FYamlConfiguration;
import net.flectone.utils.ObjectUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public abstract class Database {
    final Main plugin;
    protected static Connection connection;

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
            ps.executeQuery();

            if (SQLite.isOldVersion) migrateDatabase3_9_0();

        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, "Unable to retrieve connection", ex);
        }
    }

    public void deleteRow(@NotNull String table, @NotNull String column, @NotNull String filter) {
        try {
            Connection connection = getSQLConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM " + table + " WHERE " + column + " = ?");

            preparedStatement.setString(1, filter);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void clearOldRows(String table) {
        try {
            Connection connection = getSQLConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM " + table + " WHERE time<=?");
            preparedStatement.setInt(1, ObjectUtil.getCurrentTime());

            preparedStatement.executeUpdate();

        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    private void dropColumn(Statement statement, String table, String column) throws SQLException {
        statement.executeUpdate("ALTER TABLE " + table + " DROP COLUMN " + column);
    }

    private void addColumn(Statement statement, String table, String column, String type) throws SQLException {
        statement.executeUpdate("ALTER TABLE " + table + " ADD COLUMN " + column + " " + type);
    }

    public void insertPlayer(@NotNull UUID uuid) {
        try {
            Connection conn = getSQLConnection();
            PreparedStatement ps = conn.prepareStatement("INSERT OR IGNORE INTO players (uuid) VALUES(?)");
            ps.setString(1, uuid.toString());

            ps.executeUpdate();

        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, "Couldn't execute MySQL statement: ", ex);
        }
    }

    @Nullable
    public Object getPlayerInfo(@NotNull String table, @NotNull String column, @NotNull String filter) {
        try (Connection conn = getSQLConnection()) {
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT * FROM " + table + " WHERE " + column + " = ?");
            preparedStatement.setString(1, filter);
            ResultSet playerResult = preparedStatement.executeQuery();

            if (!playerResult.next()) return null;

            switch (table) {
                case "bans", "mutes" -> {
                    int time = playerResult.getInt(2);
                    String reason = playerResult.getString(3);
                    String moderator = playerResult.getString(4);
                    return new ModInfo(filter, time, reason, moderator);
                }
                case "warns" -> {

                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return getPlayerInfo(table, column, filter);
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
                    if (uuid.isEmpty()) continue;

                    PreparedStatement ps2 = connection.prepareStatement("SELECT * FROM mails WHERE uuid = ?");
                    ps2.setString(1, uuid);

                    ResultSet resultMail = ps2.executeQuery();
                    resultMail.next();

                    fPlayer.addMail(UUID.fromString(uuid), new Mail(UUID.fromString(uuid),
                            UUID.fromString(resultMail.getString("sender")),
                            UUID.fromString(resultMail.getString("receiver")),
                            resultMail.getString("message")));
                }
            }

            ChatInfo chatInfo = new ChatInfo(fPlayer.getUUID().toString());

            String chat = playerResult.getString("chat");
            chatInfo.setChatType(chat == null ? "local" : chat);

            String option = playerResult.getString("enable_advancements");
            chatInfo.setOption("advancement", parseBoolean("advancement.message.enable", option));

            option = playerResult.getString("enable_deaths");
            chatInfo.setOption("death", parseBoolean("death.message.enable", option));

            option = playerResult.getString("enable_joins");
            chatInfo.setOption("join", parseBoolean("player.join.message.enable", option));

            option = playerResult.getString("enable_quits");
            chatInfo.setOption("quit", parseBoolean("player.quit.message.enable", option));

            option = playerResult.getString("enable_command_me");
            chatInfo.setOption("me", parseBoolean("command.me.enable", option));

            option = playerResult.getString("enable_command_try");
            chatInfo.setOption("try", parseBoolean("command.try.enable", option));

            option = playerResult.getString("enable_command_try_cube");
            chatInfo.setOption("try-cube", parseBoolean("command.try-cube.enable", option));

            option = playerResult.getString("enable_command_ball");
            chatInfo.setOption("ball", parseBoolean("command.ball.enable", option));

            option = playerResult.getString("enable_command_tempban");
            chatInfo.setOption("tempban", parseBoolean("command.tempban.enable", option));

            option = playerResult.getString("enable_command_mute");
            chatInfo.setOption("mute", parseBoolean("command.mute.enable", option));

            option = playerResult.getString("enable_command_warn");
            chatInfo.setOption("warn", parseBoolean("command.warn.enable", option));

            option = playerResult.getString("enable_command_msg");
            chatInfo.setOption("msg", parseBoolean("command.msg.enable", option));

            option = playerResult.getString("enable_command_reply");
            chatInfo.setOption("reply", parseBoolean("command.reply.enable", option));

            option = playerResult.getString("enable_command_mail");
            chatInfo.setOption("mail", parseBoolean("command.mail.enable", option));

            option = playerResult.getString("enable_command_tic_tac_toe");
            chatInfo.setOption("tic-tac-toe", parseBoolean("command.tic-tac-toe.enable", option));

            if (fPlayer.hasPermission("flectonechat.stream")) {
                fPlayer.setStreaming(Boolean.parseBoolean(playerResult.getString("stream")));
            }

            if (fPlayer.hasPermission("flectonechat.spy")) {
                fPlayer.setSpies(Boolean.parseBoolean(playerResult.getString("spy")));
            }

            fPlayer.setChatInfo(chatInfo);

        } catch (SQLException e) {
            e.printStackTrace();
            initFPlayer(fPlayer);
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
                migrateOldModeratorColumn(conn, "mutes", playerUUID, muteTime, muteReason);

                int banTime = playerResultSet.getInt("tempban_time");
                String banReason = playerResultSet.getString("tempban_reason");
                migrateOldModeratorColumn(conn, "bans", playerUUID, banTime, banReason);
            }

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
            addColumn(statement, "players", "stream", "varchar(11)");
            addColumn(statement, "players", "spy", "varchar(11)");
            dropColumn(statement, "players", "mute_time");
            dropColumn(statement, "players", "mute_reason");
            dropColumn(statement, "players", "tempban_time");
            dropColumn(statement, "players", "tempban_reason");

        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, "Couldn't execute MySQL statement: ", ex);
        }

    }

    private void migrateOldModeratorColumn(Connection conn, String table, String playerUUID, int time, String reason) throws SQLException {
        if (reason == null) return;

        PreparedStatement statement = conn.prepareStatement("INSERT OR REPLACE INTO "+ table + " (player, time, reason, moderator) VALUES(?,?,?,?)");

        statement.setString(1, playerUUID);
        statement.setInt(2, time);
        statement.setString(3, reason);
        statement.setString(4, null);

        statement.executeUpdate();
    }

    private boolean parseBoolean(String configString, String option) {
        FYamlConfiguration config = FileManager.config;

        boolean bool = true;

        if (!config.getString(configString).isEmpty()) {
            bool = config.getBoolean(configString);
        }

        return bool && (option == null || Boolean.parseBoolean(option));
    }

    public int getCountRow(String table) {
        try {
            Connection conn = getSQLConnection();
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT COUNT(1) FROM " + table + " WHERE time>?");
            preparedStatement.setInt(1, ObjectUtil.getCurrentTime());
            ResultSet resultSet = preparedStatement.executeQuery();

            resultSet.next();

            return resultSet.getInt(1);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public ArrayList<String> getPlayerNameList(String table, String column) {
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

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return arrayList;
    }

    public ArrayList<ModInfo> getModInfoList(String table, int limit, int skip) {
        ArrayList<ModInfo> modInfos = new ArrayList<>();
        try (Connection conn = getSQLConnection()) {

            PreparedStatement preparedStatement = conn.prepareStatement("SELECT * FROM " + table + " WHERE time>? LIMIT " + limit + " OFFSET " + skip);
            preparedStatement.setInt(1, ObjectUtil.getCurrentTime());
            ResultSet playerResult = preparedStatement.executeQuery();

            while (playerResult.next()) {

                String playerUUID = playerResult.getString(1);
                int time = playerResult.getInt(2);
                String reason = playerResult.getString(3);
                String moderator = playerResult.getString(4);

                modInfos.add(new ModInfo(playerUUID, time, reason, moderator));
            }

            close(preparedStatement, playerResult);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return modInfos;
    }

    public void updateFPlayer(@NotNull FPlayer fPlayer, @NotNull String column) {
        String playerUUID = fPlayer.getUUID().toString();

        try (Connection conn = getSQLConnection()) {
            PreparedStatement preparedStatement =
                    conn.prepareStatement("UPDATE players SET " + column + "=? WHERE uuid=?");

            switch (column) {
                case "spy" -> {
                    preparedStatement.setString(1, String.valueOf(fPlayer.isSpies()));
                    preparedStatement.setString(2, playerUUID);

                    preparedStatement.executeUpdate();
                }
                case "stream" -> {
                    preparedStatement.setString(1, String.valueOf(fPlayer.isStreaming()));
                    preparedStatement.setString(2, playerUUID);

                    preparedStatement.executeUpdate();
                }
                case "colors" -> {
                    preparedStatement.setString(1, fPlayer.getColors()[0] + "," + fPlayer.getColors()[1]);
                    preparedStatement.setString(2, playerUUID);

                    preparedStatement.executeUpdate();
                }
                case "ignore_list" -> {
                    preparedStatement.setString(1, arrayToString(fPlayer.getIgnoreList()));
                    preparedStatement.setString(2, playerUUID);

                    preparedStatement.executeUpdate();
                }
                case "mails" -> {

                    if (fPlayer.getMails().isEmpty()) {
                        preparedStatement.setString(1, null);
                        preparedStatement.setString(2, playerUUID);
                        preparedStatement.executeUpdate();
                        preparedStatement.close();
                        break;
                    }

                    preparedStatement = connection.prepareStatement("SELECT mails FROM players WHERE uuid=?");
                    preparedStatement.setString(1, playerUUID);
                    ResultSet playerResult = preparedStatement.executeQuery();
                    playerResult.next();

                    String mails = playerResult.getString("mails");

                    saveMails(fPlayer, mails);
                }
            }

        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, "Couldn't execute SQLite statement: ", ex);
        }
    }

    public void saveMails(FPlayer fPlayer, String addMails) {
        try {
            Connection connection = getSQLConnection();

            String playerUUID = fPlayer.getUUID().toString();

            String newMails = arrayToString(new ArrayList<>(fPlayer.getMails().keySet()));

            StringBuilder mailsBuilder = new StringBuilder();
            mailsBuilder.append(addMails != null ? addMails : "");
            mailsBuilder.append(newMails != null ? newMails : "");

            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE players SET mails=? WHERE uuid=?");
            preparedStatement.setString(1, mailsBuilder.toString());
            preparedStatement.setString(2, playerUUID);
            preparedStatement.executeUpdate();

            fPlayer.getMails().forEach((uuid, mail) -> {
                try {
                    PreparedStatement ps2 = connection.prepareStatement("REPLACE INTO mails (uuid,sender,receiver,message) VALUES(?,?,?,?)");
                    ps2.setString(1, mail.getUUID().toString());
                    ps2.setString(2, mail.getSender().toString());
                    ps2.setString(3, mail.getReceiver().toString());
                    ps2.setString(4, mail.getMessage());
                    ps2.executeUpdate();
                    ps2.close();
                } catch (SQLException e) {
                    plugin.getLogger().log(Level.SEVERE, "Couldn't execute MySQL statement: ", e);
                }
            });
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    private String arrayToString(List<UUID> arrayList) {
        StringBuilder ignoreListString = new StringBuilder();
        for (UUID ignoredPlayer : arrayList)
            ignoreListString.append(ignoredPlayer).append(",");

        return ignoreListString.length() == 0 ? null : ignoreListString.toString();
    }

    public void updatePlayerInfo(@NotNull String table, @NotNull Object playerInfo) {
        try {
            Connection connection = getSQLConnection();

            switch (table) {
                case "mutes", "bans" -> {
                    ModInfo modInfo = (ModInfo) playerInfo;

                    PreparedStatement preparedStatement = connection.prepareStatement("REPLACE INTO " + table + " (player, time, reason, moderator) VALUES(?,?,?,?)");

                    preparedStatement.setString(1, modInfo.getPlayer());
                    preparedStatement.setInt(2, modInfo.getTime());
                    preparedStatement.setString(3, modInfo.getReason());
                    preparedStatement.setString(4, modInfo.getModerator());

                    preparedStatement.executeUpdate();
                }
                case "mails" -> {
                    Mail mail = (Mail) playerInfo;
                    deleteRow("mails", "uuid", mail.getUUID().toString());
                }
                case "chats" -> {

                    ChatInfo chatInfo = (ChatInfo) playerInfo;

                    PreparedStatement preparedStatement = connection.prepareStatement("UPDATE players SET enable_advancements=?," +
                            "enable_deaths=?,enable_joins=?,enable_quits=?,enable_command_me=?,enable_command_try=?," +
                            "enable_command_try_cube=?,enable_command_ball=?,enable_command_tempban=?,enable_command_mute=?," +
                            "enable_command_warn=?,enable_command_msg=?,enable_command_reply=?,enable_command_mail=?," +
                            "enable_command_tic_tac_toe=? WHERE uuid=?");

                    preparedStatement.setString(1, chatInfo.getOptionString(("advancement")));
                    preparedStatement.setString(2, chatInfo.getOptionString(("death")));
                    preparedStatement.setString(3, chatInfo.getOptionString(("join")));
                    preparedStatement.setString(4, chatInfo.getOptionString(("quit")));
                    preparedStatement.setString(5, chatInfo.getOptionString(("me")));
                    preparedStatement.setString(6, chatInfo.getOptionString(("try")));
                    preparedStatement.setString(7, chatInfo.getOptionString(("try-cube")));
                    preparedStatement.setString(8, chatInfo.getOptionString(("ball")));
                    preparedStatement.setString(9, chatInfo.getOptionString(("tempban")));
                    preparedStatement.setString(10, chatInfo.getOptionString(("mute")));
                    preparedStatement.setString(11, chatInfo.getOptionString(("warn")));
                    preparedStatement.setString(12, chatInfo.getOptionString(("msg")));
                    preparedStatement.setString(13, chatInfo.getOptionString(("reply")));
                    preparedStatement.setString(14, chatInfo.getOptionString(("mail")));
                    preparedStatement.setString(15, chatInfo.getOptionString(("tic-tac-toe")));
                    preparedStatement.setString(16, chatInfo.getPlayer());

                    preparedStatement.executeUpdate();
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
