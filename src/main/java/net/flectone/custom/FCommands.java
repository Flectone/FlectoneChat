package net.flectone.custom;

import net.flectone.managers.FPlayerManager;
import net.flectone.utils.ObjectUtil;
import net.flectone.utils.ReflectionUtil;
import net.md_5.bungee.api.chat.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import net.flectone.Main;
import net.flectone.commands.CommandAfk;
import net.flectone.managers.FileManager;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FCommands {

    public static final HashMap<String, Integer> commandsCDMap = new HashMap<>();

    private final FileManager locale = Main.locale;

    private final String command;

    private final String[] args;

    private final CommandSender sender;

    private Player player = null;

    private final String senderName;

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

        checkAndResetAfk();

        if(isConsole || !isCDEnabled()) return;

        if(hasCD()) {
            String[] replaceStrings = {"<alias>", "<time>"};
            String[] replaceTo = {alias, ObjectUtil.convertTimeToString(getCDTime() - ObjectUtil.getCurrentTime())};
            sendMeMessage("command.have_cooldown", replaceStrings, replaceTo);
            return;
        }

        if(isCDExpired()) {
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
        return Main.config.getBoolean("cooldown.enable") && Main.config.getBoolean(command + ".cooldown.enable");
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
                && !player.hasPermission(Main.config.getString(command + ".cooldown.permission"));

        return isHaveCD;
    }

    private int getCDTime() {
        return hasCD() ? commandsCDMap.get(player.getUniqueId() + command) : Main.config.getInt(command + ".cooldown.time") + ObjectUtil.getCurrentTime();
    }

    public boolean isMuted(){
        if(isConsole || !getFPlayer().isMuted()) return false;

        String[] stringsReplace = {"<time>", "<reason>"};
        String[] stringsTo = {ObjectUtil.convertTimeToString(getFPlayer().getMuteTime()), getFPlayer().getMuteReason()};

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
        return FPlayerManager.getPlayer(player.getUniqueId());
    }

    public Player getPlayer() {
        return player;
    }

    public boolean isInsufficientArgs(int count){
        boolean isInsufficientArgs = args.length < count;
        if(isInsufficientArgs){
            sendUsageMessage();
        }
        return isInsufficientArgs;
    }

    public boolean isSelfCommand(){
        if(isConsole) return false;

        return args[0].equalsIgnoreCase(player.getName());
    }

    public static boolean isOnlinePlayer(String playerName){
        return Bukkit.getOnlinePlayers().stream()
                .anyMatch(onlinePlayer -> onlinePlayer.getName().equalsIgnoreCase(playerName));
    }

    public void sendUsageMessage(){
        sendMeMessage(command + ".usage", "<command>", alias);
    }

    public void sendGlobalMessage(String message){
        sendGlobalMessage(message, true);
    }

    public void sendGlobalMessage(String format, String message){
        sendGlobalMessage(format, message, true);
    }

    public void sendGlobalMessage(String format, String message, boolean clickable){
        sendGlobalMessage(getFilteredPlayers(), format, message, null, clickable);
    }

    public void sendGlobalMessage(String message, boolean clickable){
        sendGlobalMessage(getFilteredPlayers(), message, clickable);
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

            ObjectUtil.playSound(recipient, command);

            String formatMessage = ObjectUtil.formatString(format, recipient)
                    .replace("<message>", message);

            ComponentBuilder componentBuilder = new ComponentBuilder();

            processMessage(formatMessage, componentBuilder, ChatColor.getLastColors(formatMessage), recipient, sender, itemStack);

            if(!clickable || isConsole){
                recipient.spigot().sendMessage(componentBuilder.create());
                return;
            }

            List<BaseComponent> list = componentBuilder.getParts();

            ComponentBuilder finalBuilder = new ComponentBuilder();

            boolean isFirst = true;

            for(BaseComponent baseComponent : list){

                if(isFirst){
                    baseComponent = createClickableComponent(baseComponent.toLegacyText(), senderName, recipient, sender);
                    isFirst = false;
                } else if(baseComponent.getHoverEvent() != null) isFirst = true;

                finalBuilder.append(baseComponent);
            }

            recipient.spigot().sendMessage(finalBuilder.create());
        });

        Bukkit.getConsoleSender().sendMessage(ObjectUtil.formatString(format, null).replace("<message>", message));
    }

    private Set<Player> getFilteredPlayers(){
        if(Main.config.getString(command + ".global") != null
                && !Main.config.getBoolean(command + ".global")
                && !isConsole){

            int localRange = Main.config.getInt("chat.locale.range");

            Set<Player> playerSet = player.getNearbyEntities(localRange, localRange, localRange)
                    .stream()
                    .filter(entity -> entity instanceof Player
                            && !FPlayerManager.getPlayer((Player) entity).isIgnored(player))
                    .map(entity -> (Player) entity)
                    .collect(Collectors.toSet());

            playerSet.add(player);

            return playerSet;
        }

        return Bukkit.getOnlinePlayers()
                .stream()
                .filter(onlinePlayer -> !FPlayerManager.getPlayer(onlinePlayer).isIgnored(player))
                .collect(Collectors.toSet());
    }

    public void sendMeMessage(String localeString){
        ObjectUtil.playSound(player, command);

        sender.sendMessage(locale.getFormatString(localeString, sender));
    }

    public void sendMeMessage(String localeString, String replaceString, String replaceTo){
        ObjectUtil.playSound(player, command);

        sender.sendMessage(locale.getFormatString(localeString, sender).replace(replaceString, replaceTo));
    }

    public void sendMeMessage(String localeString, String[] replaceStrings, String[] replaceTos){
        ObjectUtil.playSound(player, command);

        String formatString = locale.getFormatString(localeString, sender);

        for(int x = 0; x < replaceStrings.length; x++){
            formatString = formatString.replace(replaceStrings[x], replaceTos[x]);
        }

        sender.sendMessage(formatString);
    }

    private boolean isPlayer(CommandSender sender){
        return sender instanceof Player;
    }

    public void sendTellMessage(CommandSender firstPlayer, CommandSender secondPlayer, String typeMessage, String message){

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

        if(firstPlayer instanceof Player) ObjectUtil.playSound((Player) firstPlayer, "msg");

        ComponentBuilder getBuilder = new ComponentBuilder();

        String[] getFormatString = locale.getFormatString("msg.success_" + typeMessage, firstPlayer, secondPlayer).split("<player>");

        getBuilder.append(TextComponent.fromLegacyText(getFormatString[0]));

        //Add getter name for builder
        getBuilder.append(createClickableComponent(secondPlayer.getName(), secondPlayer.getName(), firstPlayer, secondPlayer));

        getBuilder.append(TextComponent.fromLegacyText(org.bukkit.ChatColor.getLastColors(getFormatString[1]) + getFormatString[1]), ComponentBuilder.FormatRetention.NONE);

        processMessage(message, getBuilder, org.bukkit.ChatColor.getLastColors(getFormatString[1]), firstPlayer, secondPlayer, itemStack);

        firstPlayer.spigot().sendMessage(getBuilder.create());

        if(isPlayer(firstPlayer) && isPlayer(secondPlayer)){
            FPlayerManager.getPlayer((Player) firstPlayer).setLastWriter((Player) secondPlayer);
        }

    }

    public boolean isIgnored(OfflinePlayer firstPlayer, OfflinePlayer secondPlayer){
        return FPlayerManager.getPlayer(firstPlayer).isIgnored(secondPlayer);
    }

    private void processMessage(String message, ComponentBuilder componentBuilder, String chatColor, CommandSender colorPlayer, CommandSender papiPlayer, ItemStack itemStack) {
        // Ping player in message
        String pingPrefix = Main.config.getString("chat.ping.prefix");
        String urlRegex = "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w:#@%/;$()~_?\\+-=\\\\\\.&]*)";
        Pattern pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
        BaseComponent[] colorComponent = TextComponent.fromLegacyText(chatColor);

        for(String word : message.split(" ")) {

            TextComponent wordComponent = new TextComponent(TextComponent.fromLegacyText(chatColor + word));

            chatColor = ChatColor.getLastColors(chatColor + word);

            if(word.startsWith(pingPrefix) && FCommands.isOnlinePlayer(word.substring(1))){

                Player player = Bukkit.getPlayer(word.substring(1));

                String pingMessage = Main.locale.getFormatString("chat.ping.message", colorPlayer, papiPlayer)
                        .replace("<player>", player.getName())
                        .replace("<prefix>", pingPrefix);

                if(command.equals("globalchat")) ObjectUtil.playSound(player, "chatping");

                wordComponent = createClickableComponent(pingMessage, player.getName(), colorPlayer, player);
            }

            Matcher urlMatcher = pattern.matcher(word);
            if (urlMatcher.find()) {
                wordComponent = new TextComponent(TextComponent.fromLegacyText(Main.config.getFormatString("chat.color.url", colorPlayer, papiPlayer) + word));
                wordComponent.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, word.substring(urlMatcher.start(0), urlMatcher.end(0))));
                wordComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(Main.locale.getFormatString("chat.click_url", colorPlayer, papiPlayer))));
            }

            if (itemStack != null && word.contains("%item%")) {
                String[] words;
                TranslatableComponent item = null;

                words = word.split("%item%");
                if (words.length < 2) {
                    words = new String[]{words.length > 0 ? words[0] : "", ""};
                }

                String[] formattedItemArray = ReflectionUtil.getFormattedStringItem(itemStack);
                item = new TranslatableComponent(formattedItemArray[0]);
                item.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new BaseComponent[]{new TextComponent(formattedItemArray[1])}));

                String[] componentsStrings = Main.config.getFormatString("chat.color.tooltip", colorPlayer, papiPlayer).split("<tooltip>");

                componentBuilder
                        .append(colorComponent)
                        .append(words[0])
                        .append(TextComponent.fromLegacyText(componentsStrings[0]))
                        .append(item)
                        .append(TextComponent.fromLegacyText(componentsStrings[1]))
                        .append(colorComponent)
                        .append(words[1])
                        .append(" ");

                continue;
            }

            componentBuilder.append(wordComponent, ComponentBuilder.FormatRetention.NONE).append(" ");
        }
    }

    private TextComponent createClickableComponent(String text, String playerName, CommandSender colorPlayer, CommandSender papiPlayer){
        String suggestCommand = "/msg " + playerName + " ";
        String showText = Main.locale.getFormatString("chat.click_player_name", colorPlayer, papiPlayer);

        TextComponent textComponent = new TextComponent(TextComponent.fromLegacyText(text));

        if(papiPlayer instanceof ConsoleCommandSender) return textComponent;

        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, suggestCommand));
        textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(showText)));
        return textComponent;
    }
}
