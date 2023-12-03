package net.flectone.chat.database.sqlite;

import lombok.Getter;
import net.flectone.chat.FlectoneChat;
import net.flectone.chat.model.file.FConfiguration;
import net.flectone.chat.model.mail.Mail;
import net.flectone.chat.model.player.FPlayer;
import net.flectone.chat.model.player.Moderation;
import net.flectone.chat.model.player.Settings;
import net.flectone.chat.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.util.*;

@Getter
public class Database extends SQLHandler {

    private SQLiteExecutor executor;

    private final FConfiguration config;
    public Database(FlectoneChat plugin) {
        super(plugin.getDataFolder().getAbsolutePath());

        config = FlectoneChat.getPlugin().getFileManager().getConfig();

        connect();

        clearExpiredData();
    }

    public void execute(Runnable runnable) {
        executor.executeRunnable(runnable);
    }

    public void execute(StatementConsumer action) {
        executor.executeStatement(action);
    }

    public void connect() {
        super.connect(config.getString("plugin.database.name"));
    }

    @Override
    public void onConnect() {
        FlectoneChat.info("SQLite DB Connected successfully");
        init();
    }

    public void init() {
        try {
            executor = new SQLiteExecutor(this);

            Statement statement = connection.createStatement();

            statement.executeUpdate("PRAGMA foreign_keys = ON");

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS `players` (" +
                    "`uuid` TEXT NOT NULL UNIQUE, " +
                    "`name` TEXT NOT NULL, " +
                    "`ip` TEXT NOT NULL, " +
                    "PRIMARY KEY (`uuid`)" +
                    ");");

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS `bans` (" +
                    "`id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    "`player` TEXT NOT NULL UNIQUE, " +
                    "`time` INTEGER NOT NULL, " +
                    "`reason` TEXT NOT NULL, " +
                    "`moderator` TEXT, " +
                    "FOREIGN KEY (`player`) REFERENCES `players`(`uuid`), " +
                    "FOREIGN KEY (`moderator`) REFERENCES `players`(`uuid`)" +
                    ");");

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS `warns` (" +
                    "`id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    "`player` TEXT NOT NULL, " +
                    "`time` INTEGER NOT NULL, " +
                    "`reason` TEXT NOT NULL, " +
                    "`moderator` TEXT, " +
                    "FOREIGN KEY (`player`) REFERENCES `players`(`uuid`), " +
                    "FOREIGN KEY (`moderator`) REFERENCES `players`(`uuid`)" +
                    ");");

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS `mutes` (" +
                    "`id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    "`player` TEXT NOT NULL, " +
                    "`time` INTEGER NOT NULL, " +
                    "`reason` TEXT NOT NULL, " +
                    "`moderator` TEXT, " +
                    "FOREIGN KEY (`player`) REFERENCES `players`(`uuid`), " +
                    "FOREIGN KEY (`moderator`) REFERENCES `players`(`uuid`)" +
                    ");");

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS `mails` (" +
                    "`id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE, " +
                    "`sender` TEXT NOT NULL, " +
                    "`receiver` TEXT NOT NULL, " +
                    "`message` TEXT NOT NULL" +
                    ");");

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS `ignores` (" +
                    "`id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    "`initiator` TEXT NOT NULL, " +
                    "`target` TEXT NOT NULL" +
                    ");");

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS `settings` (" +
                    "`uuid` TEXT NOT NULL, " +
                    "`colors` TEXT, " +
                    "`chat` TEXT, " +
                    "`stream` INTEGER, " +
                    "`spy` INTEGER, " +
                    "`enable_advancement` INTEGER, " +
                    "`enable_death` INTEGER, " +
                    "`enable_join` INTEGER, " +
                    "`enable_quit` INTEGER, " +
                    "`enable_auto_message` INTEGER, " +
                    "`enable_command_me` INTEGER, " +
                    "`enable_command_try` INTEGER, " +
                    "`enable_command_dice` INTEGER, " +
                    "`enable_command_ball` INTEGER, " +
                    "`enable_command_ban` INTEGER, " +
                    "`enable_command_mute` INTEGER, " +
                    "`enable_command_warn` INTEGER, " +
                    "`enable_command_tell` INTEGER, " +
                    "`enable_command_reply` INTEGER, " +
                    "`enable_command_mail` INTEGER, " +
                    "`enable_command_tictactoe` INTEGER, " +
                    "`enable_command_kick` INTEGER, " +
                    "`enable_command_translateto` INTEGER," +
                    "PRIMARY KEY (`uuid`), " +
                    "FOREIGN KEY (`uuid`) REFERENCES `players`(`uuid`)" +
                    ");");

            statement.executeUpdate("PRAGMA JOURNAL_MODE=WAL");
            statement.executeUpdate("PRAGMA OPTIMIZE");
            statement.executeUpdate("PRAGMA LOCKING_MODE=EXCLUSIVE");
            statement.executeUpdate("PRAGMA SYNCHRONOUS=EXTRA");
            statement.executeUpdate("PRAGMA WAL_CHECKPOINT(TRUNCATE)");
            statement.executeUpdate("PRAGMA WAL_AUTOCHECKPOINT=100");
            statement.close();

            if (FlectoneChat.getPlugin().getFileManager().isLess420()) {
                migrate420();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void migrate420() {
        try (Connection conn = getConnection()) {
            Statement statement = conn.createStatement();

            addColumn(statement, "settings", "enable_command_translateto", "INTEGER");

        } catch (SQLException ex) {
            FlectoneChat.warning("Couldn't execute MySQL statement: " + ex);
        }
    }

    private void addColumn(@NotNull Statement statement, @NotNull String table, @NotNull String column, @NotNull String type) throws SQLException {
        statement.executeUpdate("ALTER TABLE " + table + " ADD COLUMN " + column + " " + type);
    }

    public void offlineToDatabase(OfflinePlayer offlinePlayer) {
        execute(connection -> {
            PreparedStatement insertStatement = connection.prepareStatement("INSERT OR IGNORE INTO `players`(`uuid`, `name`, `ip`) VALUES (?, ?, ?)");
            insertStatement.setString(1, offlinePlayer.getUniqueId().toString());
            insertStatement.setString(2, offlinePlayer.getName());
            insertStatement.setString(3, "0.0.0.0");
            insertStatement.executeUpdate();
        });
    }

    public void toDatabase(@NotNull FPlayer fPlayer) {
        execute(connection -> {
            PreparedStatement updateStatement = connection.prepareStatement("UPDATE `players` SET `ip` = ? WHERE `uuid` = ?");
            updateStatement.setString(1, fPlayer.getIp());
            updateStatement.setString(2, fPlayer.getUuid().toString());

            if (updateStatement.executeUpdate() == 0) {
                PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO `players`(`uuid`, `name`, `ip`) VALUES (?, ?, ?)");
                insertStatement.setString(1, fPlayer.getUuid().toString());
                insertStatement.setString(2, fPlayer.getMinecraftName());
                insertStatement.setString(3, fPlayer.getIp());
                insertStatement.executeUpdate();
            }
        });
    }

    public void fromDatabase(FPlayer fPlayer) {
        execute(() -> {
            getBan(fPlayer);
            getMute(fPlayer);
            getWarns(fPlayer);
            getIgnores(fPlayer);
            asyncGetMails(fPlayer);
            asyncGetSettings(fPlayer);
        });
    }

    public void getMute(FPlayer fPlayer) {
        try {
            String uuid = fPlayer.getUuid().toString();

            PreparedStatement playerStatement = getConnection().prepareStatement("SELECT `time`, `reason`, `moderator` FROM `mutes` WHERE `player` = ?");
            playerStatement.setString(1, uuid);
            ResultSet playerResult = playerStatement.executeQuery();

            if (playerResult.next()) {
                int time = playerResult.getInt("time");
                String reason = playerResult.getString("reason");
                String moderator = playerResult.getString("moderator");
                fPlayer.setMute(new Moderation(uuid, time, reason, moderator, Moderation.Type.MUTE));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void getBan(@NotNull FPlayer fPlayer) {
        try {
            String uuid = fPlayer.getUuid().toString();

            PreparedStatement playerStatement = getConnection().prepareStatement("SELECT `time`, `reason`, `moderator` FROM `bans` WHERE `player` = ?");
            playerStatement.setString(1, uuid);
            ResultSet playerResult = playerStatement.executeQuery();

            if (playerResult.next()) {
                int time = playerResult.getInt("time");
                String reason = playerResult.getString("reason");
                String moderator = playerResult.getString("moderator");
                fPlayer.setBan(new Moderation(uuid, time, reason, moderator, Moderation.Type.BAN));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void getWarns(@NotNull FPlayer fPlayer) {
        try {
            String uuid = fPlayer.getUuid().toString();

            PreparedStatement playerStatement = getConnection().prepareStatement("SELECT `id`, `time`, `reason`, `moderator` FROM `warns` WHERE `player` = ?");
            playerStatement.setString(1, uuid);
            ResultSet playerResult = playerStatement.executeQuery();

            List<Moderation> warnsList = new ArrayList<>();

            while (playerResult.next()) {
                int id = playerResult.getInt("id");
                int time = playerResult.getInt("time");
                String reason = playerResult.getString("reason");
                String moderator = playerResult.getString("moderator");
                warnsList.add(new Moderation(id, uuid, time, reason, moderator, Moderation.Type.WARN));
            }

            fPlayer.setWarnList(warnsList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeWarn(@NotNull Moderation moderation) {
        try {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM `warns` WHERE `id`=?");
            statement.setString(1, String.valueOf(moderation.getId()));
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void getIgnores(FPlayer fPlayer) {
        execute(connection -> {
            String uuid = fPlayer.getUuid().toString();

            PreparedStatement playerStatement = connection.prepareStatement("SELECT `target` FROM `ignores` WHERE `initiator` = ?");
            playerStatement.setString(1, uuid);
            ResultSet playerResult = playerStatement.executeQuery();

            List<UUID> ignoreList = new ArrayList<>();

            while (playerResult.next()) {
                String target = playerResult.getString("target");
                ignoreList.add(UUID.fromString(target));
            }

            fPlayer.setIgnoreList(ignoreList);
        });
    }

    public void addIgnore(@NotNull UUID initiator, @NotNull UUID target) {
        execute(connection -> {
            PreparedStatement statement = connection.prepareStatement("INSERT OR IGNORE INTO `ignores` (`initiator`, `target`) VALUES (?, ?)");
            statement.setString(1, initiator.toString());
            statement.setString(2, target.toString());
            statement.executeUpdate();
        });
    }

    public void removeIgnore(@NotNull UUID initiator, @NotNull UUID target) {
        execute(connection -> {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM `ignores` WHERE `initiator`=? AND `target`=?");
            statement.setString(1, initiator.toString());
            statement.setString(2, target.toString());
            statement.executeUpdate();
        });
    }
    public void asyncGetMails(FPlayer fPlayer) {
        execute(connection -> getMails(fPlayer));
    }

    public void getMails(@NotNull FPlayer fPlayer) {
        try {
            String uuid = fPlayer.getUuid().toString();

            PreparedStatement playerStatement = getConnection().prepareStatement("SELECT * FROM `mails` WHERE `receiver` = ?");
            playerStatement.setString(1, uuid);
            ResultSet playerResult = playerStatement.executeQuery();

            List<Mail> mailList = new ArrayList<>();

            while (playerResult.next()) {
                int id = playerResult.getInt("id");
                String sender = playerResult.getString("sender");
                String message = playerResult.getString("message");
                mailList.add(new Mail(id, sender, uuid, message));
            }

            fPlayer.setMailList(mailList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addMail(@NotNull Mail mail) {
        execute(connection -> {
            PreparedStatement statement = connection.prepareStatement("INSERT OR IGNORE INTO `mails` (`sender`, `receiver`, `message`) VALUES (?, ?, ?)");
            statement.setString(1, mail.getSender());
            statement.setString(2, mail.getReceiver());
            statement.setString(3, mail.getMessage());
            statement.executeUpdate();
        });
    }

    public void removeMail(@NotNull Mail mail) {
        execute(connection -> {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM `mails` WHERE `id`=?");
            statement.setString(1, String.valueOf(mail.getId()));
            statement.executeUpdate();
        });
    }

    public void asyncGetSettings(FPlayer fPlayer) {
        execute(() -> getSettings(fPlayer));
    }

    public void getSettings(@NotNull FPlayer fPlayer) {
        try {
            String uuid = fPlayer.getUuid().toString();

            PreparedStatement playerStatement = getConnection().prepareStatement("SELECT * FROM `settings` WHERE `uuid` = ?");
            playerStatement.setString(1, uuid);
            ResultSet playerResult = playerStatement.executeQuery();

            Settings settings = new Settings();

            if (playerResult.next()) {
                String colors = playerResult.getString(Settings.Type.COLORS.toString());
                if (colors != null) {
                    HashMap<String, String> colorList = new HashMap<>();
                    for (String color : colors.split(",")) {
                        String[] entry = color.split(":");
                        colorList.put(entry[0], entry[1]);
                    }
                    settings.add(Settings.Type.COLORS, colorList);
                }

                String chat = playerResult.getString(Settings.Type.CHAT.toString());
                if (chat != null) {
                    settings.add(Settings.Type.CHAT, chat);
                }

                getSettingInt(playerResult, Settings.Type.STREAM, settings);
                getSettingInt(playerResult, Settings.Type.SPY, settings);
                getSettingInt(playerResult, Settings.Type.ADVANCEMENT, settings);
                getSettingInt(playerResult, Settings.Type.DEATH, settings);
                getSettingInt(playerResult, Settings.Type.JOIN, settings);
                getSettingInt(playerResult, Settings.Type.QUIT, settings);
                getSettingInt(playerResult, Settings.Type.AUTO_MESSAGE, settings);
                getSettingInt(playerResult, Settings.Type.COMMAND_ME, settings);
                getSettingInt(playerResult, Settings.Type.COMMAND_TRY, settings);
                getSettingInt(playerResult, Settings.Type.COMMAND_DICE, settings);
                getSettingInt(playerResult, Settings.Type.COMMAND_BALL, settings);
                getSettingInt(playerResult, Settings.Type.COMMAND_BAN, settings);
                getSettingInt(playerResult, Settings.Type.COMMAND_MUTE, settings);
                getSettingInt(playerResult, Settings.Type.COMMAND_WARN, settings);
                getSettingInt(playerResult, Settings.Type.COMMAND_TELL, settings);
                getSettingInt(playerResult, Settings.Type.COMMAND_REPLY, settings);
                getSettingInt(playerResult, Settings.Type.COMMAND_MAIL, settings);
                getSettingInt(playerResult, Settings.Type.COMMAND_TICTACTOE, settings);
                getSettingInt(playerResult, Settings.Type.COMMAND_KICK, settings);
                getSettingInt(playerResult, Settings.Type.COMMAND_TRANSLATETO, settings);
            }

            fPlayer.setSettings(settings);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void getSettingInt(ResultSet resultSet, Settings.Type type, Settings settings) throws SQLException {
        int value = resultSet.getInt(type.toString());
        if (value != 0) {
            settings.add(type, value);
        }
    }

    public int getCountRow(@NotNull String table) {
        try {

            String filter = " WHERE time>?";
            if (table.equals("bans")) filter += " OR time=-1";

            PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT COUNT(1) FROM " + table + filter);
            preparedStatement.setInt(1, TimeUtil.getCurrentTime());
            ResultSet resultSet = preparedStatement.executeQuery();

            resultSet.next();

            return resultSet.getInt(1);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @NotNull
    public ArrayList<Moderation> getModerationList(@NotNull String table, int limit, int skip, @NotNull Moderation.Type type) {
        ArrayList<Moderation> playerMods = new ArrayList<>();
        try (Connection conn = getConnection()) {

            String filter = " WHERE time>?";
            if (table.equals("bans")) filter += " OR time=-1";

            PreparedStatement preparedStatement = conn.prepareStatement("SELECT * FROM " + table + filter + " LIMIT " + limit + " OFFSET " + skip);
            preparedStatement.setInt(1, TimeUtil.getCurrentTime());
            ResultSet playerResult = preparedStatement.executeQuery();

            while (playerResult.next()) {

                int id = playerResult.getInt(1);
                String playerUUID = playerResult.getString(2);
                int time = playerResult.getInt(3);
                String reason = playerResult.getString(4);
                String moderator = playerResult.getString(5);

                playerMods.add(new Moderation(id, playerUUID, time, reason, moderator, type));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return playerMods;
    }

    @Nullable
    public Moderation getPlayerInfo(@NotNull String table, @NotNull String column, @NotNull String filter, @NotNull Moderation.Type type) {
        try (Connection conn = getConnection()) {
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT * FROM " + table + " WHERE " + column + " = ?");
            preparedStatement.setString(1, filter);
            ResultSet playerResult = preparedStatement.executeQuery();

            if (!playerResult.next()) return null;

            int time = playerResult.getInt("time");
            String reason = playerResult.getString("reason");
            String moderator = playerResult.getString("moderator");
            return new Moderation(filter, time, reason, moderator, type);

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void updateSettings(@NotNull FPlayer fPlayer, @NotNull String column) {
        String playerUUID = fPlayer.getPlayer().getUniqueId().toString();

        try (Connection conn = getConnection()) {
            PreparedStatement preparedStatement =
                    conn.prepareStatement("UPDATE `settings` SET " + column + "=? WHERE uuid=?");

            if (column.equals("colors")) {
                Settings settings = fPlayer.getSettings();
                if (settings.getColors() == null) return;

                StringBuilder stringBuilder = new StringBuilder();
                for (Map.Entry<String, String> entry : settings.getColors().entrySet()) {
                    stringBuilder
                            .append(entry.getKey())
                            .append(":")
                            .append(entry.getValue())
                            .append(",");
                }

                String colors = stringBuilder.substring(0, stringBuilder.length() - 1);

                preparedStatement.setString(1, colors);
                preparedStatement.setString(2, playerUUID);

                if (preparedStatement.executeLargeUpdate() == 0) {
                    PreparedStatement insertStatement = conn.prepareStatement("INSERT INTO `settings` (`uuid`, `colors`) VALUES (?, ?)");

                    insertStatement.setString(1, playerUUID);
                    insertStatement.setString(2, colors);
                    insertStatement.executeUpdate();
                }
            }

        } catch (SQLException ex) {
            FlectoneChat.warning( "Couldn't execute SQLite statement: ");
            ex.printStackTrace();
        }
    }

    public void updateFPlayer(@NotNull String table, @NotNull Object playerInfo) {
        try {
            Connection connection = getConnection();

            switch (table) {
                case "mutes", "bans" -> {
                    Moderation playerMod = (Moderation) playerInfo;

                    PreparedStatement preparedStatement = connection.prepareStatement("REPLACE INTO " + table + " (player, time, reason, moderator) VALUES(?,?,?,?)");

                    preparedStatement.setString(1, playerMod.getPlayerUUID());
                    preparedStatement.setInt(2, playerMod.getTime());
                    preparedStatement.setString(3, playerMod.getReason());
                    preparedStatement.setString(4, playerMod.getModeratorUUID());

                    preparedStatement.executeUpdate();
                }
                case "warns" -> {
                    Moderation warn = (Moderation) playerInfo;

                    PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO `warns` (`player`, `time`, `reason`, `moderator`) VALUES (?, ?, ?, ?)");
                    preparedStatement.setString(1, warn.getPlayerUUID());
                    preparedStatement.setInt(2, warn.getTime());
                    preparedStatement.setString(3, warn.getReason());
                    preparedStatement.setString(4, warn.getModeratorUUID());

                    preparedStatement.executeUpdate();
                }
                case "spy" -> {
                    PreparedStatement preparedStatement = connection.prepareStatement("UPDATE `settings` SET `spy`=? WHERE `uuid`=?");
                    FPlayer fPlayer = (FPlayer) playerInfo;
                    Settings settings = fPlayer.getSettings();

                    preparedStatement.setString(1, settings.getValue(Settings.Type.SPY));
                    preparedStatement.setString(2, fPlayer.getUuid().toString());

                    if (preparedStatement.executeUpdate() == 0) {

                        PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO `settings` (`spy`, `uuid`) VALUES (?, ?)");
                        insertStatement.setString(1, settings.getValue(Settings.Type.SPY));
                        insertStatement.setString(2, fPlayer.getUuid().toString());
                        insertStatement.executeUpdate();
                    }
                }
                case "stream" -> {
                    PreparedStatement preparedStatement = connection.prepareStatement("UPDATE `settings` SET `stream`=? WHERE `uuid`=?");
                    FPlayer fPlayer = (FPlayer) playerInfo;
                    Settings settings = fPlayer.getSettings();

                    preparedStatement.setString(1, settings.getValue(Settings.Type.STREAM));
                    preparedStatement.setString(2, fPlayer.getUuid().toString());

                    if (preparedStatement.executeUpdate() == 0) {

                        PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO `settings` (`stream`, `uuid`) VALUES (?, ?)");
                        insertStatement.setString(1, settings.getValue(Settings.Type.STREAM));
                        insertStatement.setString(2, fPlayer.getUuid().toString());
                        insertStatement.executeUpdate();
                    }
                }
                case "settings" -> {

                    PreparedStatement preparedStatement = connection.prepareStatement("UPDATE `settings` SET " +
                            "enable_advancement=?," +
                            "enable_death=?," +
                            "enable_join=?," +
                            "enable_quit=?," +
                            "enable_command_me=?," +
                            "enable_command_try=?," +
                            "enable_command_dice=?," +
                            "enable_command_ball=?," +
                            "enable_command_ban=?," +
                            "enable_command_mute=?," +
                            "enable_command_warn=?," +
                            "enable_command_tell=?," +
                            "enable_command_reply=?," +
                            "enable_command_mail=?," +
                            "enable_command_tictactoe=?," +
                            "enable_command_kick=?," +
                            "enable_auto_message=?," +
                            "chat=?" +
                            "WHERE uuid=?");

                    FPlayer fPlayer = (FPlayer) playerInfo;
                    Settings settings = fPlayer.getSettings();

                    setSettingsForStatement(fPlayer.getUuid().toString(), settings, preparedStatement);

                    if (preparedStatement.executeUpdate() == 0) {
                        PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO `settings` (" +
                                "enable_advancement," +
                                "enable_death," +
                                "enable_join," +
                                "enable_quit," +
                                "enable_command_me," +
                                "enable_command_try," +
                                "enable_command_dice," +
                                "enable_command_ball," +
                                "enable_command_ban," +
                                "enable_command_mute," +
                                "enable_command_warn," +
                                "enable_command_tell," +
                                "enable_command_reply," +
                                "enable_command_mail," +
                                "enable_command_tictactoe," +
                                "enable_command_kick," +
                                "enable_auto_message," +
                                "chat," +
                                "uuid" +
                                ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
                        setSettingsForStatement(fPlayer.getUuid().toString(), settings, insertStatement);
                        insertStatement.executeUpdate();
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setSettingsForStatement(@NotNull String uuid, @NotNull Settings settings, @NotNull PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.setString(1, settings.getValue(Settings.Type.ADVANCEMENT));
        preparedStatement.setString(2, settings.getValue(Settings.Type.DEATH));
        preparedStatement.setString(3, settings.getValue(Settings.Type.JOIN));
        preparedStatement.setString(4, settings.getValue(Settings.Type.QUIT));
        preparedStatement.setString(5, settings.getValue(Settings.Type.COMMAND_ME));
        preparedStatement.setString(6, settings.getValue(Settings.Type.COMMAND_TRY));
        preparedStatement.setString(7, settings.getValue(Settings.Type.COMMAND_DICE));
        preparedStatement.setString(8, settings.getValue(Settings.Type.COMMAND_BALL));
        preparedStatement.setString(9, settings.getValue(Settings.Type.COMMAND_BAN));
        preparedStatement.setString(10, settings.getValue(Settings.Type.COMMAND_MUTE));
        preparedStatement.setString(11, settings.getValue(Settings.Type.COMMAND_WARN));
        preparedStatement.setString(12, settings.getValue(Settings.Type.COMMAND_TELL));
        preparedStatement.setString(13, settings.getValue(Settings.Type.COMMAND_REPLY));
        preparedStatement.setString(14, settings.getValue(Settings.Type.COMMAND_MAIL));
        preparedStatement.setString(15, settings.getValue(Settings.Type.COMMAND_TICTACTOE));
        preparedStatement.setString(16, settings.getValue(Settings.Type.COMMAND_KICK));
        preparedStatement.setString(17, settings.getValue(Settings.Type.AUTO_MESSAGE));
        preparedStatement.setString(18, settings.getValue(Settings.Type.CHAT));
        preparedStatement.setString(19, uuid);
    }

    public void deleteRow(@NotNull String table, @NotNull String column, @NotNull String filter) {
        try {
            Connection connection = getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM " + table + " WHERE " + column + " = ?");

            preparedStatement.setString(1, filter);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void clearExpiredData() {
        List.of("bans", "warns", "mutes").forEach(table -> {
            try {
                Connection connection = getConnection();
                String filter = " WHERE time<=?";
                if (table.equals("bans")) filter += " AND time!=-1";
                PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM " + table + filter);
                preparedStatement.setInt(1, TimeUtil.getCurrentTime());

                preparedStatement.executeUpdate();

            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        });
    }

    public void loadOfflinePlayersToDatabase() {
        for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
            this.offlineToDatabase(offlinePlayer);
        }
    }
}
