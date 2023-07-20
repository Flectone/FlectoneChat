package net.flectone.messages;

import net.flectone.Main;
import net.flectone.managers.FPlayerManager;
import net.flectone.utils.ObjectUtil;
import net.flectone.utils.ReflectionUtil;
import net.md_5.bungee.api.chat.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MessageBuilder {

    private final LinkedHashMap<Integer, WordParams> messageHashMap = new LinkedHashMap<>();

    private static final Pattern urlPattern = Pattern.compile("((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w:#@%/;$()~_?\\+-=\\\\\\.&]*)", Pattern.CASE_INSENSITIVE);

    private final ComponentBuilder componentBuilder = new ComponentBuilder();

    private final ItemStack itemStack;

    private String command;

    public MessageBuilder(String command, String text, ItemStack itemStack, boolean clickable) {
        this.itemStack = itemStack;
        this.command = command;

        String pingPrefix = Main.locale.getString("chat.ping.prefix");

        AtomicInteger index = new AtomicInteger();

        Arrays.stream(text.split(" ")).parallel().map(word -> {
            WordParams wordParams = new WordParams();

            word = replacePattern(word);

            if (itemStack != null && word.equalsIgnoreCase("%item%")) {
                wordParams.setItem(true);
                wordParams.setText("\uD83D\uDD32");
                return wordParams;
            }

            if (word.startsWith(pingPrefix)) {
                String playerName = word.replaceFirst(pingPrefix, "");

                if(FPlayerManager.getPlayerFromName(playerName) != null){
                    Player player = Bukkit.getPlayer(playerName);

                    word = Main.locale.getString("chat.ping.message")
                            .replace("<player>", player.getName())
                            .replace("<prefix>", pingPrefix);

                    wordParams.setClickable(clickable, player.getName());
                    wordParams.setPlayerPing(true);

                    if(command.equals("globalchat")) ObjectUtil.playSound(player, "chatping");

                }
            }

            if (word.startsWith("||") && word.endsWith("||") && !word.replace("||", "").isEmpty()) {
                word = word.replace("||", "");

                wordParams.setHideMessage(word);
                wordParams.setHide(true);

                word = Main.locale.getString("chat.hide.message")
                        .repeat(word.length());
            }

            Matcher urlMatcher = urlPattern.matcher(word);
            if (urlMatcher.find()) {
                wordParams.setUrl(word.substring(urlMatcher.start(0), urlMatcher.end(0)));

                word = Main.locale.getString("chat.url.message")
                        .replace("<url>", word);
            }

            wordParams.setText(word);
            return wordParams;
        })
        .forEachOrdered(wordParams -> messageHashMap.put(index.getAndIncrement(), wordParams));
    }

    public String getMessage() {
        return messageHashMap.values().parallelStream()
                .map(wordParams -> {
                    String word = wordParams.getText();
                    if (wordParams.isEdited()) {
                        word = ObjectUtil.formatString(word, null);
                        word = ChatColor.stripColor(word);
                    }
                    return word;
                })
                .collect(Collectors.joining(" "));
    }

    public BaseComponent[] build(String format, CommandSender recipient, CommandSender sender){
        ComponentBuilder componentBuilder = new ComponentBuilder();

        String[] formats = ObjectUtil.formatString(format, recipient, sender).split("<message>");
        componentBuilder.append(createClickableComponent(formats[0], sender.getName(), recipient, sender));

        String color = ChatColor.getLastColors(formats[0]);

        componentBuilder.append(buildMessage(color, recipient, sender), ComponentBuilder.FormatRetention.NONE);

        if(formats.length > 1) componentBuilder.append(TextComponent.fromLegacyText(color + formats[1]), ComponentBuilder.FormatRetention.NONE);

        return componentBuilder.create();
    }

    private BaseComponent[] buildMessage(String lastColor, CommandSender recipient, CommandSender sender){
        ComponentBuilder componentBuilder = new ComponentBuilder();

        for(Map.Entry<Integer, WordParams> entry : messageHashMap.entrySet()){
            String word = entry.getValue().getText();
            WordParams wordParams = entry.getValue();

            if((sender.isOp() || sender.hasPermission("flectonechat.formatting")) && !wordParams.isEdited()){
                String color1 = ChatColor.getLastColors(word);
                word = ObjectUtil.formatString(true, word, recipient, sender);
                String color2 = ChatColor.getLastColors(word);

                wordParams.setFormatted(!color1.equals(color2));
                wordParams.setText(word);
            }

            TextComponent wordComponent = new TextComponent(componentFromText(lastColor + word));

            if(!wordParams.isEdited() || wordParams.isFormatted()) lastColor = ChatColor.getLastColors(lastColor + word);

            if(wordParams.isItem()){
                componentBuilder.append(createItemComponent(itemStack, lastColor, recipient, sender));
                continue;
            }

            if(wordParams.isPlayerPing()){
                String playerPingMessage = ObjectUtil.formatString(word, recipient, sender);
                wordComponent = new TextComponent(componentFromText(playerPingMessage));
            }

            if(wordParams.isClickable()){
                wordComponent = createClickableComponent(wordComponent, wordParams.getPlayerPingName(), recipient, sender);
            }

            if(wordParams.isUrl()){
                word = ObjectUtil.formatString(word, recipient, sender);
                wordComponent = createUrlComponent(ObjectUtil.formatString(word, recipient, sender), wordParams.getUrl(), recipient, sender);
            }

            if(wordParams.isHide()){
                ClickEvent clickEvent = wordComponent.getClickEvent();
                wordComponent = new TextComponent(TextComponent.fromLegacyText(ObjectUtil.formatString(word, recipient, sender)));
                wordComponent.setClickEvent(clickEvent);
                wordComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(lastColor + wordParams.getHideMessage())));
            }

            componentBuilder
                    .append(wordComponent, ComponentBuilder.FormatRetention.NONE)
                    .append(" ");
        }

        return componentBuilder.create();
    }


    private TextComponent createUrlComponent(String text, String url, CommandSender recipient, CommandSender sender){
        TextComponent wordComponent = new TextComponent(componentFromText(text));
        wordComponent.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
        wordComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(Main.locale.getFormatString("chat.url.hover-message", recipient, sender))));
        return wordComponent;
    }

    private BaseComponent[] createItemComponent(ItemStack itemStack, String lastColor, CommandSender recipient, CommandSender sender){
        ComponentBuilder itemBuilder = new ComponentBuilder();

        TranslatableComponent item;

        String[] formattedItemArray = ReflectionUtil.getFormattedStringItem(itemStack);
        item = new TranslatableComponent(formattedItemArray[0]);
        item.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new BaseComponent[]{new TextComponent(formattedItemArray[1])}));

        String[] componentsStrings = Main.locale.getFormatString("chat.tooltip.message", recipient, sender).split("<tooltip>");

        return itemBuilder
                .append(componentFromText(lastColor))
                .append(componentFromText(componentsStrings[0]))
                .append(item)
                .append(componentFromText(componentsStrings[1]))
                .append(componentFromText(lastColor))
                .append(" ")
                .create();
    }

    public BaseComponent[] create(){
        return componentBuilder.create();
    }

    private BaseComponent[] componentFromText(String text){
        return TextComponent.fromLegacyText(text);
    }

    private TextComponent createClickableComponent(String text, String playerName, CommandSender recipient, CommandSender sender){
        TextComponent textComponent = new TextComponent(componentFromText(text));
        return createClickableComponent(textComponent, playerName, recipient, sender);
    }

    private TextComponent createClickableComponent(TextComponent textComponent, String playerName, CommandSender recipient, CommandSender sender){
        String suggestCommand = "/msg " + playerName + " ";
        String showText = Main.locale.getFormatString("player.name.hover-message", recipient, sender);

        if(sender instanceof ConsoleCommandSender) return textComponent;

        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, suggestCommand));
        textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(showText)));
        return textComponent;
    }

    private static final HashMap<String, String> patternMap = new HashMap<>();

    static {
        loadPatterns();
    }

    public static void loadPatterns(){
        patternMap.clear();

        Main.config.getStringList("chat.patterns")
                .forEach(patternString -> {
                    String[] patternComponents = patternString.split(" , ");
                    if(patternComponents.length < 2) return;

                    patternMap.put(patternComponents[0], patternComponents[1]);
                });
    }

    private String replacePattern(String word){
        String wordLowerCased = word.toLowerCase();

        Map.Entry<String, String> pattern = patternMap.entrySet()
                .parallelStream()
                .filter(entry -> wordLowerCased.contains(entry.getKey().toLowerCase()))
                .findFirst()
                .orElse(null);

        if(pattern == null) return word;

        return wordLowerCased.replace(pattern.getKey().toLowerCase(), pattern.getValue());
    }
}
