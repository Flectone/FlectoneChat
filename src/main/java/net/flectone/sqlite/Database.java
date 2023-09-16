package net.flectone.sqlite;

import net.flectone.Main;
import net.flectone.commands.CommandChatcolor;
import net.flectone.managers.FileManager;
import net.flectone.misc.entity.FPlayer;
import net.flectone.misc.entity.player.PlayerChat;
import net.flectone.misc.entity.player.PlayerMail;
import net.flectone.misc.entity.player.PlayerMod;
import net.flectone.misc.entity.player.PlayerWarn;
import net.flectone.misc.files.FYamlConfiguration;
import net.flectone.utils.ObjectUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

public abstract class Database {
    private static boolean migrate3_9_0 = false;

    public static void setMigrate3_9_0(boolean migrate3_9_0) {
        Database.migrate3_9_0 = migrate3_9_0;
    }

    private static boolean migrate3_10_1 = false;

    public static void setMigrate3_10_1(boolean migrate3_10_1) {
        Database.migrate3_10_1 = migrate3_10_1;
    }

    private static boolean migrate3_10_3 = false;

    public static void setMigrate3_10_3(boolean migrate3_10_3) {
        Database.migrate3_10_3 = migrate3_10_3;
    }

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

            if (migrate3_9_0) migrateDatabase3_9_0();
            if (migrate3_10_1) migrateDatabase3_10_1();
            if (migrate3_10_3) migrateDatabase3_10_3();

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

    public void clearOldRows(@NotNull String table) {
        try {
            Connection connection = getSQLConnection();
            String filter = " WHERE time<=?";
            if (table.equals("bans")) filter += " AND time!=-1";
            PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM " + table + filter);
            preparedStatement.setInt(1, ObjectUtil.getCurrentTime());

            preparedStatement.executeUpdate();

        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    private void dropColumn(@NotNull Statement statement, @NotNull String table, @NotNull String column) throws SQLException {
        statement.executeUpdate("ALTER TABLE " + table + " DROP COLUMN " + column);
    }

    private void addColumn(@NotNull Statement statement, @NotNull String table, @NotNull String column, @NotNull String type) throws SQLException {
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
    public PlayerMod getPlayerInfo(@NotNull String table, @NotNull String column, @NotNull String filter) {
        try (Connection conn = getSQLConnection()) {
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT * FROM " + table + " WHERE " + column + " = ?");
            preparedStatement.setString(1, filter);
            ResultSet playerResult = preparedStatement.executeQuery();

            if (!playerResult.next()) return null;

            int time = playerResult.getInt(2);
            String reason = playerResult.getString(3);
            String moderator = playerResult.getString(4);
            return new PlayerMod(filter, time, reason, moderator);

        } catch (SQLException e) {
            e.printStackTrace();
            return getPlayerInfo(table, column, filter);
        }
    }

    public void loadPlayersTable(@NotNull FPlayer fPlayer) {
        try {
            Connection connection = getSQLConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM players WHERE uuid = ?");
            preparedStatement.setString(1, fPlayer.getUUID().toString());

            ResultSet playerResult = preparedStatement.executeQuery();
            if (!(playerResult.next())) return;
            if (playerResult.isClosed()) return;

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

                    fPlayer.addMail(UUID.fromString(uuid), new PlayerMail(UUID.fromString(uuid),
                            UUID.fromString(resultMail.getString("sender")),
                            UUID.fromString(resultMail.getString("receiver")),
                            resultMail.getString("message")));
                }
            }

            String warn = playerResult.getString("warns");
            if (warn != null) {
                String[] warns = warn.split(",");

                for (String uuid : warns) {
                    if (uuid.isEmpty()) continue;

                    PreparedStatement ps2 = connection.prepareStatement("SELECT * FROM warns WHERE uuid = ?");
                    ps2.setString(1, uuid);

                    ResultSet resultMail = ps2.executeQuery();
                    resultMail.next();

                    fPlayer.addWarn(new PlayerWarn(UUID.fromString(uuid),
                            resultMail.getString("player"),
                            resultMail.getInt("time"),
                            resultMail.getString("reason"),
                            resultMail.getString("moderator")));
                }
            }

            PlayerChat playerChat = new PlayerChat(fPlayer.getUUID().toString());

            String chat = playerResult.getString("chat");
            playerChat.setChatType(chat == null ? "local" : chat);

            String option = playerResult.getString("enable_advancements");
            playerChat.setOption("advancement", parseBoolean("advancement.message.enable", option));

            option = playerResult.getString("enable_deaths");
            playerChat.setOption("death", parseBoolean("death.message.enable", option));

            option = playerResult.getString("enable_joins");
            playerChat.setOption("join", parseBoolean("player.join.message.enable", option));

            option = playerResult.getString("enable_quits");
            playerChat.setOption("quit", parseBoolean("player.quit.message.enable", option));

            option = playerResult.getString("enable_command_me");
            playerChat.setOption("me", parseBoolean("command.me.enable", option));

            option = playerResult.getString("enable_command_try");
            playerChat.setOption("try", parseBoolean("command.try.enable", option));

            option = playerResult.getString("enable_command_try_cube");
            playerChat.setOption("try-cube", parseBoolean("command.try-cube.enable", option));

            option = playerResult.getString("enable_command_ball");
            playerChat.setOption("ball", parseBoolean("command.ball.enable", option));

            option = playerResult.getString("enable_command_tempban");
            playerChat.setOption("tempban", parseBoolean("command.tempban.enable", option));

            option = playerResult.getString("enable_command_mute");
            playerChat.setOption("mute", parseBoolean("command.mute.enable", option));

            option = playerResult.getString("enable_command_warn");
            playerChat.setOption("warn", parseBoolean("command.warn.enable", option));

            option = playerResult.getString("enable_command_msg");
            playerChat.setOption("msg", parseBoolean("command.msg.enable", option));

            option = playerResult.getString("enable_command_reply");
            playerChat.setOption("reply", parseBoolean("command.reply.enable", option));

            option = playerResult.getString("enable_command_mail");
            playerChat.setOption("mail", parseBoolean("command.mail.enable", option));

            option = playerResult.getString("enable_command_tic_tac_toe");
            playerChat.setOption("tic-tac-toe", parseBoolean("command.tic-tac-toe.enable", option));

            option = playerResult.getString("enable_command_kick");
            playerChat.setOption("kick", parseBoolean("command.kick.enable", option));

            option = playerResult.getString("enable_auto_message");
            playerChat.setOption("auto-message", parseBoolean("chat.auto-message.enable", option));

            if (fPlayer.hasPermission("flectonechat.stream")) {
                fPlayer.setStreaming(Boolean.parseBoolean(playerResult.getString("stream")));
            }

            if (fPlayer.hasPermission("flectonechat.spy")) {
                fPlayer.setSpies(Boolean.parseBoolean(playerResult.getString("spy")));
            }

            fPlayer.setChatInfo(playerChat);

        } catch (SQLException e) {
            e.printStackTrace();
            loadPlayersTable(fPlayer);
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

    private void migrateDatabase3_10_1() {
        try (Connection conn = getSQLConnection()) {
            Statement statement = conn.createStatement();

            addColumn(statement, "players", "enable_command_kick", "varchar(11)");

        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, "Couldn't execute MySQL statement: ", ex);
        }

    }

    private void migrateDatabase3_10_3() {
        try (Connection conn = getSQLConnection()) {
            Statement statement = conn.createStatement();

            addColumn(statement, "players", "enable_auto_message", "varchar(11)");

        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, "Couldn't execute MySQL statement: ", ex);
        }

    }

    private void migrateOldModeratorColumn(@NotNull Connection conn, @NotNull String table, @NotNull String playerUUID, int time, String reason) throws SQLException {
        if (reason == null) return;

        PreparedStatement statement = conn.prepareStatement("INSERT OR REPLACE INTO "+ table + " (player, time, reason, moderator) VALUES(?,?,?,?)");

        statement.setString(1, playerUUID);
        statement.setInt(2, time);
        statement.setString(3, reason);
        statement.setString(4, null);

        statement.executeUpdate();
    }

    private boolean parseBoolean(@NotNull String configString, @Nullable String option) {
        FYamlConfiguration config = FileManager.config;

        boolean bool = true;

        if (!config.getString(configString).isEmpty()) {
            bool = config.getBoolean(configString);
        }

        return bool && (option == null || Boolean.parseBoolean(option));
    }

    public int getCountRow(@NotNull String table) {
        try {
            Connection conn = getSQLConnection();

            String filter = " WHERE time>?";
            if (table.equals("bans")) filter += " OR time=-1";

            PreparedStatement preparedStatement = conn.prepareStatement("SELECT COUNT(1) FROM " + table + filter);
            preparedStatement.setInt(1, ObjectUtil.getCurrentTime());
            ResultSet resultSet = preparedStatement.executeQuery();

            resultSet.next();

            return resultSet.getInt(1);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @NotNull
    public ArrayList<String> getPlayerNameList(@NotNull String table, @NotNull String column) {
        ArrayList<String> arrayList = new ArrayList<>();
        try {
            Connection conn = getSQLConnection();

            String filter = " WHERE time>?";
            if (table.equals("bans")) filter += " OR time=-1";

            PreparedStatement preparedStatement = conn.prepareStatement("SELECT " + column + " FROM " + table + filter);
            preparedStatement.setInt(1, ObjectUtil.getCurrentTime());

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

    @NotNull
    public ArrayList<PlayerMod> getModInfoList(@NotNull String table, int limit, int skip) {
        ArrayList<PlayerMod> playerMods = new ArrayList<>();
        try (Connection conn = getSQLConnection()) {

            String filter = " WHERE time>?";
            if (table.equals("bans")) filter += " OR time=-1";

            PreparedStatement preparedStatement = conn.prepareStatement("SELECT * FROM " + table + filter + " LIMIT " + limit + " OFFSET " + skip);
            preparedStatement.setInt(1, ObjectUtil.getCurrentTime());
            ResultSet playerResult = preparedStatement.executeQuery();

            while (playerResult.next()) {

                String playerUUID = playerResult.getString(1);
                int time = playerResult.getInt(2);
                String reason = playerResult.getString(3);
                String moderator = playerResult.getString(4);

                playerMods.add(new PlayerMod(playerUUID, time, reason, moderator));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return playerMods;
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

                    saveMails(fPlayer);
                }

                case "warns" -> {
                    if (fPlayer.getWarnList().isEmpty()) {
                        preparedStatement.setString(1, null);
                        preparedStatement.setString(2, playerUUID);
                        preparedStatement.executeUpdate();
                        preparedStatement.close();
                        break;
                    }

                    saveWarns(fPlayer);
                }
            }

        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, "Couldn't execute SQLite statement: ", ex);
        }
    }

    public void saveWarns(@NotNull FPlayer fPlayer) {
        try {
            Connection connection = getSQLConnection();

            String playerUUID = fPlayer.getUUID().toString();

            String newWarns = arrayToString(fPlayer.getWarnList().stream().map(PlayerWarn::getUUID).toList());

            String warnsUUIDS = (newWarns != null ? newWarns : "");

            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE players SET warns=? WHERE uuid=?");
            preparedStatement.setString(1, warnsUUIDS);
            preparedStatement.setString(2, playerUUID);
            preparedStatement.executeUpdate();

            fPlayer.getWarnList().forEach(warn -> {
                try {
                    PreparedStatement ps2 = connection.prepareStatement("REPLACE INTO warns (uuid,player,time,reason,moderator) VALUES(?,?,?,?,?)");
                    ps2.setString(1, warn.getUUID().toString());
                    ps2.setString(2, warn.getPlayer());
                    ps2.setInt(3, warn.getTime());
                    ps2.setString(4, warn.getReason());
                    ps2.setString(5, warn.getModerator());
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

    public void saveMails(@NotNull FPlayer fPlayer) {
        try {
            Connection connection = getSQLConnection();

            String playerUUID = fPlayer.getUUID().toString();

            String newMails = arrayToString(new ArrayList<>(fPlayer.getMails().keySet()));

            String mailsBuilder = (newMails != null ? newMails : "");

            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE players SET mails=? WHERE uuid=?");
            preparedStatement.setString(1, mailsBuilder);
            preparedStatement.setString(2, playerUUID);
            preparedStatement.executeUpdate();

            fPlayer.getMails().forEach((uuid, playerMail) -> {
                try {
                    PreparedStatement ps2 = connection.prepareStatement("REPLACE INTO mails (uuid,sender,receiver,message) VALUES(?,?,?,?)");
                    ps2.setString(1, playerMail.getUUID().toString());
                    ps2.setString(2, playerMail.getSender().toString());
                    ps2.setString(3, playerMail.getReceiver().toString());
                    ps2.setString(4, playerMail.getMessage());
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

    @Nullable
    private String arrayToString(@NotNull List<UUID> arrayList) {
        StringBuilder ignoreListString = new StringBuilder();
        for (UUID ignoredPlayer : arrayList)
            ignoreListString.append(ignoredPlayer).append(",");

        return ignoreListString.isEmpty() ? null : ignoreListString.toString();
    }

    public void updatePlayerInfo(@NotNull String table, @NotNull Object playerInfo) {
        try {
            Connection connection = getSQLConnection();

            switch (table) {
                case "mutes", "bans" -> {
                    PlayerMod playerMod = (PlayerMod) playerInfo;

                    PreparedStatement preparedStatement = connection.prepareStatement("REPLACE INTO " + table + " (player, time, reason, moderator) VALUES(?,?,?,?)");

                    preparedStatement.setString(1, playerMod.getPlayer());
                    preparedStatement.setInt(2, playerMod.getTime());
                    preparedStatement.setString(3, playerMod.getReason());
                    preparedStatement.setString(4, playerMod.getModerator());

                    preparedStatement.executeUpdate();
                }
                case "mails" -> {
                    PlayerMail playerMail = (PlayerMail) playerInfo;
                    deleteRow("mails", "uuid", playerMail.getUUID().toString());
                }
                case "warns" -> {
                    PlayerWarn playerWarn = (PlayerWarn) playerInfo;
                    deleteRow("warns", "uuid", playerWarn.getUUID().toString());
                }
                case "chats" -> {

                    PlayerChat playerChat = (PlayerChat) playerInfo;

                    PreparedStatement preparedStatement = connection.prepareStatement("UPDATE players SET enable_advancements=?," +
                            "enable_deaths=?,enable_joins=?,enable_quits=?,enable_command_me=?,enable_command_try=?," +
                            "enable_command_try_cube=?,enable_command_ball=?,enable_command_tempban=?,enable_command_mute=?," +
                            "enable_command_warn=?,enable_command_msg=?,enable_command_reply=?,enable_command_mail=?," +
                            "enable_command_tic_tac_toe=?, enable_command_kick=?, enable_auto_message=? WHERE uuid=?");

                    preparedStatement.setString(1, playerChat.getOptionString("advancement"));
                    preparedStatement.setString(2, playerChat.getOptionString("death"));
                    preparedStatement.setString(3, playerChat.getOptionString("join"));
                    preparedStatement.setString(4, playerChat.getOptionString("quit"));
                    preparedStatement.setString(5, playerChat.getOptionString("me"));
                    preparedStatement.setString(6, playerChat.getOptionString("try"));
                    preparedStatement.setString(7, playerChat.getOptionString("try-cube"));
                    preparedStatement.setString(8, playerChat.getOptionString("ball"));
                    preparedStatement.setString(9, playerChat.getOptionString("tempban"));
                    preparedStatement.setString(10, playerChat.getOptionString("mute"));
                    preparedStatement.setString(11, playerChat.getOptionString("warn"));
                    preparedStatement.setString(12, playerChat.getOptionString("msg"));
                    preparedStatement.setString(13, playerChat.getOptionString("reply"));
                    preparedStatement.setString(14, playerChat.getOptionString("mail"));
                    preparedStatement.setString(15, playerChat.getOptionString("tic-tac-toe"));
                    preparedStatement.setString(16, playerChat.getOptionString("kick"));
                    preparedStatement.setString(17, playerChat.getOptionString("auto-message"));
                    preparedStatement.setString(18, playerChat.getPlayer());

                    preparedStatement.executeUpdate();
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
