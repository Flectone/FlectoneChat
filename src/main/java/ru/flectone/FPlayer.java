package ru.flectone;

import net.milkbowl.vault.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;
import ru.flectone.utils.PlayerUtils;
import ru.flectone.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FPlayer {

    public FPlayer(Player player){
        this.player = player;
        this.name = player.getName();
        this.uuid = player.getUniqueId();
        this.ignoreList = Main.ignores.getStringList(uuid.toString());

        PlayerUtils.addPlayer(this);

        setName(player.getWorld());


        new BukkitRunnable() {
            @Override
            public void run() {
                setPlayerListHeaderFooter();
            }
        }.runTaskTimer(Main.getInstance(), 0L, 20L * Main.config.getInt("tab.update.rate"));
    }

    private void setPlayerListHeaderFooter(){
        if(Main.config.getBoolean("tab.header.enable")){
            player.setPlayerListHeader(Main.locale.getFormatString("tab.header.message", player));
        }
        if(Main.config.getBoolean("tab.footer.enable")){
            player.setPlayerListFooter(Main.locale.getFormatString("tab.footer.message", player));
        }
    }

    private Player lastWritePlayer;

    public void setLastWritePlayer(Player lastWritePlayer) {
        this.lastWritePlayer = lastWritePlayer;
    }

    public Player getLastWritePlayer() {
        return lastWritePlayer;
    }

    private final UUID uuid;

    public UUID getUUID() {
        return uuid;
    }

    private final Player player;

    public Player getPlayer() {
        return player;
    }

    private String name;

    public void setName(World world) {
        if(Main.getInstance().getConfig().getBoolean("color.worlds.enable")){
            this.name = Main.config.getFormatString("color." + world.getEnvironment().toString().toLowerCase(), player) + player.getName();
        }

        this.player.setPlayerListName(getName());
    }

    public void addToName(String string) {
        this.name = string + this.name;
        this.player.setPlayerListName(getName());
    }

    public void removeFromName(String string){
        this.name = this.name.replaceFirst(string, "");
        this.player.setPlayerListName(getName());
    }

    public String getName() {

        if(Bukkit.getPluginManager().getPlugin("Vault") != null){
            String vaultPrefix = Main.getInstance().getServer().getServicesManager().getRegistration(Chat.class).getProvider().getPlayerPrefix(player);
            String vaultSuffix = Main.getInstance().getServer().getServicesManager().getRegistration(Chat.class).getProvider().getPlayerSuffix(player);

            String finalName = Main.config.getString("vault.prefix") + this.name + Main.config.getString("vault.suffix");
            finalName = finalName.replace("<vault_prefix>", vaultPrefix).replace("<vault_suffix>", vaultSuffix);

            return Utils.translateColor(finalName);
        }
        return name;
    }

    private List<String> ignoreList;

    public List<String> getIgnoreList() {
        return ignoreList;
    }

    public void saveIgnoreList(List<String> ignoreList) {
        this.ignoreList = ignoreList;

        Main.ignores.set(uuid.toString(), ignoreList);
        Main.ignores.saveFile();
    }

    private List<Inventory> inventoryList;

    public List<Inventory> getInventoryList() {
        return inventoryList;
    }

    public void setInventoryList(List<Inventory> inventoryList) {
        this.inventoryList = inventoryList;
    }

    private int numberLastInventory = 0;

    public void setNumberLastInventory(int numberLastInventory) {
        this.numberLastInventory = numberLastInventory;
    }

    public int getNumberLastInventory() {
        return numberLastInventory;
    }

    private List<String> colors = new ArrayList<>();

    public void setColors(String firstColor, String secondColor) {
        //200IQ
        if(colors.isEmpty()){
            colors.add("0");
            colors.add("1");
        }
        colors.set(0, firstColor);
        colors.set(1, secondColor);

        Main.themes.set(uuid.toString(), colors);
        Main.themes.saveFile();
    }

    public List<String> getColors() {
        colors = Main.themes.getStringList(uuid.toString());

        if(colors.isEmpty()){
            List<String> list = new ArrayList<>();
            list.add(Main.getInstance().getConfig().getString("color.first"));
            list.add(Main.getInstance().getConfig().getString("color.second"));
            return list;
        }

        return colors;
    }

    private boolean streamer = false;

    public boolean isStreamer() {
        return streamer;
    }

    public void setStreamer(boolean streamer) {
        this.streamer = streamer;
    }
}
