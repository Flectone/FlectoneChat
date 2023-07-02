package net.flectone.custom;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import net.flectone.Main;
import net.flectone.commands.CommandAfk;
import net.flectone.utils.FileResource;
import net.flectone.utils.PlayerUtils;
import net.flectone.utils.Utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class FCommands {

    public static final HashMap<String, Integer> commandsCDMap = new HashMap<>();

    private final FileResource locale = Main.locale;

    private final String command;

    private final String[] args;

    private final CommandSender sender;

    private Player player = null;

    private String senderName;

    private final boolean isConsole;

    private boolean isHaveCD = false;

    private final String alias;

    public FCommands(CommandSender sender, String command, String label, String[] args) {
        this.sender = sender;
        this.senderName = sender.getName();
        this.command = command;
        this.args = args;
        this.alias = label;

        this.player = (sender instanceof Player) ? ((Player) sender).getPlayer() : null;
        this.isConsole = (this.player == null);

        if(!isConsole && getFPlayer().isAfk() && !command.equalsIgnoreCase("afk")){
            CommandAfk.setAfkFalse(player);
        }

        if(isConsole || !Main.config.getBoolean("cooldown.enable") || !Main.config.getBoolean(command + ".cooldown.enable")) return;

        if(hasCooldown()) {
            String[] replaceStrings = {"<alias>", "<time>"};
            String[] replaceTo = {alias, String.valueOf(getCooldownTime() - Utils.getCurrentTime())};
            sendMeMessage("command.have_cooldown", replaceStrings, replaceTo);
            return;
        }

        if(getCooldownTime() < Utils.getCurrentTime()) {
            commandsCDMap.remove(player.getUniqueId() + command);
            return;
        }

        commandsCDMap.put(player.getUniqueId() + command, getCooldownTime());
    }

    private boolean hasCooldown() {
        isHaveCD = commandsCDMap.get(player.getUniqueId() + command) != null
                && commandsCDMap.get(player.getUniqueId() + command) > Utils.getCurrentTime()
                && !player.isOp()
                && !player.hasPermission(Main.config.getString(command + ".cooldown.permission"));

        return isHaveCD;
    }

    private int getCooldownTime() {
        return hasCooldown() ? commandsCDMap.get(player.getUniqueId() + command) : Main.config.getInt(command + ".cooldown.time") + Utils.getCurrentTime();
    }

    public boolean isMuted(){
        if(isConsole || !getFPlayer().isMuted()) return false;

        String[] stringsReplace = {"<time>", "<reason>"};
        String[] stringsTo = {String.valueOf(getFPlayer().getTimeMuted()), getFPlayer().getReasonMute()};

        sendMeMessage("mute.success_get", stringsReplace, stringsTo);

        return true;
    }

    public boolean isHaveCD() {
        return isHaveCD;
    }

    public String getSenderName() {
        return senderName;
    }

    public boolean isConsoleMessage() {

        if(isConsole){
            sendMeMessage("command.not_support_console");
        }

        return isConsole;
    }

    public boolean isConsole(){
        return isConsole;
    }


    public FPlayer getFPlayer(){
        return PlayerUtils.getPlayer(((Player) sender).getUniqueId());
    }

    public boolean checkCountArgs(int count){
        boolean is = args.length < count;
        if(is){
            sendUsageMessage();
        }
        return is;
    }

    public boolean isYourselfCommand(){
        if(isConsole) return false;

        return args[0].equalsIgnoreCase(player.getName());
    }

    public static boolean isRealOfflinePlayer(String playerName){
        return Arrays.stream(Bukkit.getOfflinePlayers())
                .anyMatch(offlinePlayer -> offlinePlayer.getName().equalsIgnoreCase(playerName));
    }

    public static boolean isRealOnlinePlayer(String playerName){
        return Bukkit.getOnlinePlayers().stream()
                .anyMatch(onlinePlayer -> onlinePlayer.getName().equalsIgnoreCase(playerName));
    }

    public void sendUsageMessage(){
        sendMeMessage(command + ".usage", "<command>", alias);
    }

    public void sendGlobalMessage(String message){
        sendGlobalMessage(message, true);
    }

    public void sendGlobalMessage(String message, boolean clickable){
        Set<Player> newSet = Bukkit.getOnlinePlayers()
                .stream()
                .filter(onlinePlayer -> !PlayerUtils.getPlayer(onlinePlayer).checkIgnoreList(player))
                .collect(Collectors.toSet());

        sendGlobalMessage(newSet, message, clickable);
    }

    public void sendGlobalMessage(Set<Player> set, String message, boolean clickable){
        sendGlobalMessage(set, message, null, clickable);
    }

    public void sendGlobalMessage(Set<Player> set, String message, ItemStack item, boolean clickable){
        sendGlobalMessage(set, message, "", item, clickable);
    }

    public void sendGlobalMessage(Set<Player> recipientsSet, String format, String message, ItemStack item, boolean clickable){

        ItemStack itemStack = message.contains("%item%") ? item == null ? player.getItemInHand() : item : null;

        recipientsSet.forEach(recipient -> {

            String formatMessage = Utils.translateColor(format, recipient) + message;

            ComponentBuilder componentBuilder = new ComponentBuilder();

            Utils.buildMessage(formatMessage, componentBuilder, ChatColor.getLastColors(formatMessage), recipient, sender, itemStack);

            if(!clickable || isConsole){
                recipient.spigot().sendMessage(componentBuilder.create());
                return;
            }

            List<BaseComponent> list = componentBuilder.getParts();

            ComponentBuilder finalBuilder = new ComponentBuilder();

            boolean isFirst = true;

            for(BaseComponent baseComponent : list){

                if(isFirst){
                    baseComponent = Utils.getNameComponent(baseComponent.toLegacyText(), senderName, recipient, sender);
                    isFirst = false;
                } else if(baseComponent.getHoverEvent() != null) isFirst = true;

                finalBuilder.append(baseComponent);
            }

            recipient.spigot().sendMessage(finalBuilder.create());
        });

        Bukkit.getConsoleSender().sendMessage(Utils.translateColor(message, null));
    }

    public void sendMeMessage(String localeString){
        sender.sendMessage(locale.getFormatString(localeString, sender));
    }

    public void sendMeMessage(String localeString, String replaceString, String replaceTo){
        sender.sendMessage(locale.getFormatString(localeString, sender).replace(replaceString, replaceTo));
    }

    public void sendMeMessage(String localeString, String[] replaceStrings, String[] replaceTos){
        String formatString = locale.getFormatString(localeString, sender);

        for(int x = 0; x < replaceStrings.length; x++){
            formatString = formatString.replace(replaceStrings[x], replaceTos[x]);
        }

        sender.sendMessage(formatString);
    }

    private boolean isPlayer(CommandSender sender){
        return sender instanceof Player;
    }

    public void usingTellUtils(CommandSender firstPlayer, CommandSender secondPlayer, String typeMessage, String message){

        ItemStack itemStack = null;

        if(message.contains("%item%")){
            switch (typeMessage){
                case "send":
                    if(!isPlayer(firstPlayer)) break;
                    itemStack = ((Player) firstPlayer).getItemInHand();
                    break;
                case "get":
                    if(!isPlayer(secondPlayer)) break;
                    itemStack = ((Player) secondPlayer).getItemInHand();
                    break;
            }
        }

        ComponentBuilder getBuilder = new ComponentBuilder();

        String[] getFormatString = locale.getFormatString("msg.success_" + typeMessage, firstPlayer, secondPlayer).split("<player>");

        getBuilder.append(TextComponent.fromLegacyText(getFormatString[0]));

        //Add getter name for builder
        getBuilder.append(Utils.getNameComponent(secondPlayer.getName(), secondPlayer.getName(), firstPlayer, secondPlayer));

        getBuilder.append(TextComponent.fromLegacyText(org.bukkit.ChatColor.getLastColors(getFormatString[1]) + getFormatString[1]), ComponentBuilder.FormatRetention.NONE);

        Utils.buildMessage(message, getBuilder, org.bukkit.ChatColor.getLastColors(getFormatString[1]), firstPlayer, secondPlayer, itemStack);

        firstPlayer.spigot().sendMessage(getBuilder.create());

        if(isPlayer(firstPlayer) && isPlayer(secondPlayer)){
            PlayerUtils.getPlayer(((Player) firstPlayer).getUniqueId()).setLastWritePlayer((Player) secondPlayer);
        }

    }

    public boolean checkIgnoreList(OfflinePlayer firstPlayer, OfflinePlayer secondPlayer){
        return Main.ignores.getStringList(firstPlayer.getUniqueId().toString()).contains(secondPlayer.getUniqueId().toString());
    }
}
