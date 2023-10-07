package net.flectone.misc.entity;

import net.flectone.Main;
import net.flectone.integrations.luckperms.FLuckPerms;
import net.flectone.integrations.supervanish.FSuperVanish;
import net.flectone.integrations.vault.FVault;
import net.flectone.managers.FPlayerManager;
import net.flectone.managers.HookManager;
import net.flectone.misc.entity.player.*;
import net.flectone.misc.files.FYamlConfiguration;
import net.flectone.utils.ObjectUtil;
import net.md_5.bungee.api.chat.BaseComponent;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
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
    private final HashMap<UUID, PlayerMail> mails = new HashMap<>();
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
    private PlayerMod muteInfo = null;
    private PlayerMod banInfo = null;
    private List<PlayerWarn> warnList = null;
    private PlayerChat playerChat = null;
    private int lastTimeMoved;
    private String streamPrefix = "";
    private String afkSuffix = "";
    private String vaultSuffix = "";
    private String vaultPrefix = "";
    private Player lastWriter;
    private String worldPrefix = "";
    private PlayerDamager lastPlayerDamager = new PlayerDamager();

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

    public void synchronizeDatabase() {
        Main.getDatabase().insertPlayer(this.uuid);

        this.muteInfo = Main.getDatabase().getPlayerInfo("mutes", "player", this.uuid.toString());
        // check and unmute if is over
        isMuted();

        this.banInfo = Main.getDatabase().getPlayerInfo("bans", "player", this.uuid.toString());
        // check and unban if is over
        isBanned();

        this.warnList = new ArrayList<>();

        Main.getDatabase().loadPlayersTable(this);

        if (mails.isEmpty() || player == null) return;

        mails.values().forEach(playerMail -> {
            String playerName = Bukkit.getOfflinePlayer(playerMail.getSender()).getName();
            if (playerName == null) return;

            String localeString = locale.getFormatString("command.mail.get", player)
                    .replace("<player>", playerName);

            String newLocaleString = localeString.replace("<message>", playerMail.getMessage());
            player.sendMessage(newLocaleString);

            Main.getDatabase().updatePlayerInfo("mails", playerMail);
        });

        mails.clear();

        Main.getDatabase().updateFPlayer(this, "mails");
    }

    public void setChatInfo(PlayerChat playerChat) {
        this.playerChat = playerChat;
    }

    public PlayerChat getChatInfo() {
        return playerChat;
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

    public PlayerMod getMute() {
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
        boolean isMuted = muteInfo != null && !muteInfo.isExpired();

        if (!isMuted) {
            unmute();
        }

        return isMuted;
    }

    public boolean isBanned() {
        boolean isBanned = banInfo != null && !banInfo.isExpired();

        if (!isBanned) {
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

    public void warn(int time, @NotNull String reason, @Nullable String moderatorUUID) {
        int finalTime = time + ObjectUtil.getCurrentTime();

        PlayerWarn playerWarn = new PlayerWarn(this.uuid.toString(), finalTime, reason, moderatorUUID);
        warnList.add(playerWarn);

        int countWarns = getRealWarnsCount();
        String warnAction = config.getString("command.warn.action." + countWarns);
        if (!warnAction.isEmpty()) {
            Bukkit.getScheduler().runTask(Main.getInstance(), () ->
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), warnAction.replace("<player>", this.name)));
        }

        Main.getDataThreadPool().execute(() ->
                Main.getDatabase().saveWarns(this));
    }

    public int getRealWarnsCount() {
        return (int) warnList.stream().filter(warn -> warn.getDifferenceTime() > 0).count();
    }

    public void addWarn(PlayerWarn playerWarn) {
        warnList.add(playerWarn);
    }

    public List<PlayerWarn> getWarnList() {
        if (warnList == null) return null;
        return warnList.stream().filter(playerWarn -> !playerWarn.isExpired()).toList();
    }

    public void unwarn(int index) {
        PlayerWarn playerWarn = warnList.get(index);
        warnList.remove(index);

        Main.getDataThreadPool().execute(() -> {
            Main.getDatabase().updatePlayerInfo("warns", playerWarn);
            Main.getDatabase().saveWarns(this);
        });
    }

    public void mute(int time, @NotNull String reason, @Nullable String moderatorUUID) {
        int finalTime = time + ObjectUtil.getCurrentTime();

        this.muteInfo = new PlayerMod(this.uuid.toString(), finalTime, reason, moderatorUUID);

        Main.getDataThreadPool().execute(() ->
                Main.getDatabase().updatePlayerInfo("mutes", muteInfo));
    }

    public void unmute() {
        this.muteInfo = null;

        Main.getDataThreadPool().execute(() ->
                Main.getDatabase().deleteRow("mutes", "player", this.uuid.toString()));
    }

    public void tempban(int time, @NotNull String reason, @Nullable String moderatorUUID) {
        int finalTime = time == -1 ? -1 : time + ObjectUtil.getCurrentTime();

        PlayerMod playerMod = new PlayerMod(this.uuid.toString(), finalTime, reason, moderatorUUID);
        this.banInfo = playerMod;

        Main.getDataThreadPool().execute(() ->
                Main.getDatabase().updatePlayerInfo("bans", playerMod));

        FPlayerManager.getBannedPlayers().add(getRealName());

        if (player == null || !offlinePlayer.isOnline()) return;

        String localStringMessage = time == -1 ? "command.ban.local-message" : "command.tempban.local-message";

        String localMessage = locale.getFormatString(localStringMessage, player)
                .replace("<time>", ObjectUtil.convertTimeToString(time))
                .replace("<reason>", reason);

        Bukkit.getScheduler().runTask(Main.getInstance(), () ->
                player.kickPlayer(localMessage));
    }

    public void unban() {
        this.banInfo = null;
        FPlayerManager.getBannedPlayers().remove(getRealName());

        Main.getDataThreadPool().execute(() ->
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

        String sortName;

        if (!Bukkit.getBukkitVersion().startsWith("1.16.5")) {
            int rank = 0;

            if (HookManager.enabledLuckPerms) {
                rank = FLuckPerms.getPlayerGroupWeight(player);
            }

            sortName = ObjectUtil.generateSortString(rank, player.getName());
        } else {
            sortName = player.getName();
        }

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
        if (team == null || isRemoved) return;
        this.team.unregister();
        this.isRemoved = true;
    }

    private boolean isRemoved = false;

    @Nullable
    public Player getLastWriter() {
        return this.lastWriter;
    }

    public void setLastWriter(@NotNull Player lastWriter) {
        this.lastWriter = lastWriter;
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

        String name = config.getString("player.name." + formatName)
                .replace("<vault_prefix>", this.vaultPrefix)
                .replace("<world_prefix>", this.worldPrefix)
                .replace("<stream_prefix>", this.streamPrefix)
                .replace("<name>", getVaultConfigString(player,"player.name.<group>"))
                .replace("<player>", this.name)
                .replace("<vault_suffix>", this.vaultSuffix)
                .replace("<afk_suffix>", this.afkSuffix);

        return ObjectUtil.formatString(name, this.getPlayer());

    }

    @NotNull
    public String getDisplayName() {
        return getName("display");
    }

    @NotNull
    public String getTabName() {
        return getName("tab");
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
            String displayName = config.getString("player.name.display")
                    .replace("<vault_prefix>", this.vaultPrefix)
                    .replace("<world_prefix>", this.worldPrefix)
                    .replace("<stream_prefix>", this.streamPrefix)
                    .replace("<vault_suffix>", this.vaultSuffix)
                    .replace("<afk_suffix>", this.afkSuffix);

            String[] strings = displayName.split("<name>");
            prefix = strings.length > 0 ? ObjectUtil.formatString(strings[0], player) : "";
            suffix = strings.length > 1 ? ObjectUtil.formatString(strings[1], player) : "";
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
    public HashMap<UUID, PlayerMail> getMails() {
        return mails;
    }

    public void addMail(@NotNull UUID uuid, @NotNull PlayerMail playerMail) {
        mails.put(uuid, playerMail);
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
    public PlayerDamager getLastFDamager() {
        return lastPlayerDamager;
    }

    public void setLastDamager(@Nullable Entity lastDamager) {
        this.lastPlayerDamager.replaceDamager(lastDamager);
    }

    public void resetLastDamager() {
        this.lastPlayerDamager = new PlayerDamager();
    }

    public boolean isDeathByObject() {
        return ObjectUtil.getCurrentTime() - this.lastPlayerDamager.getTime() < 5;
    }

    public void spigotMessage(BaseComponent baseComponent) {
        if(this.player == null) return;

        this.player.spigot().sendMessage(baseComponent);
    }

    public void spigotMessage(BaseComponent[] baseComponents) {
        if(this.player == null) return;

        this.player.spigot().sendMessage(baseComponents);
    }

    public void setSpies(boolean spies) {
        this.spies = spies;
    }

    public boolean isSpies() {
        return spies;
    }

    @NotNull
    public static String getVaultLocaleString(@NotNull CommandSender sender, @NotNull String localeString) {
        return getVaultString(sender, localeString, locale);
    }

    @NotNull
    public static String getVaultConfigString(@NotNull CommandSender sender, @NotNull String configString) {
        return getVaultString(sender, configString, config);
    }

    private static String getVaultString(@NotNull CommandSender sender, @NotNull String localeString, FYamlConfiguration configuration) {
        String formattedLocaleString = configuration.getString(localeString.replaceFirst("<group>", getVaultGroup(sender)));

        return formattedLocaleString.isEmpty()
                ? configuration.getString(localeString.replaceFirst("<group>", "default"))
                : formattedLocaleString;
    }

    public static String getVaultGroup(@NotNull CommandSender sender) {
        String hoverGroup = "default";

        if (sender instanceof Player player) {

            if (HookManager.enabledLuckPerms) {
                hoverGroup = FLuckPerms.getPrimaryGroup(player);
            } else if (HookManager.enabledVault) {
                hoverGroup = FVault.getPrimaryGroup(player);
            }

            if (hoverGroup == null) hoverGroup = "default";
        }
        return hoverGroup;
    }
}
