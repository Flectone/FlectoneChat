package net.flectone.sqlite;

import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

import net.flectone.Main;
import net.flectone.commands.CommandChatcolor;
import net.flectone.custom.FPlayer;
import net.flectone.custom.Mail;
import net.flectone.managers.FPlayerManager;
import net.flectone.utils.ObjectUtil;
import org.bukkit.Bukkit;

public abstract class Database {
    Main plugin;
    Connection connection;

    public Database(Main instance){
        plugin = instance;
    }

    public abstract Connection getSQLConnection();

    public abstract void load();

    public void initialize(){
        connection = getSQLConnection();
        try{
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM players WHERE uuid = ?");
            ResultSet rs = ps.executeQuery();
            close(ps,rs);

            Arrays.stream(Bukkit.getOfflinePlayers())
                    .forEach(offlinePlayer -> setPlayer(offlinePlayer.getUniqueId().toString()));

        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, "Unable to retreive connection", ex);
        }
    }

    public void setPlayer(String uuid){
        try (Connection conn = getSQLConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT OR IGNORE INTO players (uuid) VALUES(?)")) {

            ps.setString(1, uuid);

            ps.executeUpdate();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        }
    }


    public void loadDatabase(){
        try (Connection conn = getSQLConnection();
             PreparedStatement ps1 = conn.prepareStatement("SELECT * FROM players")){

            ResultSet resultSet = ps1.executeQuery();

            while(resultSet.next()){

                FPlayer fPlayer = FPlayerManager.getPlayer(resultSet.getString("uuid"));

                String color = resultSet.getString("colors");

                String[] colors = color == null ? CommandChatcolor.getDefaultColors() : color.split(",");
                fPlayer.setColors(colors[0], colors[1]);

                String ignoreList = resultSet.getString("ignore_list");

                ArrayList<String> arrayList = ignoreList == null ? new ArrayList<>() : new ArrayList<>(Arrays.asList(ignoreList.split(",")));
                fPlayer.setIgnoreList(arrayList);
                fPlayer.setMuteTime(resultSet.getInt("mute_time"));
                fPlayer.setMuteReason(resultSet.getString("mute_reason"));

                String mail = resultSet.getString("mails");
                if(mail == null) continue;
                String[] mails = mail.split(",");

                for(String uuid : mails){
                    PreparedStatement ps2 = conn.prepareStatement("SELECT * FROM mails WHERE uuid = ?");
                    ps2.setString(1, uuid);

                    ResultSet resultMail = ps2.executeQuery();

                    fPlayer.addMail(uuid, new Mail(resultMail.getString("sender"),
                            resultMail.getString("receiver"),
                            resultMail.getString("message")));
                }
            }

        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        }
    }

    public void uploadDatabase(FPlayer fPlayer){
        try (Connection conn = getSQLConnection();
             PreparedStatement ps1 = conn.prepareStatement("UPDATE players SET mute_time = ? , mute_reason = ? , colors = ? , ignore_list = ? , mails = ? WHERE uuid = ?")){

            int muteTime = fPlayer.getMuteTime();
            if(muteTime > 0) ps1.setInt(1, muteTime + ObjectUtil.getCurrentTime());
            else ps1.setObject(1, null);

            String muteReason = fPlayer.getMuteReason();
            muteReason = muteTime > 0 ? muteReason : null;
            ps1.setString(2, muteReason);

            String[] colors = fPlayer.getColors();
            ps1.setString(3, colors[0] + "," + colors[1]);

            String ignoreListString = "";
            for(String ignoredPlayer : fPlayer.getIgnoreList()) ignoreListString += ignoredPlayer + ",";
            ignoreListString = ignoreListString.length() == 0 ? null : ignoreListString;
            ps1.setString(4, ignoreListString);

            if(!fPlayer.getMails().isEmpty() && fPlayer.getMails().size() != 0){
                fPlayer.getMails().forEach((uuid, mail) -> {
                    if(mail.isRemoved()){
                        try {
                            PreparedStatement ps2 = conn.prepareStatement("DELETE FROM mails WHERE uuid = ?");
                            ps2.setString(1, uuid);
                            ps2.executeUpdate();
                        } catch (SQLException e){
                            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), e);
                        }
                        return;
                    }

                    try {
                        PreparedStatement ps2 = conn.prepareStatement("REPLACE INTO mails (uuid,sender,receiver,message) VALUES(?,?,?,?)");
                        ps2.setString(1, mail.getUUID());
                        ps2.setString(2, mail.getSender());
                        ps2.setString(3, mail.getReceiver());
                        ps2.setString(4, mail.getMessage());
                        ps2.executeUpdate();
                    } catch (SQLException e) {
                        plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), e);
                    }
                });
            }

            String mails = fPlayer.getMails().entrySet().stream()
                    .filter(entry -> !entry.getValue().isRemoved())
                    .map(Map.Entry::getKey)
                    .collect(Collectors.joining(","));

            mails = mails.length() == 0 ? null : mails;

            ps1.setString(5, mails);
            ps1.setString(6, fPlayer.getUUID());
            ps1.executeUpdate();



        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        }
    }

    public void close(PreparedStatement ps,ResultSet rs){
        try {
            if (ps != null)
                ps.close();
            if (rs != null)
                rs.close();
        } catch (SQLException ex) {
            Error.close(plugin, ex);
        }
    }
}
