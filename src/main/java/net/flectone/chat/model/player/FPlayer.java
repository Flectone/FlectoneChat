package net.flectone.chat.model.player;

import lombok.Getter;
import lombok.Setter;
import net.flectone.chat.FlectoneChat;
import net.flectone.chat.database.sqlite.Database;
import net.flectone.chat.manager.FModuleManager;
import net.flectone.chat.manager.FPlayerManager;
import net.flectone.chat.model.damager.PlayerDamager;
import net.flectone.chat.model.file.FConfiguration;
import net.flectone.chat.model.mail.Mail;
import net.flectone.chat.model.sound.FSound;
import net.flectone.chat.module.FModule;
import net.flectone.chat.module.integrations.IntegrationsModule;
import net.flectone.chat.module.player.afkTimeout.AfkTimeoutModule;
import net.flectone.chat.module.player.nameTag.NameTagModule;
import net.flectone.chat.module.player.world.WorldModule;
import net.flectone.chat.module.sounds.SoundsModule;
import net.flectone.chat.util.MessageUtil;
import net.flectone.chat.util.PlayerUtil;
import net.flectone.chat.util.TimeUtil;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Getter
public class FPlayer {

    private Player player;
    private OfflinePlayer offlinePlayer;
    private UUID uuid;
    private String minecraftName;
    private String ip = "0.0.0.0";
    private Team team;
    @Setter
    private Moderation mute;
    @Setter
    private Moderation ban;
    @Setter
    private List<Moderation> warnList = new ArrayList<>();
    @Setter
    private List<UUID> ignoreList = new ArrayList<>();
    @Setter
    private List<Mail> mailList = new ArrayList<>();
    @Setter
    private Settings settings;
    private final Queue<String> chatBubbles = new PriorityQueue<>();
    private PlayerDamager playerDamager = new PlayerDamager();
    @Setter
    private Player lastWriter;
    @Setter
    private String streamPrefix = "";
    @Setter
    private String worldPrefix = "";
    @Setter
    private String afkSuffix = "";

    private final FPlayerManager playerManager;
    private final FModuleManager moduleManager;
    private final Scoreboard scoreboard;
    private final Database database;
    private final FConfiguration locale;
    private final FConfiguration cooldowns;
    private final FConfiguration commands;

    private FPlayer() {
        FlectoneChat plugin = FlectoneChat.getPlugin();
        playerManager = plugin.getPlayerManager();
        moduleManager = plugin.getModuleManager();
        scoreboard = plugin.getScoreBoard();
        database = plugin.getDatabase();
        locale = plugin.getFileManager().getLocale();
        cooldowns = plugin.getFileManager().getCooldowns();
        commands = plugin.getFileManager().getCommands();
    }

    public FPlayer(@NotNull Player player) {
        this();
        this.player = player;
        this.offlinePlayer = player;
        this.uuid = player.getUniqueId();
        this.minecraftName = player.getName();
        this.ip = PlayerUtil.getIP(player);
    }

    public FPlayer(@NotNull OfflinePlayer offlinePlayer) {
        this();
        this.offlinePlayer = offlinePlayer;
        this.uuid = offlinePlayer.getUniqueId();
        this.minecraftName = offlinePlayer.getName() != null ? offlinePlayer.getName() : "Unknown";
    }

    public void init() {
        playerManager.add(this);

        fromDatabase();

        database.execute(this::reloadStreamPrefix);

        registerWorldPrefix();
        registerTeam();
    }

    public void reloadStreamPrefix() {
        if (!getPlayer().hasPermission("flectonechat.commands.stream")) return;
        if (getSettings() == null) return;

        String value = getSettings().getValue(Settings.Type.STREAM);
        String typePrefix = value != null && value.equals("1") ? "online" : "offline";

        String prefix = locale.getVaultString(getPlayer(), "commands.stream." + typePrefix + "-prefix");
        setStreamPrefix(MessageUtil.formatAll(getPlayer(), prefix));
    }

    public void terminate() {

        FModule fModule = moduleManager.get(AfkTimeoutModule.class);
        if (fModule instanceof AfkTimeoutModule afkTimeoutModule) {
            afkTimeoutModule.removePlayer(this.uuid);
        }

        toDatabase();
        unregisterTeam();
        playerManager.remove(this);
    }

    public void registerTeam() {
        FModule fModule = moduleManager.get(NameTagModule.class);
        if (fModule instanceof NameTagModule nameTagModule) {
            this.team = nameTagModule.getTeam(player);
            return;
        }

        team = scoreboard.getTeam(player.getName());
        if (team == null) team = scoreboard.registerNewTeam(player.getName());

        if (!team.hasEntry(player.getName())) {
            team.addEntry(player.getName());
        }
    }

    public void unregisterTeam() {
        if (team == null) return;
        try {
            team.unregister();
        } catch (IllegalStateException ignore) {}
    }

    public void registerWorldPrefix() {
        FModule fModule = moduleManager.get(WorldModule.class);
        if (fModule instanceof WorldModule worldModule) {
            setWorldPrefix(worldModule.getPrefix(player, player.getWorld()));
        }
    }

    public void toDatabase() {
        database.toDatabase(this);
    }

    public void fromDatabase() {
        database.fromDatabase(this);
    }

    public boolean isMuted() {
        return mute != null && !mute.isExpired();
    }

    public boolean isBanned() {
        return ban != null && !ban.isExpired();
    }

    public void mute(String reason, int time, String moderator) {
        int finalTime = time == -1 ? -1 : time + TimeUtil.getCurrentTime();
        setMute(new Moderation(this.uuid.toString(), finalTime, reason, moderator, Moderation.Type.MUTE));

        database.execute(() -> database.updateFPlayer("mutes", mute));

        playerManager.getMutedPlayers().add(getMinecraftName());

        if (player != null) {
            IntegrationsModule.mutePlasmoVoice(player, moderator == null ? null : UUID.fromString(moderator), time, reason);
        }
    }

    public void unmute() {
        this.mute = null;
        playerManager.getMutedPlayers().remove(getMinecraftName());

        database.execute(() -> database.deleteRow("mutes", "player", uuid.toString()));

        if (player != null) {
            IntegrationsModule.unmutePlasmoVoice(player);
        }
    }

    public void ban(String reason, int time, String moderator) {
        int finalTime = time == -1 ? -1 : time + TimeUtil.getCurrentTime();

        setBan(new Moderation(this.uuid.toString(), finalTime, reason, moderator, Moderation.Type.BAN));

        database.execute(() -> database.updateFPlayer("bans", ban));

        playerManager.getBannedPlayers().add(getUuid());

        if (player == null) return;

        String localStringMessage = time == -1
                ? ".permanent-player-message"
                : ".player-message";

        String localMessage = locale.getVaultString(player, "commands.ban." + localStringMessage)
                .replace("<time>", TimeUtil.convertTime(player, time))
                .replace("<reason>", reason)
                .replace("<moderator>", ban.getModeratorName());

        Bukkit.getScheduler().runTask(FlectoneChat.getPlugin(), () ->
                player.kickPlayer(MessageUtil.formatAll(player, localMessage)));
    }

    public void unban() {
        this.ban = null;
        playerManager.getBannedPlayers().remove(getUuid());

        database.execute(() -> database.deleteRow("bans", "player", uuid.toString()));

    }

    public void warn(@NotNull String reason, int time, @Nullable String moderator) {
        int finalTime = time == -1 ? -1 : time + TimeUtil.getCurrentTime();

        Moderation warn = new Moderation(this.uuid.toString(), finalTime, reason, moderator, Moderation.Type.WARN);
        getWarnList().add(warn);

        playerManager.getWarnsPlayers().put(getMinecraftName(), getWarnList());

        int count = getCountWarns();
        String warnAction = commands.getString("warn.action." + count);
        if (!warnAction.isEmpty()) {
            Bukkit.getScheduler().runTask(FlectoneChat.getPlugin(), () ->
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), warnAction.replace("<player>", getMinecraftName())));
        }

        database.execute(() -> database.updateFPlayer("warns", warn));
    }

    public void unwarn(int index) {
        Moderation warn = warnList.get(index);
        warnList.remove(index);

        playerManager.getWarnsPlayers().put(getMinecraftName(), getWarnList());

        database.execute(() -> database.removeWarn(warn));
    }

    public int getCountWarns() {
       return (int) warnList.stream().filter(warn -> warn.getRemainingTime() > 0).count();
    }

    public void addChatBubble(String message) {
        chatBubbles.add(message);
    }

    public void setLastDamager(@Nullable Entity lastDamager) {
        this.playerDamager.replaceDamager(lastDamager);
    }

    public void setLastDamager(@Nullable PlayerDamager playerDamager) {
        this.playerDamager = playerDamager;
    }

    public boolean isAfk() {
        return !afkSuffix.isEmpty();
    }

    public boolean isStreaming() {
        String value = getSettings().getValue(Settings.Type.STREAM);
        return value != null && value.equals("1");
    }


    private static final HashMap<String, Integer> COOLDOWN_MAP = new HashMap<>();
    private Integer cooldownTime;

    public boolean isHaveCooldown(@NotNull String action) {
        String cooldownMapKey = getUuid() + action;

        if (!cooldowns.getVaultBoolean(player, action + ".enable")) return false;

        cooldownTime = COOLDOWN_MAP.get(cooldownMapKey);
        boolean isHaveCooldown = cooldownTime != null
                && cooldownTime > TimeUtil.getCurrentTime()
                && !getPlayer().hasPermission("flectonechat.cooldowns." + action + ".bypass");

        if (!isHaveCooldown) {
            cooldownTime = cooldowns.getVaultInt(getPlayer(), action + ".time");
            COOLDOWN_MAP.put(cooldownMapKey, cooldownTime + TimeUtil.getCurrentTime());
        }

        return isHaveCooldown;
    }

    public void sendCDMessage(@NotNull String action) {
        String message = locale.getVaultString(player, "commands.cooldown");
        message = message
                .replace("<alias>", action)
                .replace("<time>", TimeUtil.convertTime(player, cooldownTime - TimeUtil.getCurrentTime()));
        message = MessageUtil.formatAll(player, message);

        player.sendMessage(message);
    }

    public void sendMutedMessage() {
        String message = locale.getVaultString(player, "commands.muted");

        Moderation mute = getMute();
        message = message
                .replace("<time>", TimeUtil.convertTime(player, mute.getTime() - TimeUtil.getCurrentTime()))
                .replace("<reason>", mute.getReason())
                .replace("<moderator>", mute.getModeratorName());

        message = MessageUtil.formatAll(player, message);

        player.sendMessage(message);
    }

    public void playSound(@NotNull Location location, @NotNull String action) {
        FModule fModule = moduleManager.get(SoundsModule.class);
        if (fModule instanceof SoundsModule soundsModule) {
            soundsModule.play(new FSound(player, location, action));
        }
    }

    public void playSound(@NotNull String action) {
        playSound(player.getLocation(), action);
    }

    public void playSound(@Nullable Player sender, @NotNull Player recipient, @NotNull String action) {
        FModule fModule = moduleManager.get(SoundsModule.class);
        if (fModule instanceof SoundsModule soundsModule) {
            soundsModule.play(new FSound(sender, recipient, action));
        }
    }

    public void playSound(@Nullable Player sender, @NotNull Collection<Player> recipients, @NotNull String action) {
        recipients.forEach(recipient -> playSound(sender, recipient, action));
    }

    public static void sendToConsole(@NotNull String message) {
        Bukkit.getConsoleSender().sendMessage(MessageUtil.formatAll(null, message));
    }

    public static void sendToConsole(@NotNull BaseComponent[] baseComponents) {
        Bukkit.getConsoleSender().spigot().sendMessage(baseComponents);
    }
}
