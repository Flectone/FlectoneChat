package net.flectone.custom;

import net.flectone.Main;
import net.flectone.managers.PlayerManager;
import net.flectone.utils.ObjectUtil;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class FPlayer {

    private final UUID uuid;

    private final Player player;

    private String name;

    private List<String> ignoreList;

    private List<Inventory> inventoryList;

    private int numberLastInventory = 0;

    private List<String> colors = new ArrayList<>();

    private boolean streamer = false;

    private boolean isAfk = false;

    private int lastTimeMoved;

    private Block lastBlock;

    private Player lastWritePlayer;

    private Team team;

    public FPlayer(Player player) {
        this.player = player;
        this.name = player.getName();
        this.uuid = player.getUniqueId();
        this.ignoreList = Main.ignores.getStringList(uuid.toString());
        this.team = getPlayerTeam();

        PlayerManager.addPlayer(this);

        setName(player.getWorld());

        setLastBlock(player.getLocation().getBlock());

        setPlayerListHeaderFooter();

        isMuted();
    }

    private Team getPlayerTeam(){
        Team bukkitTeam = Bukkit.getScoreboardManager().getMainScoreboard().getTeam(player.getName());
        Team team = bukkitTeam != null ? bukkitTeam : Bukkit.getScoreboardManager().getMainScoreboard().registerNewTeam(player.getName());
        if(!team.hasEntry(player.getName())) team.addEntry(player.getName());

        team.setColor(ChatColor.WHITE);

        return team;
    }

    public void setTeamColor(String teamColor){
        this.team.setColor(ChatColor.valueOf(teamColor));
    }

    public Player getPlayer() {
        return player;
    }

    public UUID getUUID() {
        return uuid;
    }

    public void setPlayerListHeaderFooter() {
        if (!player.isOnline()) return;

        player.setPlayerListName(getName());

        if (Main.config.getBoolean("tab.header.enable")) {
            player.setPlayerListHeader(Main.locale.getFormatString("tab.header.message", player));
        }
        if (Main.config.getBoolean("tab.footer.enable")) {
            player.setPlayerListFooter(Main.locale.getFormatString("tab.footer.message", player));
        }
    }

    public Player getLastWritePlayer() {
        return lastWritePlayer;
    }

    public void setLastWritePlayer(Player lastWritePlayer) {
        this.lastWritePlayer = lastWritePlayer;
    }

    public void setName(World world) {
        if (Main.getInstance().getConfig().getBoolean("color.worlds.enable")) {
            addPrefixToName(Main.config.getFormatString("color." + world.getEnvironment().toString().toLowerCase(), player));
        }

        this.player.setPlayerListName(getName());
        this.team.setPrefix(prefix);
        this.team.setSuffix(suffix);
    }

    private String prefix = "";

    private String suffix = "";

    public void addPrefixToName(String string) {
        this.prefix += string;

        this.player.setPlayerListName(getName());
        this.team.setPrefix(prefix);
        this.team.setSuffix(suffix);
    }

    public void removeFromName(String string) {
        this.prefix = this.prefix.replaceFirst(string, "");
        this.suffix = this.suffix.replaceFirst(string, "");
        this.player.setPlayerListName(getName());
        this.team.setPrefix(prefix);
        this.team.setSuffix(suffix);
    }

    public void addSuffixToName(String string) {
        this.suffix += string;
        this.player.setPlayerListName(getName());
        this.team.setPrefix(prefix);
        this.team.setSuffix(suffix);
    }

    public String getName() {
        if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
            Chat provider = Objects.requireNonNull(Main.getInstance().getServer().getServicesManager().getRegistration(Chat.class)).getProvider();
            String vaultPrefix = provider.getPlayerPrefix(player);
            String vaultSuffix = provider.getPlayerSuffix(player);

            String finalName = prefix + Main.config.getString("vault.prefix") + this.name + Main.config.getString("vault.suffix") + suffix;

            finalName = finalName
                    .replace("<vault_prefix>", vaultPrefix)
                    .replace("<vault_suffix>", vaultSuffix);
            return ObjectUtil.translateHexToColor(finalName);
        }
        return prefix + name + suffix;
    }

    public List<String> getIgnoreList() {
        return ignoreList;
    }

    public void saveIgnoreList(List<String> ignoreList) {
        this.ignoreList = ignoreList;
        Main.ignores.updateFile(uuid.toString(), ignoreList);
    }

    public List<Inventory> getInventoryList() {
        return inventoryList;
    }

    public void setInventoryList(List<Inventory> inventoryList) {
        this.inventoryList = inventoryList;
    }

    public int getNumberLastInventory() {
        return numberLastInventory;
    }

    public void setNumberLastInventory(int numberLastInventory) {
        this.numberLastInventory = numberLastInventory;
    }

    public void setColors(String firstColor, String secondColor) {
        if (colors.isEmpty()) {
            colors.add("0");
            colors.add("1");
        }
        colors.set(0, firstColor);
        colors.set(1, secondColor);

        Main.themes.updateFile(uuid.toString(), colors);
    }

    public List<String> getColors() {
        colors = Main.themes.getStringList(uuid.toString());
        if (colors.isEmpty()) {
            List<String> list = new ArrayList<>();
            list.add(Main.getInstance().getConfig().getString("color.first"));
            list.add(Main.getInstance().getConfig().getString("color.second"));
            return list;
        }
        return colors;
    }

    public boolean isStreamer() {
        return streamer;
    }

    public void setStreamer(boolean streamer) {
        this.streamer = streamer;
    }

    public boolean checkIgnoreList(Player secondPlayer) {
        if (secondPlayer == null) {
            return false;
        }
        return ignoreList.contains(secondPlayer.getUniqueId().toString());
    }

    public boolean isAfk() {
        return isAfk;
    }

    public void setAfk(boolean afk) {
        isAfk = afk;
    }

    public void setLastBlock(Block lastBlock) {
        this.lastTimeMoved = ObjectUtil.getCurrentTime();
        this.lastBlock = lastBlock;
    }

    public int getLastTimeMoved() {
        return lastTimeMoved;
    }

    public boolean isMoved(Block block) {
        return !this.lastBlock.equals(block);
    }

    public int getRealTimeMuted() {
        List<String> list = Main.mutes.getStringList(uuid.toString());
        if (list.isEmpty()) return 0;
        return Integer.parseInt(list.get(1));
    }

    public int getTimeMuted() {
        return getRealTimeMuted() - ObjectUtil.getCurrentTime();
    }

    public String getReasonMute() {
        List<String> list = Main.mutes.getStringList(uuid.toString());
        if (list.isEmpty()) return null;
        return list.get(0);
    }

    public boolean isMuted() {
        boolean isMuted = getTimeMuted() > 0;
        if (!isMuted && !Main.mutes.getStringList(uuid.toString()).isEmpty()) {
            Main.mutes.updateFile(uuid.toString(), null);
        }
        return isMuted;
    }
}
