package net.flectone.custom;

import net.flectone.Main;
import net.flectone.commands.CommandAfk;
import net.flectone.integrations.interactivechat.FlectoneInteractiveChat;
import net.flectone.managers.FPlayerManager;
import net.flectone.managers.FileManager;
import net.flectone.messages.MessageBuilder;
import net.flectone.utils.ObjectUtil;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

public class FCommands {

    public static final HashMap<String, Integer> commandsCDMap = new HashMap<>();
    public final static String[] formatTimeList = {"s", "m", "h", "d", "y"};
    private final FileManager locale = Main.locale;
    private final String command;
    private final String[] args;
    private final CommandSender sender;
    private final String senderName;

    private final boolean isConsole;
    private final String alias;
    private final Player player;
    private boolean isHaveCD = false;

    public FCommands(CommandSender sender, String command, String label, String[] args) {
        this.sender = sender;
        this.senderName = sender.getName();
        this.command = command;
        this.args = args;
        this.alias = label;

        this.player = (sender instanceof Player) ? ((Player) sender).getPlayer() : null;
        this.isConsole = (this.player == null);

        checkAndResetAfk();

        if (isConsole || !isCDEnabled()) return;

        if (hasCD()) {
            String[] replaceStrings = {"<alias>", "<time>"};
            String[] replaceTo = {alias, ObjectUtil.convertTimeToString(getCDTime() - ObjectUtil.getCurrentTime())};
            sendMeMessage("command.cool-down", replaceStrings, replaceTo);
            return;
        }

        if (isCDExpired()) {
            commandsCDMap.remove(getCommandKey());
            return;
        }

        commandsCDMap.put(getCommandKey(), getCDTime());
    }

    private void checkAndResetAfk() {
        if (!isConsole && getFPlayer().isAfk() && !command.equalsIgnoreCase("afk")) {
            CommandAfk.setAfkFalse(player);
        }
    }

    private boolean isCDEnabled() {
        return Main.config.getBoolean("cool-down.enable") && Main.config.getBoolean("cool-down." + command + ".enable");
    }

    private boolean isCDExpired() {
        return getCDTime() < ObjectUtil.getCurrentTime();
    }

    private String getCommandKey() {
        if (player != null) {
            return player.getUniqueId() + command;
        }
        return "";
    }

    private boolean hasCD() {
        isHaveCD = commandsCDMap.get(player.getUniqueId() + command) != null
                && commandsCDMap.get(player.getUniqueId() + command) > ObjectUtil.getCurrentTime()
                && !player.isOp()
                && !player.hasPermission(Main.config.getString("cool-down." + command + ".permission"));

        return isHaveCD;
    }

    private int getCDTime() {
        return hasCD() ? commandsCDMap.get(player.getUniqueId() + command) : Main.config.getInt("cool-down." + command + ".time") + ObjectUtil.getCurrentTime();
    }

    public boolean isMuted() {
        if (isConsole || !getFPlayer().isMuted()) return false;

        String[] stringsReplace = {"<time>", "<reason>"};
        String[] stringsTo = {ObjectUtil.convertTimeToString(getFPlayer().getMuteTime()), getFPlayer().getMuteReason()};

        sendMeMessage("command.mute.local-message", stringsReplace, stringsTo);

        return true;
    }

    public boolean isHaveCD() {
        return isHaveCD;
    }

    public String getSenderName() {
        return senderName;
    }

    public boolean isConsoleMessage() {

        if (isConsole) {
            sendMeMessage("command.not-support-console");
        }

        return isConsole;
    }

    public boolean isConsole() {
        return isConsole;
    }

    public FPlayer getFPlayer() {
        return FPlayerManager.getPlayer(player.getUniqueId());
    }

    public Player getPlayer() {
        return player;
    }

    public boolean isInsufficientArgs(int count) {
        boolean isInsufficientArgs = args.length < count;
        if (isInsufficientArgs) {
            sendUsageMessage();
        }
        return isInsufficientArgs;
    }

    public boolean isSelfCommand() {
        if (isConsole) return false;

        return args[0].equalsIgnoreCase(player.getName());
    }

    public void sendUsageMessage() {
        sendMeMessage("command." + command + ".usage", "<command>", alias);
    }

    public void sendGlobalMessage(String message) {
        sendGlobalMessage(message, true);
    }

    public void sendGlobalMessage(String format, String message) {
        sendGlobalMessage(format, message, true);
    }

    public void sendGlobalMessage(String format, String message, boolean clickable) {
        sendGlobalMessage(getFilteredPlayers(), format, message, null, clickable);
    }

    public void sendGlobalMessage(String message, boolean clickable) {
        sendGlobalMessage(getFilteredPlayers(), message, clickable);
    }

    public void sendGlobalMessage(Set<Player> set, String message, boolean clickable) {
        sendGlobalMessage(set, message, null, clickable);
    }

    public void sendGlobalMessage(Set<Player> set, String message, ItemStack item, boolean clickable) {
        sendGlobalMessage(set, message, "", item, clickable);
    }

    public void sendGlobalMessage(Set<Player> recipientsSet, String format, String message, ItemStack itemStack, boolean clickable) {

        itemStack = message.contains("%item%") ? itemStack == null ? player.getInventory().getItemInMainHand() : itemStack : null;

        Bukkit.getConsoleSender().sendMessage(ObjectUtil.formatString(format, null).replace("<message>", message));

        if (Main.isHaveInteractiveChat) {
            message = FlectoneInteractiveChat.mark(message, player.getUniqueId());
        }

        MessageBuilder messageBuilder = new MessageBuilder(command, message, itemStack, clickable);

        recipientsSet.parallelStream().forEach(recipient -> {

            ObjectUtil.playSound(recipient, command);

            recipient.spigot().sendMessage(messageBuilder.build(format, recipient, sender));
        });

        String bubbleMessage = messageBuilder.getMessage();

        if (command.contains("chat")) {
            if (Main.isHaveInteractiveChat) {
                bubbleMessage = bubbleMessage.replaceAll("(<chat=.*>)", "[]");
            }

            getFPlayer().addChatBubble(bubbleMessage);
        }
    }

    private Set<Player> getFilteredPlayers() {
        if (!Main.config.getString("command." + command + ".global").isEmpty()
                && !Main.config.getBoolean("command." + command + ".global")
                && !isConsole) {

            int localRange = Main.config.getInt("chat.local.range");

            Set<Player> playerSet = player.getNearbyEntities(localRange, localRange, localRange).parallelStream()
                    .filter(entity -> entity instanceof Player && !FPlayerManager.getPlayer((Player) entity).isIgnored(player))
                    .map(entity -> (Player) entity)
                    .collect(Collectors.toSet());

            playerSet.add(player);

            return playerSet;
        }

        return Bukkit.getOnlinePlayers().parallelStream()
                .filter(onlinePlayer -> !FPlayerManager.getPlayer(onlinePlayer).isIgnored(player))
                .collect(Collectors.toSet());
    }

    public void sendMeMessage(String localeString) {
        ObjectUtil.playSound(player, command);

        sender.sendMessage(locale.getFormatString(localeString, sender));
    }

    public void sendMeMessage(String localeString, String replaceString, String replaceTo) {
        ObjectUtil.playSound(player, command);

        sender.sendMessage(locale.getFormatString(localeString, sender).replace(replaceString, replaceTo));
    }

    public void sendMeMessage(String localeString, String[] replaceStrings, String[] replaceTos) {
        ObjectUtil.playSound(player, command);

        String formatString = locale.getFormatString(localeString, sender);

        for (int x = 0; x < replaceStrings.length; x++) {
            formatString = formatString.replace(replaceStrings[x], replaceTos[x]);
        }

        sender.sendMessage(formatString);
    }

    private boolean isPlayer(CommandSender sender) {
        return sender instanceof Player;
    }

    public void sendTellMessage(CommandSender firstPlayer, CommandSender secondPlayer, String message) {

        ItemStack itemStack = firstPlayer instanceof Player ? ((Player) firstPlayer).getInventory().getItemInMainHand() : null;

        if (Main.isHaveInteractiveChat) {
            message = FlectoneInteractiveChat.mark(message, player.getUniqueId());
        }

        MessageBuilder messageBuilder = new MessageBuilder(command, message, itemStack, true);

        if (firstPlayer instanceof Player) ObjectUtil.playSound((Player) firstPlayer, "msg");

        sendTellUtil(messageBuilder, "send", firstPlayer, secondPlayer, firstPlayer);
        sendTellUtil(messageBuilder, "get", secondPlayer, firstPlayer, firstPlayer);
    }

    private void sendTellUtil(MessageBuilder messageBuilder, String typeMessage, CommandSender firstPlayer, CommandSender secondPlayer, CommandSender sender) {
        String getFormatString1 = Main.locale.getFormatString("command.msg." + typeMessage, firstPlayer, secondPlayer)
                .replace("<player>", secondPlayer.getName());

        BaseComponent[] baseComponents1 = messageBuilder.build(getFormatString1, firstPlayer, sender);
        firstPlayer.spigot().sendMessage(baseComponents1);

        if (isPlayer(firstPlayer) && isPlayer(secondPlayer)) {
            FPlayerManager.getPlayer((Player) firstPlayer).setLastWriter((Player) secondPlayer);
        }
    }

    public boolean isIgnored(OfflinePlayer firstPlayer, OfflinePlayer secondPlayer) {
        return FPlayerManager.getPlayer(firstPlayer).isIgnored(secondPlayer);
    }

    public boolean isStringTime(String string) {
        return Arrays.stream(formatTimeList).parallel().anyMatch(string::contains);
    }

    public int getTimeFromString(String string) {
        if (string.equals("permanent") || string.equals("0")) return -1;
        int time = Integer.parseInt(string.substring(0, string.length() - 1));
        string = string.substring(string.length() - 1);

        switch (string) {
            case "y":
                time *= 30 * 12;
            case "d":
                time *= 24;
            case "h":
                time *= 60;
            case "m":
                time *= 60;
            case "s":
                break;
        }

        return time;
    }
}
