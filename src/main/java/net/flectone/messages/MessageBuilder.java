package net.flectone.messages;

import net.flectone.Main;
import net.flectone.custom.FCommands;
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

    private ItemStack itemStack;

    public MessageBuilder(String text, ItemStack itemStack, boolean clickable) {
        this.itemStack = itemStack;

        String pingPrefix = Main.locale.getString("chat.ping.prefix");
        List<String> patternList = Main.config.getStringList("chat.patterns");

        AtomicInteger index = new AtomicInteger();

        Arrays.stream(text.split(" ")).map(word -> {
            WordParams wordParams = new WordParams();

            for (String patternString : patternList) {
                String[] patternComponents = patternString.split(" , ");
                if(patternComponents.length < 2) continue;
                word = word.toLowerCase().replace(patternComponents[0].toLowerCase(), patternComponents[1]);
            }

            if (itemStack != null && word.equalsIgnoreCase("%item%")) {
                wordParams.setItem(true);
                wordParams.setText("\uD83D\uDD32");
                return wordParams;
            }

            if (FCommands.isContainsPlayerName(word) && clickable) {
                wordParams.setClickable(true, Bukkit.getPlayer(word).getName());
            }

            if (word.startsWith(pingPrefix)) {
                String playerName = word.replaceFirst(pingPrefix, "");

                if(FCommands.isOnlinePlayer(playerName)){
                    Player player = Bukkit.getPlayer(playerName);

                    word = Main.locale.getString("chat.ping.message")
                            .replace("<player>", player.getName())
                            .replace("<prefix>", pingPrefix);

                    wordParams.setClickable(clickable, player.getName());
                    wordParams.setPlayerPing(true);
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
        .forEach(wordParams -> messageHashMap.put(index.getAndIncrement(), wordParams));
    }

    public String getMessage() {
        return messageHashMap.values().stream()
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

        format = ObjectUtil.formatString(format, recipient, sender)
                .replace("<message>", "");

        componentBuilder.append(createClickableComponent(format, sender.getName(), recipient, sender));

        return build(componentBuilder.create(), ChatColor.getLastColors(format), recipient, sender);
    }

    public BaseComponent[] build(BaseComponent[] baseComponents, String lastColor, CommandSender recipient, CommandSender sender){
        ComponentBuilder componentBuilder = new ComponentBuilder();

        componentBuilder.append(baseComponents);

        for(Map.Entry<Integer, WordParams> entry : messageHashMap.entrySet()){
            String word = entry.getValue().getText();
            WordParams wordParams = entry.getValue();

            if(sender.isOp() || sender.hasPermission("flectonechat.formatting")){
                word = ObjectUtil.formatString(word, recipient, sender);
                wordParams.setFormatted(true);
            }

            TextComponent wordComponent = new TextComponent(componentFromText(lastColor + word));

            lastColor = ChatColor.getLastColors(lastColor + word);

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
}
