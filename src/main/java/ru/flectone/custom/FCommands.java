package ru.flectone.custom;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.flectone.Main;
import ru.flectone.utils.FileResource;
import ru.flectone.utils.PlayerUtils;
import ru.flectone.utils.Utils;

import java.util.Arrays;
import java.util.List;

public class FCommands {

    private FileResource locale = Main.locale;

    private String command;

    private String[] args;

    private CommandSender sender;

    private Player player = null;

    private String senderName;

    private boolean isConsole;

    private String alias;

    public FCommands(CommandSender sender, String command, String label, String[] args){
        if(sender instanceof Player) {
            this.player = ((Player) sender).getPlayer();

        }
        isConsole = this.player == null;
        this.senderName = sender.getName();

        this.sender = sender;
        this.command = command;
        this.args = args;
        this.alias = label;
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

    public void sendUsageMessage(){
        sendMeMessage(command + ".usage", "<command>", alias);
    }

    public void sendGlobalMessage(String message){

        ItemStack itemStack = message.contains("%item%") ? player.getItemInHand() : null;

        Bukkit.getOnlinePlayers().stream()
                .filter(onlinePlayer -> !PlayerUtils.getPlayer(onlinePlayer).checkIgnoreList(player))
                .forEach(onlinePlayer -> {

                    String formatMessage = Utils.translateColor(message, onlinePlayer);

                    ComponentBuilder componentBuilder = new ComponentBuilder();

                    Utils.buildMessage(formatMessage, componentBuilder, ChatColor.getLastColors(formatMessage), sender, itemStack);
                    List<BaseComponent> list = componentBuilder.getParts();

                    ComponentBuilder finalBuilder = new ComponentBuilder();

                    list.forEach(baseComponent -> {
                        if(baseComponent.getHoverEvent() == null)
                            baseComponent = Utils.getNameComponent(baseComponent.toLegacyText(), senderName, sender);

                        finalBuilder.append(baseComponent);
                    });

                    onlinePlayer.spigot().sendMessage(finalBuilder.create());
        });

        Bukkit.getConsoleSender().sendMessage(Utils.translateColor(message, null));
    }

    public void sendMeMessage(String localeString){
        sender.sendMessage(locale.getFormatString(localeString, sender));
    }

    public void sendMeMessage(String localeString, String replaceString, String replaceTo){
        player.sendMessage(locale.getFormatString(localeString, player).replace(replaceString, replaceTo));
    }

    public void sendMeMessage(String localeString, String[] replaceStrings, String[] replaceTos){
        String formatString = locale.getFormatString(localeString, player);

        for(int x = 0; x < replaceStrings.length; x++){
            formatString = formatString.replace(replaceStrings[x], replaceTos[x]);
        }

        player.sendMessage(formatString);
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

        String[] getFormatString = locale.getFormatString("msg.success_" + typeMessage, firstPlayer).split("<player>");
        getBuilder.append(TextComponent.fromLegacyText(getFormatString[0]));
        //Add getter name for builder
        getBuilder.append(Utils.getNameComponent(secondPlayer.getName(), secondPlayer.getName(), firstPlayer));
        getBuilder.append(TextComponent.fromLegacyText(org.bukkit.ChatColor.getLastColors(getFormatString[1]) + getFormatString[1]), ComponentBuilder.FormatRetention.NONE);
        Utils.buildMessage(message, getBuilder, org.bukkit.ChatColor.getLastColors(getFormatString[1]), firstPlayer, itemStack);

        firstPlayer.spigot().sendMessage(getBuilder.create());

        if(isPlayer(firstPlayer) && isPlayer(secondPlayer)){
            PlayerUtils.getPlayer(((Player) firstPlayer).getUniqueId()).setLastWritePlayer((Player) secondPlayer);
        }

    }
}
