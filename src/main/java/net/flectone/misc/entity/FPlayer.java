package net.flectone.misc.entity;

import net.flectone.Main;
import net.flectone.integrations.luckperms.FLuckPerms;
import net.flectone.integrations.supervanish.FSuperVanish;
import net.flectone.integrations.vault.FVault;
import net.flectone.managers.FPlayerManager;
import net.flectone.managers.HookManager;
import net.flectone.misc.entity.info.Mail;
import net.flectone.misc.entity.info.ChatInfo;
import net.flectone.misc.entity.info.ModInfo;
import net.flectone.utils.ObjectUtil;
import net.md_5.bungee.api.chat.BaseComponent;
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
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static net.flectone.managers.FileManager.config;
import static net.flectone.managers.FileManager.locale;

public class FPlayer {

    private final OfflinePlayer offlinePlayer;
    private final String name;
    private final UUID uuid;
    private final HashMap<UUID, Mail> mails = new HashMap<>();
    private final List<String> listChatBubbles = new ArrayList<>();
    private Player player;
    private Block block;
    private Team team;
    private boolean isAfk = false;
    private boolean spies = false;
    private boolean isStreaming = false;
    private boolean isStreamer = false;
    private String[] colors = new String[]{};
    private ArrayList<UUID> ignoreList = new ArrayList<>();
    private List<Inventory> inventoryList = new ArrayList<>();
    private ModInfo muteInfo = null;
    private ModInfo banInfo = null;
    private ChatInfo chatInfo = null;
    private int numberLastInventory;
    private int lastTimeMoved;
    private String streamPrefix = "";
    private String afkSuffix = "";
    private String vaultSuffix = "";
    private String vaultPrefix = "";
    private Player lastWriter;
    private String worldPrefix = "";
    private FDamager lastFDamager = new FDamager();

    public FPlayer(@NotNull OfflinePlayer offlinePlayer) {
        this.offlinePlayer = offlinePlayer;
        this.uuid = offlinePlayer.getUniqueId();

        String offlinePlayerName = offlinePlayer.getName();
        this.name = offlinePlayerName != null ? offlinePlayerName : "";
    }

    public FPlayer(@NotNull Player player) {
        this.player = player;
        this.offlinePlayer = player;
        this.name = player.getName();
        this.uuid = player.getUniqueId();
    }

    public void initialize(@NotNull Player player) {
        setPlayer(player);
        player.setScoreboard(FPlayerManager.getScoreBoard());
        setTeam(getPlayerTeam());
        setBlock(this.player.getLocation().getBlock());
        setVaultPrefixSuffix();
        setWorldPrefix(player.getWorld());
        setStreamer();
        updateName();
    }

    public void loadMuteInfo() {
        ModInfo modInfo = (ModInfo) Main.getDatabase().getPlayerInfo("mutes", "player", this.uuid.toString());
        if (modInfo != null) {
            this.muteInfo = modInfo;
        }
    }

    public void loadBanInfo() {
        ModInfo modInfo = (ModInfo) Main.getDatabase().getPlayerInfo("bans", "player", this.uuid.toString());
        if (modInfo != null) {
            this.banInfo = modInfo;
        }
    }

    public void synchronizeDatabase() {
        Main.getDatabase().insertPlayer(this.uuid);

        loadMuteInfo();
        loadBanInfo();

        Main.getDatabase().initFPlayer(this);

        if (mails.isEmpty() || player == null) return;

        mails.values().forEach(mail -> {
            String playerName = Bukkit.getOfflinePlayer(mail.getSender()).getName();
            if (playerName == null) return;

            String localeString = locale.getFormatString("command.mail.get", player)
                    .replace("<player>", playerName);

            String newLocaleString = localeString.replace("<message>", mail.getMessage());
            player.sendMessage(newLocaleString);

            Main.getDatabase().updatePlayerInfo("mails", mail);
        });

        mails.clear();

        Main.getDatabase().updateFPlayer(this, "mails");
    }

    public void setChatInfo(ChatInfo chatInfo) {
        this.chatInfo = chatInfo;
    }

    public ChatInfo getChatInfo() {
        return chatInfo;
    }

    public boolean isOnline() {
        if (player != null && FSuperVanish.isVanished(player)) return false;

        return this.offlinePlayer.isOnline();
    }

    public void setStreamer() {
        if (player == null) return;
        this.isStreamer = player.hasPermission("flectonechat.stream");
        if (isStreamer) setStreaming(isStreaming);
        else this.streamPrefix = "";
    }

    @Nullable
    public Player getPlayer() {
        return this.player;
    }

    public void setPlayer(@NotNull Player player) {
        this.player = player;
    }

    public boolean hasPermission(@NotNull String permission) {
        if (player == null) return false;
        return player.hasPermission(permission);
    }

    @NotNull
    public OfflinePlayer getOfflinePlayer() {
        return this.offlinePlayer;
    }

    @NotNull
    public UUID getUUID() {
        return this.uuid;
    }

    public ModInfo getMute() {
        return this.muteInfo;
    }

    public boolean isMoved(Block block) {
        return !block.equals(this.block);
    }

    public void setBlock(@Nullable Block block) {
        this.block = block;
        this.lastTimeMoved = ObjectUtil.getCurrentTime();
    }

    public int getLastTimeMoved() {
        return lastTimeMoved;
    }

    public boolean isMuted() {
        boolean isMuted = muteInfo != null && muteInfo.getDifferenceTime() > 0;

        if (!isMuted && muteInfo != null) {
            unmute();
        }

        return isMuted;
    }

    public boolean isBanned() {
        boolean isBanned = banInfo != null && (banInfo.getDifferenceTime() > 0 || banInfo.getTime() == -1);

        if (isBanned) {
            unban();
        }

        return isBanned;
    }

    public boolean isAfk() {
        return this.isAfk;
    }

    public void setAfk(boolean afk) {
        isAfk = afk;
    }

    @NotNull
    public String getAfkSuffix() {
        return isAfk ? locale.getString("command.afk.suffix") : "";
    }

    public void setAfkSuffix(String afkSuffix) {
        this.afkSuffix = afkSuffix;
    }

    @NotNull
    public String getStreamPrefix() {
        return streamPrefix;
    }

    public void mute(int time, @NotNull String reason, @Nullable String moderatorUUID) {
        int finalTime = time + ObjectUtil.getCurrentTime();

        this.muteInfo = new ModInfo(this.uuid.toString(), finalTime, reason, moderatorUUID);

        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () ->
                Main.getDatabase().updatePlayerInfo("mutes", muteInfo));
    }

    public void unmute() {
        this.muteInfo = null;

        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () ->
                Main.getDatabase().deleteRow("mutes", "player", this.uuid.toString()));
    }

    public void tempban(int time, @NotNull String reason, @Nullable String moderatorUUID) {
        int finalTime = time == -1 ? -1 : time + ObjectUtil.getCurrentTime();

        ModInfo modInfo = new ModInfo(this.uuid.toString(), finalTime, reason, moderatorUUID);
        this.banInfo = modInfo;

        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () ->
                Main.getDatabase().updatePlayerInfo("bans", modInfo));

        if (player == null || !offlinePlayer.isOnline()) return;

        String localStringMessage = time == -1 ? "command.ban.local-message" : "command.tempban.local-message";

        String localMessage = locale.getFormatString(localStringMessage, player)
                .replace("<time>", ObjectUtil.convertTimeToString(time))
                .replace("<reason>", reason);

        player.kickPlayer(localMessage);
    }

    public void unban() {
        this.banInfo = null;

        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () ->
                Main.getDatabase().deleteRow("bans", "player", this.uuid.toString()));
    }

    @NotNull
    public ArrayList<UUID> getIgnoreList() {
        return ignoreList;
    }

    public void setIgnoreList(@NotNull ArrayList<UUID> ignoreList) {
        this.ignoreList = ignoreList;
    }

    public boolean isIgnored(@Nullable Player player) {
        if (player == null || this.player == player) return false;

        return isIgnored(player.getUniqueId());
    }

    public boolean isIgnored(@Nullable OfflinePlayer offlinePlayer) {
        if (offlinePlayer == null || this.offlinePlayer == offlinePlayer) return false;

        return isIgnored(offlinePlayer.getUniqueId());
    }

    public boolean isIgnored(@NotNull UUID uuid) {
        return this.ignoreList.contains(uuid);
    }

    public void setColors(@NotNull String firstColor, @NotNull String secondColor) {
        this.colors = new String[]{firstColor, secondColor};
    }

    @NotNull
    public String[] getColors() {
        if (this.colors.length != 0) return this.colors;

        String firstColor = config.getString("color.first");
        String secondColor = config.getString("color.second");
        setColors(firstColor, secondColor);

        return this.colors;
    }

    public void setTeam(@NotNull Team team) {
        this.team = team;
    }

    @NotNull
    public Team getPlayerTeam() {

        int rank = 0;

        if (HookManager.enabledVault && HookManager.enabledLuckPerms) {
            rank = FLuckPerms.getPlayerGroupWeight(player);
        }

        String sortName = ObjectUtil.generateSortString(rank, player.getName());

        Team bukkitTeam = FPlayerManager.getScoreBoard().getTeam(sortName);
        Team team = bukkitTeam != null ? bukkitTeam : FPlayerManager.getScoreBoard().registerNewTeam(sortName);

        if (!team.hasEntry(this.name)) team.addEntry(this.name);

        team.setOption(Team.Option.NAME_TAG_VISIBILITY, config.getBoolean("player.name-visible")
                ? Team.OptionStatus.ALWAYS : Team.OptionStatus.NEVER);

        team.setColor(ChatColor.WHITE);

        return team;
    }

    public void setTeamColor(@NotNull String teamColor) {
        this.team.setColor(ChatColor.valueOf(teamColor));
    }

    public void removeTeam() {
        this.team.unregister();
    }

    @Nullable
    public Player getLastWriter() {
        return this.lastWriter;
    }

    public void setLastWriter(@NotNull Player lastWriter) {
        this.lastWriter = lastWriter;
    }

    @NotNull
    public List<Inventory> getInventoryList() {
        return inventoryList;
    }

    public void setInventoryList(@NotNull List<Inventory> inventoryList) {
        this.inventoryList = inventoryList;
    }

    public int getNumberLastInventory() {
        return numberLastInventory;
    }

    public void setNumberLastInventory(int numberLastInventory) {
        this.numberLastInventory = numberLastInventory;
    }

    public boolean isStreaming() {
        return this.isStreaming;
    }

    public void setStreaming(boolean streaming) {
        this.isStreaming = streaming;

        String streamFormatPrefix = getStreamFormatPrefix();
        this.streamPrefix = streamFormatPrefix.isEmpty() ? "" : locale.getFormatString(streamFormatPrefix, getPlayer());
    }

    @NotNull
    private String getStreamFormatPrefix() {
        return isStreaming
                ? "command.stream.online-prefix"
                : config.getBoolean("command.stream.offline-prefix.enable")
                    ? "command.stream.offline-prefix"
                    : "";
    }

    public void setVaultPrefixSuffix() {
        if (HookManager.enabledVault) {
            Chat provider = FVault.getProvider();

            this.vaultPrefix = provider.getPlayerPrefix(player);
            this.vaultSuffix = provider.getPlayerSuffix(player);
        }
    }

    @NotNull
    private String getName(@NotNull String formatName) {
        setVaultPrefixSuffix();

        String name = config.getString("player." + formatName)
                .replace("<vault_prefix>", this.vaultPrefix)
                .replace("<world_prefix>", this.worldPrefix)
                .replace("<stream_prefix>", this.streamPrefix)
                .replace("<player>", this.name)
                .replace("<vault_suffix>", this.vaultSuffix)
                .replace("<afk_suffix>", this.afkSuffix);

        return ObjectUtil.formatString(name, this.getPlayer());

    }

    @NotNull
    public String getDisplayName() {
        return getName("display-name");
    }

    @NotNull
    public String getTabName() {
        return getName("tab-name");
    }

    @NotNull
    public String getRealName() {
        return this.name;
    }

    public void updateName() {
        this.player.setPlayerListName(getTabName());

        String prefix = "";
        String suffix = "";
        if (config.getBoolean("player.name-tag.enable")) {
            String[] strings = getDisplayName().split(this.name);
            prefix = strings.length > 0 ? strings[0] : "";
            suffix = strings.length > 1 ? strings[1] : "";
        }

        this.team.setPrefix(prefix);
        this.team.setSuffix(suffix);
    }

    @NotNull
    public String getWorldPrefix() {
        return worldPrefix;
    }

    public void setWorldPrefix(World world) {
        if (Main.getInstance().getConfig().getBoolean("player.world.prefix.enable")) {

            String worldType = config.getString("player.world.mode").equals("type")
                    ? world.getEnvironment().toString().toLowerCase()
                    : world.getName().toLowerCase();

            this.worldPrefix = locale.getString("player.world.prefix." + worldType);
            if (this.worldPrefix.isEmpty()) {
                Main.warning("The prefix for " + worldType + " could not be determined");
                this.worldPrefix = locale.getString("player.world.prefix.normal");
            }

            this.worldPrefix = ObjectUtil.formatString(this.worldPrefix, player);
        }

        updateName();
    }

    @NotNull
    public HashMap<UUID, Mail> getMails() {
        return mails;
    }

    public void addMail(@NotNull UUID uuid, @NotNull Mail mail) {
        mails.put(uuid, mail);
    }

    public void removeMail(@NotNull UUID uuid) {
        mails.remove(uuid);
    }

    public void addChatBubble(@NotNull String message) {
        listChatBubbles.add(message);
    }

    public void removeChatBubble() {
        if (!listChatBubbles.isEmpty()) listChatBubbles.remove(0);
    }

    @NotNull
    public List<Entity> getChatBubbleEntities() {
        return player.getPassengers().parallelStream()
                .filter(entity -> entity instanceof AreaEffectCloud)
                .collect(Collectors.toList());
    }

    public void clearChatBubbles() {
        getChatBubbleEntities().parallelStream().forEach(Entity::remove);
        listChatBubbles.clear();
    }

    @NotNull
    public List<String> getListChatBubbles() {
        return listChatBubbles;
    }

    @Nullable
    public FDamager getLastFDamager() {
        return lastFDamager;
    }

    public void setLastDamager(@Nullable Entity lastDamager) {
        this.lastFDamager.replaceDamager(lastDamager);
    }

    public void resetLastDamager() {
        this.lastFDamager = new FDamager();
    }

    public boolean isDeathByObject() {
        return ObjectUtil.getCurrentTime() - this.lastFDamager.getTime() < 5;
    }

    public void spigotMessage(BaseComponent baseComponent) {
        if(this.player == null) return;

        this.player.spigot().sendMessage(baseComponent);
    }

    public void spigotMessage(BaseComponent[] baseComponents) {
        if(this.player == null) return;

        this.player.spigot().sendMessage(baseComponents);
    }
}
