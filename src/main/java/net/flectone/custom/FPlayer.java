package net.flectone.custom;

import net.flectone.Main;
import net.flectone.managers.FPlayerManager;
import net.flectone.utils.ObjectUtil;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class FPlayer {

    private OfflinePlayer offlinePlayer;

    private Player player;

    private Block block;

    private final String name;

    private final String uuid;

    private Team team;

    private boolean isAfk = false;

    private boolean isStreamer = false;

    private boolean isMuted = false;

    private String[] colors = new String[]{};

    private ArrayList<String> ignoreList = new ArrayList<>();

    private List<Inventory> inventoryList = new ArrayList<>();

    private String muteReason;

    private int muteTime;

    private String tempBanReason;

    private int tempBanTime;

    private int numberLastInventory;

    private int lastTimeMoved;

    private String streamPrefix = "";

    private String afkSuffix = "";

    private String vaultSuffix = "";

    private String vaultPrefix = "";

    private String chat = "local";

    public FPlayer(OfflinePlayer offlinePlayer){
        this.offlinePlayer = offlinePlayer;
        this.name = offlinePlayer.getName();
        this.uuid = offlinePlayer.getUniqueId().toString();
        this.team = getPlayerTeam();
    }

    public void initialize(Player player){
        setPlayer(player);
        setBlock(this.player.getLocation().getBlock());
        getVaultPrefixSuffix();
        setWorldPrefix(player.getWorld());
        setDisplayName();
        setUpdated(true);
    }

    public FPlayer(Player player){
        this.player = player;
        this.offlinePlayer = player;
        this.name = player.getName();
        this.uuid = player.getUniqueId().toString();
        this.team = getPlayerTeam();
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public boolean isOnline(){
        return this.offlinePlayer.isOnline();
    }

    public Player getPlayer() {
        return this.player;
    }

    public OfflinePlayer getOfflinePlayer() {
        return this.offlinePlayer;
    }

    public String getUUID() {
        return this.uuid;
    }

    public boolean isMoved(Block block){
        return !block.equals(this.block);
    }

    public void setBlock(Block block) {
        this.block = block;
        this.lastTimeMoved = ObjectUtil.getCurrentTime();
    }

    public int getLastTimeMoved() {
        return lastTimeMoved;
    }

    public boolean isMuted() {
        boolean isMuted = getMuteTime() > 0;

        if(!isMuted) {
            setMuteTime(0);
            setMuteReason("");
        }

        return isMuted;
    }

    public boolean isBanned() {
        boolean isBanned = getTempBanTime() > 0;

        if (!isBanned && !isPermanentlyBanned()) {
            unban();
            return false;
        }

        return isBanned;
    }

    public boolean isPermanentlyBanned(){
        return getRealBanTime() == -1;
    }

    public boolean isAfk() {
        return this.isAfk;
    }

    public void setAfk(boolean afk) {
        isAfk = afk;
    }

    public String getAfkSuffix() {
        return isAfk ? Main.locale.getString("command.afk.suffix") : "";
    }

    public String getStreamPrefix() {
        return isStreamer ? Main.locale.getString("command.stream.prefix") : "";
    }

    public void setMuteReason(String muteReason) {
        this.muteReason = muteReason;
    }

    public String getMuteReason() {
        return this.muteReason;
    }

    public void setMuteTime(int muteTime) {
        this.muteTime = muteTime;
    }

    public int getMuteTime(){
        return this.muteTime - ObjectUtil.getCurrentTime();
    }

    public int getTempBanTime() {
        return this.tempBanTime - ObjectUtil.getCurrentTime();
    }

    public int getRealBanTime(){
        return this.tempBanTime;
    }

    public void setTempBanTime(int tempBanTime) {
        this.tempBanTime = tempBanTime;
    }

    public String getTempBanReason() {
        return tempBanReason;
    }

    public void setTempBanReason(String tempBanReason) {
        this.tempBanReason = tempBanReason;
    }

    public void tempban(int time, String reason) {
        setTempBanTime(time == -1 ? -1 : time + ObjectUtil.getCurrentTime());
        setTempBanReason(reason);
        setUpdated(true);
        FPlayerManager.getBannedPlayers().add(this);

        if(!(player != null && player.isOnline())) return;

        String localStringMessage = time == -1 ? "command.ban.local-message" : "command.tempban.local-message";

        String localMessage = Main.locale.getFormatString(localStringMessage, player)
                .replace("<time>", ObjectUtil.convertTimeToString(time))
                .replace("<reason>", reason);

        player.kickPlayer(localMessage);
    }

    public void unban() {
        setTempBanTime(0);
        setTempBanReason("");
        setUpdated(true);
        FPlayerManager.getBannedPlayers().remove(this);
    }

    public void setIgnoreList(ArrayList<String> ignoreList) {
        this.ignoreList = ignoreList;
    }

    public ArrayList<String> getIgnoreList() {
        return ignoreList;
    }

    public boolean isIgnored(Player player){
        if(player == null || this.player == player) return false;

        return isIgnored(player.getUniqueId().toString());
    }

    public boolean isIgnored(OfflinePlayer offlinePlayer){
        if(offlinePlayer == null || this.offlinePlayer == offlinePlayer) return false;

        return isIgnored(offlinePlayer.getUniqueId().toString());
    }

    public boolean isIgnored(String uuid){
        return this.ignoreList.contains(uuid);
    }

    public void setColors(String firstColor, String secondColor) {
        this.colors = new String[]{firstColor, secondColor};
    }

    public String[] getColors() {
        if(this.colors.length != 0) return this.colors;

        String firstColor = Main.getInstance().getConfig().getString("color.first");
        String secondColor = Main.getInstance().getConfig().getString("color.second");
        setColors(firstColor, secondColor);

        return this.colors;
    }

    public Team getPlayerTeam(){
        Team bukkitTeam = Bukkit.getScoreboardManager().getMainScoreboard().getTeam(this.name);
        Team team = bukkitTeam != null ? bukkitTeam : Bukkit.getScoreboardManager().getMainScoreboard().registerNewTeam(this.name);

        boolean colorWorldsEnabled = Main.config.getBoolean("player.team.enable");

        if(!team.hasEntry(this.name) && colorWorldsEnabled) team.addEntry(this.name);
        if(team.hasEntry(this.name) && !colorWorldsEnabled) team.removeEntry(this.name);

        team.setNameTagVisibility(Main.config.getBoolean("player.team.name-visible")
                ? NameTagVisibility.ALWAYS : NameTagVisibility.NEVER);

        team.setColor(ChatColor.WHITE);

        return team;
    }

    public void setTeamColor(String teamColor){
        this.team.setColor(ChatColor.valueOf(teamColor));
    }

    private Player lastWriter;

    public void setLastWriter(Player lastWriter) {
        this.lastWriter = lastWriter;
    }

    public Player getLastWriter() {
        return this.lastWriter;
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

    public boolean isStreamer() {
        return this.isStreamer;
    }

    public void setStreamer(boolean streamer) {
        this.isStreamer = streamer;
    }

    public void setStreamPrefix(String streamPrefix) {
        this.streamPrefix = streamPrefix;
    }

    public void setAfkSuffix(String afkSuffix) {
        this.afkSuffix = afkSuffix;
    }

    public void getVaultPrefixSuffix(){
        if (Main.isHaveVault) {
            Chat provider = Objects.requireNonNull(Main.getInstance().getServer().getServicesManager().getRegistration(Chat.class)).getProvider();

            this.vaultPrefix = provider.getPlayerPrefix(player);
            this.vaultSuffix = provider.getPlayerSuffix(player);
        }
    }

    public String getName() {
        getVaultPrefixSuffix();

        String name = Main.config.getString("player.name")
                .replace("<vault_prefix>", this.vaultPrefix)
                .replace("<world_prefix>", this.worldPrefix)
                .replace("<stream_prefix>", this.streamPrefix)
                .replace("<player>", this.name)
                .replace("<vault_suffix>", this.vaultSuffix)
                .replace("<afk_suffix>", this.afkSuffix);

        return ObjectUtil.translateHexToColor(name);
    }

    public String getRealName(){
        return this.name;
    }

    public void setDisplayName(){
        String name = getName();
        String[] strings = name.split(this.name);
        String prefix = strings.length > 0 ? strings[0] : "";
        String suffix = strings.length > 1 ? strings[1] : "";

        this.player.setPlayerListName(name);
        this.team.setPrefix(prefix);
        this.team.setSuffix(suffix);
    }

    public void setPlayerListHeaderFooter() {
        if (!player.isOnline()) return;

        setDisplayName();

        if (Main.config.getBoolean("tab.header-message.enable")) {
            player.setPlayerListHeader(Main.locale.getFormatString("tab.header.message", player));
        }
        if (Main.config.getBoolean("tab.footer-message.enable")) {
            player.setPlayerListFooter(Main.locale.getFormatString("tab.footer.message", player));
        }
    }

    private String worldPrefix = "";

    public void setWorldPrefix(World world) {
        if (Main.getInstance().getConfig().getBoolean("player.world.prefix.enable")) {

            String worldType = Main.config.getString("player.world.mode").equals("type")
                    ? world.getEnvironment().toString().toLowerCase()
                    : world.getName().toLowerCase();

            this.worldPrefix = Main.locale.getString("player.world.prefix." + worldType);
            if(this.worldPrefix == null){
                Main.warning("The prefix for " + worldType + " could not be determined");
                this.worldPrefix = Main.locale.getString("player.world.prefix.normal");
            }

            this.worldPrefix = ObjectUtil.formatString(this.worldPrefix, player);
        }

        setDisplayName();
    }

    public String getWorldPrefix() {
        return worldPrefix;
    }

    private final HashMap<String, Mail> mails = new HashMap<>();

    public HashMap<String, Mail> getMails() {
        return mails;
    }

    public void addMail(String uuid, Mail mail){
        mails.put(uuid, mail);
    }

    public Mail getMail(String uuid){
        return mails.get(uuid);
    }

    public void removeMail(String uuid){
        mails.get(uuid).setRemoved(true);
    }

    private boolean isUpdated;

    public void setUpdated(boolean updated) {
        isUpdated = updated;
    }

    public boolean isUpdated() {
        return isUpdated;
    }

    public void setChat(String chat) {
        this.chat = chat;
    }

    public String getChat() {
        return chat;
    }

    private final List<String> listChatBubbles = new ArrayList<>();

    public void addChatBubble(String message){
        listChatBubbles.add(message);
    }

    public void removeChatBubble(){
        if (!listChatBubbles.isEmpty()) listChatBubbles.remove(0);
    }

    public List<Entity> getChatBubbleEntities() {
        return player.getPassengers().parallelStream()
                .filter(entity -> entity instanceof AreaEffectCloud)
                .collect(Collectors.toList());
    }

    public void clearChatBubbles() {
        getChatBubbleEntities().parallelStream().forEach(Entity::remove);
        listChatBubbles.clear();
    }

    public List<String> getListChatBubbles() {
        return listChatBubbles;
    }
}
