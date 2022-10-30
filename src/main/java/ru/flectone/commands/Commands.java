package ru.flectone.commands;

import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import ru.flectone.FPlayer;
import ru.flectone.Main;
import ru.flectone.utils.FileResource;
import ru.flectone.utils.PlayerUtils;
import ru.flectone.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Commands implements CommandExecutor {

    private FileResource locale = Main.locale;

    private FileResource config = Main.config;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(!(sender instanceof Player)) return true;

        Player eventPlayer = (Player) sender;
        FPlayer feventPlayer = PlayerUtils.getPlayer(eventPlayer.getUniqueId());

        switch(command.getName()){
            case "ignore": {

                if(args.length != 1){
                    sendUsageMessage(eventPlayer, command.getName());
                    break;
                }
                if(args[0].equalsIgnoreCase(sender.getName())){
                    sendMessage(eventPlayer, "ignore.myself");
                    break;
                }

                OfflinePlayer ignoredPlayer = Bukkit.getOfflinePlayer(args[0]);

                if(!ignoredPlayer.hasPlayedBefore() && !Bukkit.getOnlinePlayers().contains(ignoredPlayer)){
                    sendMessage(eventPlayer, "ignore.no_player");
                    break;
                }

                List<String> ignoreList = feventPlayer.getIgnoreList();

                String ignoredPlayerUUID = ignoredPlayer.getUniqueId().toString();

                if(ignoreList.contains(ignoredPlayerUUID)){
                    sendMessage(eventPlayer, "ignore.success_unignore", "<player>", ignoredPlayer.getName());
                    ignoreList.remove(ignoredPlayerUUID);
                } else {
                    sendMessage(eventPlayer, "ignore.success_ignore", "<player>", ignoredPlayer.getName());
                    ignoreList.add(ignoredPlayerUUID);
                }
                feventPlayer.saveIgnoreList(ignoreList);

                break;
            }
            case "msg":{

                if(args.length < 2){
                    sendUsageMessage(eventPlayer, command.getName());
                    break;
                }

                OfflinePlayer secondPlayer = Bukkit.getOfflinePlayer(args[0]);
                if(!secondPlayer.isOnline()){
                    sendMessage(eventPlayer, "msg.no_player");
                    break;
                }


                String message = argsToString(args, 0);

                if(eventPlayer.getName().equalsIgnoreCase(args[0])){
                    sendMessage(eventPlayer, "msg.myself");
                    break;
                }

                if(feventPlayer.getIgnoreList().contains(secondPlayer.getUniqueId().toString())){
                    sendMessage(eventPlayer, "msg.you_ignore");
                    break;
                }
                if(PlayerUtils.getPlayer(secondPlayer.getUniqueId()).getIgnoreList().contains(eventPlayer.getUniqueId().toString())){
                    sendMessage(eventPlayer, "msg.he_ignore");
                    break;
                }

                usingTellUtils(eventPlayer, secondPlayer.getPlayer(), "send", message);
                usingTellUtils(secondPlayer.getPlayer(), eventPlayer, "get", message);

                break;
            }
            case "reply":{
                if(args.length == 0){
                    sendUsageMessage(eventPlayer, command.getName());
                    break;
                }

                Player secondPlayer = PlayerUtils.getPlayer(eventPlayer.getUniqueId()).getLastWritePlayer();

                if(secondPlayer == null){
                    sendMessage(eventPlayer, "reply.no_answer");
                    break;
                }
                if(!secondPlayer.isOnline()){
                    sendMessage(eventPlayer, "reply.no_online");
                    break;
                }

                if(feventPlayer.getIgnoreList().contains(secondPlayer.getUniqueId().toString())){
                    sendMessage(eventPlayer, "msg.you_ignore");
                    break;
                }
                if(PlayerUtils.getPlayer(secondPlayer.getUniqueId()).getIgnoreList().contains(eventPlayer.getUniqueId().toString())){
                    sendMessage(eventPlayer, "msg.he_ignore");
                    break;
                }
                String message = argsToString(args, args.length + 1);
                usingTellUtils(eventPlayer, secondPlayer, "send", message);
                usingTellUtils(secondPlayer, eventPlayer, "get", message);

                break;
            }
            case "try-cube":{

                if(args.length != 1){
                    sendUsageMessage(eventPlayer, command.getName());
                    break;
                }
                int amount;

                try {
                    amount = Integer.parseInt(args[0]);
                } catch (NumberFormatException ignored){
                    sendMessage(eventPlayer, "try-cube.only_int");
                    break;
                }


                if(amount > config.getInt("try-cube.max_amount")){
                    sendMessage(eventPlayer, "try-cube.too_much");
                    break;
                }

                StringBuilder stringBuilder = new StringBuilder();
                int values = 0;

                while(amount-- != 0){
                    Random random = new Random();
                    int cubeType = random.nextInt(6);
                    cubeType += 1;

                    values += cubeType;
                    stringBuilder.append(config.getString("try-cube." + cubeType)).append(" ");
                }

                if(Integer.parseInt(args[0]) == 6 && values == 21 && stringBuilder.toString().equals("⚀ ⚁ ⚂ ⚃ ⚄ ⚅ ")){
                    sendGlobalMessage(eventPlayer, locale.getFormatString("try-cube.very_lucky", eventPlayer)
                            .replace("<player>", eventPlayer.getName()));
                    break;
                }

                sendGlobalMessage(eventPlayer, locale.getFormatString("try-cube.success_" + (values >= Integer.parseInt(args[0])*3), eventPlayer)
                        .replace("<cube>", stringBuilder.toString())
                        .replace("<player>", eventPlayer.getName()));

                break;
            }
            case "try":{

                if(args.length < 1){
                    sendUsageMessage(eventPlayer, command.getName());
                    break;
                }

                Random random = new Random();
                int randomPer = random.nextInt(100);
                randomPer += 1;

                sendGlobalMessage(eventPlayer, locale.getFormatString("try.success_" + (randomPer >= 50), eventPlayer)
                        .replace("<player>", eventPlayer.getName())
                        .replace("<percent>", String.valueOf(randomPer))
                        .replace("<message>", argsToString(args, args.length + 1)));

                break;

            }
            case "me":{
                if(args.length == 0){
                    sendUsageMessage(eventPlayer, command.getName());
                    break;
                }
                sendGlobalMessage(eventPlayer, locale.getFormatString("me.message", eventPlayer)
                        .replace("<player>", eventPlayer.getName())
                        .replace("<message>", argsToString(args, args.length + 1)));
                break;
            }
            case "ignore-list":{
                if(args.length > 0){
                    sendUsageMessage(eventPlayer, command.getName());
                    break;
                }

                List<String> ignoreList = PlayerUtils.getPlayer(eventPlayer.getUniqueId()).getIgnoreList();
                if(ignoreList.isEmpty()){
                    sendMessage(eventPlayer, "ignore-list.empty");
                    break;
                }

                List<Inventory> inventoryList = new ArrayList<>();

                for(int x = 0; x < (ignoreList.size()/18)+1; x++){
                    inventoryList.add(Bukkit.createInventory(null, 9*3, locale.getFormatString("ignore-list.name", eventPlayer) + (x+1)));
                }
                feventPlayer.setInventoryList(inventoryList);
                eventPlayer.openInventory(inventoryList.get(feventPlayer.getNumberLastInventory()));
                break;
            }
            case "chatcolor":{
                if(args.length == 0 || (args.length != 2 && !args[0].equalsIgnoreCase("default"))) {
                    sendUsageMessage(eventPlayer, command.getName());
                    break;
                }


                if(args[0].equalsIgnoreCase("default")){
                    feventPlayer.setColors(config.getString("color.first"), config.getString("color.second"));

                } else feventPlayer.setColors(args[0], args[1]);

                eventPlayer.sendMessage(locale.getFormatString("chatcolor.message", eventPlayer));
                break;
            }
            case "flectonechat":{
                if(args.length < 1 || !args[0].equals("reload") && args.length < 5){
                    sendUsageMessage(eventPlayer, command.getName());
                    return true;
                }

                //if command == reload then skip
                if(!args[0].equals("reload")){
                    //check "set"
                    if(!args[2].equals("set")){
                        sendUsageMessage(eventPlayer, command.getName());
                        break;
                    }
                    if(!args[3].equals("boolean") && !args[3].equals("integer") && !args[3].equals("string")){
                        sendUsageMessage(eventPlayer, command.getName());
                        break;
                    }


                    if(!config.getKeys().contains(args[1]) && !locale.getKeys().contains(args[1])){
                        sendMessage(eventPlayer, "flectonechat.not_exist");
                        break;
                    }

                    Object object = getObject(args[3], args[4]);
                    //message for set
                    if(args.length > 5){
                        object = createMessageFromArgs(args, 4);
                    }
                    //set and save file .yml
                    switch(args[0]){
                        case "config":

                            config.setObject(args[1], object);
                            config.saveFile();

                            locale.setFileConfiguration(new FileResource("language/" + config.getString("language") + ".yml"));

                            break;
                        case "locale":
                            locale.setObject(args[1], object);
                            locale.saveFile();
                            break;
                    }
                }

                Main.getInstance().reloadConfig();

                for(Player playerOnline : Bukkit.getOnlinePlayers()){
                    PlayerUtils.removePlayer(playerOnline);
                    new FPlayer(playerOnline);
                }

                sendMessage(eventPlayer, "flectonechat.message");
                break;
            }
            case "stream":{
                if(args.length < 1 || !args[0].equalsIgnoreCase("start") && !args[0].equalsIgnoreCase("end")){
                    sendUsageMessage(eventPlayer, command.getName());
                    break;
                }

                if(!feventPlayer.isStreamer() && args[0].equalsIgnoreCase("end")){
                    sendMessage(eventPlayer, "stream.not");
                    break;
                }

                if(feventPlayer.isStreamer() && args[0].equalsIgnoreCase("start")){
                    sendMessage(eventPlayer, "stream.already");
                    break;
                }

                if(args[0].equalsIgnoreCase("end")){
                    feventPlayer.setStreamer(false);
                    feventPlayer.removeFromName(config.getFormatString("stream.prefix", eventPlayer));
                    sendMessage(eventPlayer, "stream.end");
                    break;
                }

                if(args.length < 2){
                    sendMessage(eventPlayer, "stream.need_url");
                    break;
                }

                feventPlayer.setStreamer(true);
                feventPlayer.addToName(config.getFormatString("stream.prefix", eventPlayer));

                for(Player playerOnline : Bukkit.getOnlinePlayers()){

                    ComponentBuilder streamAnnounce = new ComponentBuilder();

                    for(String string : locale.getStringList("stream.start")){

                        string = string
                                .replace("<links>", createMessageFromArgs(args, 1))
                                .replace("<player>", eventPlayer.getName());

                        string = Utils.translateColor(string, playerOnline);

                        Utils.buildMessage(string, streamAnnounce,  org.bukkit.ChatColor.getLastColors(string), playerOnline);
                        streamAnnounce.append("\n");
                    }

                    playerOnline.spigot().sendMessage(streamAnnounce.create());
                }
                break;
            }

        }
        return true;
    }

    //create message from args and add color for word
    public String createMessageFromArgs(String[] args, int firstArg){
        StringBuilder message = new StringBuilder();
        //create
        for(int x = firstArg; x < args.length; x++){
            message.append(args[x]).append(" ");
        }
        return message.toString();
    }

    //get object from string
    private Object getObject(String objectName, String arg){
        switch(objectName.toLowerCase()){
            case "string": return arg;
            case "boolean": return Boolean.parseBoolean(arg);
            default: return Integer.valueOf(arg);
        }
    }

    private void usingTellUtils(Player firstPlayer, Player secondPlayer, String typeMessage, String message){
        ComponentBuilder getBuilder = new ComponentBuilder();

        String[] getFormatString = locale.getFormatString("msg.success_" + typeMessage, firstPlayer).split("<player>");
        getBuilder.append(TextComponent.fromLegacyText(getFormatString[0]));
        //Add getter name for builder
        getBuilder.append(Utils.getNameComponent(secondPlayer.getName(), secondPlayer.getName(), firstPlayer));
        getBuilder.append(TextComponent.fromLegacyText(org.bukkit.ChatColor.getLastColors(getFormatString[1]) + getFormatString[1]), ComponentBuilder.FormatRetention.NONE);
        Utils.buildMessage(message, getBuilder, org.bukkit.ChatColor.getLastColors(getFormatString[1]), firstPlayer);

        firstPlayer.spigot().sendMessage(getBuilder.create());
        PlayerUtils.getPlayer(firstPlayer.getUniqueId()).setLastWritePlayer(secondPlayer.getPlayer());
    }

    private void sendGlobalMessage(Player player, String message){
        TextComponent textComponent = Utils.getNameComponent(message, player.getName(), player);

        for(Player recipient : Bukkit.getOnlinePlayers()){
            if(PlayerUtils.getPlayer(recipient.getUniqueId()).getIgnoreList().contains(player.getUniqueId().toString())) continue;
            recipient.spigot().sendMessage(textComponent);
        }
    }

    private String argsToString(String[] args, int skipElement){
        StringBuilder finalString = new StringBuilder();
        for(int x = 0; x < args.length; x++){
            if(x == skipElement) continue;
            finalString.append(args[x]).append(" ");
        }
        return finalString.toString();
    }

    private void sendMessage(Player player, String localeString){
        player.sendMessage(locale.getFormatString(localeString, player));
    }

    private void sendMessage(Player player, String localeString, String replaceString, String replaceTo){
        player.sendMessage(locale.getFormatString(localeString, player).replace(replaceString, replaceTo));
    }

    private void sendUsageMessage(Player player, String command){
        sendMessage(player, command + ".usage");
    }
}
