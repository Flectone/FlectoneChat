package net.flectone.sqlite;

import net.flectone.Main;
import net.flectone.commands.CommandChatcolor;
import net.flectone.misc.entity.FPlayer;
import net.flectone.misc.actions.Mail;
import net.flectone.managers.FPlayerManager;
import net.flectone.utils.ObjectUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
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

            Arrays.stream(Bukkit.getOfflinePlayers())
                    .forEach(offlinePlayer -> setPlayer(offlinePlayer.getUniqueId()));

        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, "Unable to retrieve connection", ex);
        }
    }

    private void loadOldConfigs() {
        Main.info("\uD83D\uDCCA Migrating old configs to database");
        ArrayList<FPlayer> fPlayers = new ArrayList<>();

        File themeFile = new File(plugin.getDataFolder(), "themes.yml");
        if (themeFile.exists()) {
            FileConfiguration themesConfiguration = YamlConfiguration.loadConfiguration(themeFile);
            for (String uuid : themesConfiguration.getKeys(true)) {
                FPlayer fPlayer = FPlayerManager.getPlayer(UUID.fromString(uuid));
                List<String> stringList = themesConfiguration.getStringList(uuid);
                if (fPlayer == null || stringList.size() < 2) continue;

                fPlayer.setColors(stringList.get(0), stringList.get(1));
                fPlayers.add(fPlayer);
            }
        }

        File muteFile = new File(plugin.getDataFolder(), "mutes.yml");
        if (muteFile.exists()) {
            FileConfiguration muteConfiguration = YamlConfiguration.loadConfiguration(muteFile);
            for (String uuid : muteConfiguration.getKeys(true)) {
                FPlayer fPlayer = FPlayerManager.getPlayer(UUID.fromString(uuid));
                List<String> stringList = muteConfiguration.getStringList(uuid);
                if (fPlayer == null || stringList.size() < 2) continue;

                fPlayer.setMuteReason(stringList.get(0));
                fPlayer.setMuteTime(Integer.parseInt(stringList.get(1)));
                fPlayers.add(fPlayer);
            }
        }

        File ignoreFile = new File(plugin.getDataFolder(), "ignores.yml");
        if (ignoreFile.exists()) {
            FileConfiguration ignoreConfiguration = YamlConfiguration.loadConfiguration(ignoreFile);
            for (String uuid : ignoreConfiguration.getKeys(true)) {
                FPlayer fPlayer = FPlayerManager.getPlayer(UUID.fromString(uuid));
                List<UUID> stringList = ignoreConfiguration.getStringList(uuid).stream().map(UUID::fromString).toList();
                if (fPlayer == null || stringList.isEmpty()) continue;

                fPlayer.setIgnoreList(new ArrayList<>(stringList));
                fPlayers.add(fPlayer);
            }
        }

        File mailFile = new File(plugin.getDataFolder(), "mails.yml");
        if (mailFile.exists()) {
            FileConfiguration mailConfiguration = YamlConfiguration.loadConfiguration(mailFile);
            for (String uuid : mailConfiguration.getKeys(true)) {
                if (!uuid.contains(".")) continue;
                String[] uuids = uuid.split("\\.");
                FPlayer firstFPlayer = FPlayerManager.getPlayer(UUID.fromString(uuids[0]));
                FPlayer secondFPlayer = FPlayerManager.getPlayer(UUID.fromString(uuids[1]));

                if (firstFPlayer == null || secondFPlayer == null) continue;

                List<String> stringList = mailConfiguration.getStringList(uuid);
                for (String message : stringList) {
                    Mail mail = new Mail(secondFPlayer.getUUID(), firstFPlayer.getUUID(), message);
                    firstFPlayer.addMail(mail.getUUID(), mail);
                }
                fPlayers.add(firstFPlayer);
            }
        }

        fPlayers.forEach(this::uploadDatabase);
        Main.info("\uD83D\uDCCA Migration of old configs to database is finished");
    }

    public void setPlayer(@NotNull UUID uuid) {
        try (Connection conn = getSQLConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT OR IGNORE INTO players (uuid) VALUES(?)")) {

            ps.setString(1, uuid.toString());

            ps.executeUpdate();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, "Couldn't execute MySQL statement: ", ex);
        }
    }


    public void loadDatabase() {
        Main.info("\uD83D\uDCCA Start loading database");
        try (Connection conn = getSQLConnection();
             PreparedStatement ps1 = conn.prepareStatement("SELECT * FROM players")) {

            ResultSet resultSet = ps1.executeQuery();

            while (resultSet.next()) {

                FPlayer fPlayer = FPlayerManager.getPlayer(UUID.fromString(resultSet.getString("uuid")));
                if (fPlayer == null) continue;

                String color = resultSet.getString("colors");

                String[] colors = color == null ? CommandChatcolor.getDefaultColors() : color.split(",");
                fPlayer.setColors(colors[0], colors[1]);

                String ignoreList = resultSet.getString("ignore_list");

                ArrayList<UUID> arrayList = ignoreList == null
                        ? new ArrayList<>()
                        : new ArrayList<>(Arrays.stream(ignoreList.split(","))
                        .map(UUID::fromString).collect(Collectors.toList()));

                fPlayer.setIgnoreList(arrayList);
                fPlayer.setMuteTime(resultSet.getInt("mute_time"));
                fPlayer.setMuteReason(resultSet.getString("mute_reason"));

                if (fPlayer.isMuted()) FPlayerManager.getMutedPlayers().add(fPlayer);

                fPlayer.setTempBanTime(resultSet.getInt("tempban_time"));
                fPlayer.setTempBanReason(resultSet.getString("tempban_reason"));

                if (fPlayer.isBanned()) FPlayerManager.getBannedPlayers().add(fPlayer);

                String chat = resultSet.getString("chat");
                chat = chat == null ? "local" : chat;
                fPlayer.setChat(chat);

                String mail = resultSet.getString("mails");
                if (mail == null) continue;
                String[] mails = mail.split(",");

                for (String uuid : mails) {
                    PreparedStatement ps2 = conn.prepareStatement("SELECT * FROM mails WHERE uuid = ?");
                    ps2.setString(1, uuid);

                    ResultSet resultMail = ps2.executeQuery();

                    fPlayer.addMail(UUID.fromString(uuid), new Mail(UUID.fromString(resultMail.getString("sender")),
                            UUID.fromString(resultMail.getString("receiver")),
                            resultMail.getString("message")));
                }
            }

        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, "Couldn't execute MySQL statement: ", ex);
        }

        Main.info("\uD83D\uDCCA Database loaded successfully");

        if (SQLite.isOldVersion) loadOldConfigs();
    }

    public void uploadDatabase(FPlayer fPlayer) {
        try (Connection conn = getSQLConnection();
             PreparedStatement ps1 = conn.prepareStatement("UPDATE players SET " +
                     "mute_time = ? ," +
                     "mute_reason = ? ," +
                     "tempban_time = ? ," +
                     "tempban_reason = ?," +
                     "colors = ? ," +
                     "ignore_list = ? ," +
                     "mails = ? ," +
                     "chat = ?" +
                     "WHERE uuid = ?")) {

            int muteTime = fPlayer.getMuteTime();
            if (muteTime > 0) ps1.setInt(1, muteTime + ObjectUtil.getCurrentTime());
            else ps1.setObject(1, null);

            String muteReason = fPlayer.getMuteReason();
            muteReason = muteTime > 0 ? muteReason : null;
            ps1.setString(2, muteReason);

            int tempBanTime = fPlayer.getTempBanTime();
            if (tempBanTime > 0) ps1.setInt(3, tempBanTime + ObjectUtil.getCurrentTime());
            else if (fPlayer.getRealBanTime() == -1) ps1.setInt(3, -1);
            else ps1.setObject(3, null);

            String tempBanReason = fPlayer.getBanReason();
            tempBanReason = fPlayer.isBanned() ? tempBanReason : null;
            ps1.setString(4, tempBanReason);

            String[] colors = fPlayer.getColors();
            ps1.setString(5, colors[0] + "," + colors[1]);

            StringBuilder ignoreListString = new StringBuilder();
            for (UUID ignoredPlayer : fPlayer.getIgnoreList()) ignoreListString.append(ignoredPlayer).append(",");
            ps1.setString(6, ignoreListString.length() == 0 ? null : ignoreListString.toString());

            if (!fPlayer.getMails().isEmpty() && !fPlayer.getMails().isEmpty()) {
                fPlayer.getMails().forEach((uuid, mail) -> {
                    if (mail.isRemoved()) {
                        try {
                            PreparedStatement ps2 = conn.prepareStatement("DELETE FROM mails WHERE uuid = ?");
                            ps2.setString(1, uuid.toString());
                            ps2.executeUpdate();
                        } catch (SQLException e) {
                            plugin.getLogger().log(Level.SEVERE, "Couldn't execute MySQL statement: ", e);
                        }
                        return;
                    }

                    try {
                        PreparedStatement ps2 = conn.prepareStatement("REPLACE INTO mails (uuid,sender,receiver,message) VALUES(?,?,?,?)");
                        ps2.setString(1, mail.getUUID().toString());
                        ps2.setString(2, mail.getSender().toString());
                        ps2.setString(3, mail.getReceiver().toString());
                        ps2.setString(4, mail.getMessage());
                        ps2.executeUpdate();
                    } catch (SQLException e) {
                        plugin.getLogger().log(Level.SEVERE, "Couldn't execute MySQL statement: ", e);
                    }
                });
            }

            String mails = fPlayer.getMails().entrySet().stream()
                    .filter(entry -> !entry.getValue().isRemoved())
                    .map(entry -> entry.getKey().toString())
                    .collect(Collectors.joining(","));

            mails = mails.isEmpty() ? null : mails;

            ps1.setString(7, mails);
            ps1.setString(8, fPlayer.getChat());
            ps1.setString(9, fPlayer.getUUID().toString());
            ps1.executeUpdate();

        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, "Couldn't execute MySQL statement: ", ex);
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

    public void delayedUpdateDatabase() {
        Main.info("\uD83D\uDCCA Loaded players to database");

        FPlayerManager.uploadPlayers();
    }
}
