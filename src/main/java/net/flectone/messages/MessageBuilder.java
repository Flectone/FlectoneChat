package net.flectone.messages;

import net.flectone.managers.FPlayerManager;
import net.flectone.misc.components.FComponent;
import net.flectone.misc.components.FLocaleComponent;
import net.flectone.misc.components.FPlayerComponent;
import net.flectone.misc.components.FURLComponent;
import net.flectone.misc.entity.FPlayer;
import net.flectone.utils.BlackListUtil;
import net.flectone.utils.ObjectUtil;
import net.flectone.utils.Pair;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static net.flectone.managers.FileManager.config;
import static net.flectone.managers.FileManager.locale;

public class MessageBuilder {

    private static final Pattern urlPattern = Pattern.compile("((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w:#@%/;$()~_?+-=\\\\.&]*)", Pattern.CASE_INSENSITIVE);
    private static final HashMap<String, String> patternMap = new HashMap<>();

    static {
        loadPatterns();
    }

    private final List<WordParams> messages = new ArrayList<>();
    private final ComponentBuilder componentBuilder = new ComponentBuilder();
    private final ItemStack itemStack;
    private final String command;
    private final CommandSender sender;
    private final static List<String> patterns = List.of("||", "**", "__", "##", "??", "~~");

    private final boolean clickable;

    public MessageBuilder(@NotNull String command, @NotNull String text, @Nullable CommandSender sender, @Nullable ItemStack itemStack, boolean clickable) {
        this.itemStack = itemStack;
        this.command = command;
        this.clickable = clickable;
        this.sender = sender;

        if (config.getBoolean("chat.swear-protection.enable")) {
            text = replaceSwears(text);
        }

        List<Pair<String, Integer>> sortedPairs = new ArrayList<>();
        Map<String, Integer> patternIndexes = new HashMap<>();

        int charIndex = 0;
        while (charIndex < text.length()) {
            if (text.startsWith("\\", charIndex)) {
                charIndex += 2;
                continue;
            }

            String matchedPattern = null;
            for (String pattern : patterns) {
                if (!isPatternEnabled("markdown-"+pattern)) {
                    continue;
                }

                if (text.startsWith(pattern, charIndex)) {
                    matchedPattern = pattern;
                    break;
                }
            }

            if (matchedPattern != null) {
                if (patternIndexes.containsKey(matchedPattern)) {
                    sortedPairs.add(new Pair<>(matchedPattern, patternIndexes.get(matchedPattern)));
                    sortedPairs.add(new Pair<>(matchedPattern, charIndex));
                    patternIndexes.remove(matchedPattern);
                } else {
                    patternIndexes.put(matchedPattern, charIndex);
                }
                charIndex += matchedPattern.length();
            } else {
                charIndex++;
            }
        }

        sortedPairs.sort(Comparator.comparingInt(Pair::right));

        TextParameters lastTextParamaters = new TextParameters("");

        if (sortedPairs.isEmpty()) {
            lastTextParamaters = new TextParameters(text);
            splitStringToWordParams(lastTextParamaters);

        } else if (sortedPairs.get(0).getValue() != 0) {
            String startText = text.substring(0, sortedPairs.get(0).getValue());
            lastTextParamaters = new TextParameters(startText);
            splitStringToWordParams(lastTextParamaters);
        }

        for (int x = 0; x < sortedPairs.size() - 1; x++) {

            var pairA = sortedPairs.get(x);
            var pairB = sortedPairs.get(x+1);

            String string = text.substring(pairA.getValue() + pairA.getKey().length(), pairB.getValue());
            TextParameters textParameters = new TextParameters(string);

            if (lastTextParamaters.contains(pairA.getKey())) {
                lastTextParamaters.remove(pairA.getKey());
            } else {
                lastTextParamaters.add(pairA.getKey());
            }

            textParameters.add(lastTextParamaters.getParameters());

            lastTextParamaters = textParameters;

            splitStringToWordParams(textParameters);
        }

        if (!sortedPairs.isEmpty()) {
            var pairA = sortedPairs.get(sortedPairs.size() - 1);

            if (pairA.getValue() < text.length() - pairA.getKey().length()) {
                String endText = text.substring(pairA.getValue() + pairA.getKey().length(), text.length() - 1);
                TextParameters textParameters = new TextParameters(endText);

                splitStringToWordParams(textParameters);
            }
        }

    }

    private String replaceSwears(String text) {
        String[] words = text.split(" ");

        StringBuilder stringBuilder = new StringBuilder();

        int lastX = 0;

        for (int x = 0; x < words.length; x++) {
            String word = words[x];

            stringBuilder.append(word);
            if (BlackListUtil.contains(stringBuilder.toString())) {
                word = locale.getFormatString("chat.swear-protection.message", sender).repeat(3);

                String textWithSwear = stringBuilder.toString();

                boolean remove = false;
                for (int y = lastX; y < x; y++) {
                    if (remove) {
                        words[y] = "";
                        continue;
                    }

                    textWithSwear = textWithSwear.substring(words[y].length());
                    if (!BlackListUtil.contains(textWithSwear)) {
                        words[y] = "";
                        remove = true;
                    }
                }

                words[x] = word;

                stringBuilder = new StringBuilder();
                lastX = x + 1;
            }
        }

        if (lastX != 0 && sender instanceof Player player) {
            ObjectUtil.playSound(player, "swear");
        }

        return Arrays.stream(words)
                .filter(word -> !word.isEmpty())
                .collect(Collectors.joining(" "));
    }

    private boolean isPatternEnabled(String patterName) {
        return config.getBoolean("chat." + patterName + ".enable")
                && (sender == null || sender.hasPermission("flectonechat.chat." + patterName));
    }

    private void splitStringToWordParams(TextParameters textParameters) {
        String text = replacePattern(textParameters.getText());

        if (text.equals(" ")) {
            messages.add(null);
            return;
        }

        String pingPrefix = locale.getString("chat.ping.prefix");
        Arrays.stream(text.split(" ")).parallel().map(word -> {

            WordParams wordParams = new WordParams();

            if (itemStack != null && word.equalsIgnoreCase("%item%")
                    && isPatternEnabled("tooltip")) {
                wordParams.setItem(true);
                wordParams.setText(itemStack.getItemMeta() != null && !itemStack.getItemMeta().getDisplayName().isEmpty()
                        ? net.md_5.bungee.api.ChatColor.ITALIC + itemStack.getItemMeta().getDisplayName()
                        : itemStack.getType().name());
                return wordParams;
            }

            if (word.startsWith(pingPrefix)
                    && isPatternEnabled("ping")) {
                String playerName = word.replaceFirst(pingPrefix, "");

                FPlayer fPlayer = FPlayerManager.getPlayerFromName(playerName);
                if (fPlayer != null && fPlayer.isOnline() && fPlayer.getPlayer() != null) {
                    Player player = fPlayer.getPlayer();

                    word = locale.getString("chat.ping.message")
                            .replace("<player>", player.getName())
                            .replace("<prefix>", pingPrefix);

                    wordParams.setClickable(clickable, player.getName());
                    wordParams.setPlayerPing(true);
                    wordParams.setText(word);

                    if(!config.getBoolean("chat.global.enable") || command.equals("globalchat")) {
                        ObjectUtil.playSound(player, "chatping");
                    }
                    return wordParams;
                }
            }

            Matcher urlMatcher = urlPattern.matcher(word);
            if (urlMatcher.find()
                    && isPatternEnabled("url")) {
                wordParams.setUrl(word.substring(urlMatcher.start(0), urlMatcher.end(0)));

                word = locale.getString("chat.url.message")
                        .replace("<url>", word);
                wordParams.setText(word);
                return wordParams;
            }

            if (sender instanceof Player player) {

                switch (word) {
                    case "%cords%" -> {
                        if (!isPatternEnabled("cords")) break;
                        wordParams.setCords(true);

                        Location location = player.getLocation();

                        word = locale.getString("chat.cords.message")
                                .replace("<world>", location.getWorld().getEnvironment().name())
                                .replace("<biome>", location.getBlock().getBiome().name())
                                .replace("<block_x>", String.valueOf(location.getBlockX()))
                                .replace("<block_y>", String.valueOf(location.getBlockY()))
                                .replace("<block_z>", String.valueOf(location.getBlockZ()));
                        wordParams.setText(word);
                        return wordParams;
                    }
                    case "%stats%" -> {
                        if (!isPatternEnabled("stats")) break;

                        wordParams.setStats(true);

                        AttributeInstance armor = player.getAttribute(Attribute.GENERIC_ARMOR);
                        AttributeInstance damage = player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);

                        word = locale.getString("chat.stats.message")
                                .replace("<hp>", String.valueOf(Math.round(player.getHealth() * 10.0)/10.0))
                                .replace("<armor>", String.valueOf(armor != null ? Math.round(armor.getValue() * 10.0)/10.0 : 0.0))
                                .replace("<exp>", player.getLevel() + ".0")
                                .replace("<food>", player.getFoodLevel() + ".0")
                                .replace("<attack>", String.valueOf(damage != null ? Math.round(damage.getValue() * 10.0)/10.0 : 0.0));

                        wordParams.setText(word);
                        return wordParams;
                    }
                }
            }

            wordParams.addParameters(textParameters.getParameters());

            if (textParameters.contains("||")) {
                wordParams.setHide(true);

                wordParams.setHideMessage(locale.getString("chat.hide.message")
                        .repeat(ChatColor.stripColor(word).length()));
            }
            wordParams.setText(word);
            return wordParams;
        }).forEachOrdered(wordParams -> {
            messages.add(wordParams);
            WordParams wordParams1 = new WordParams();
            wordParams1.setText(" ");
            wordParams1.addParameters(textParameters.getParameters());

            messages.add(wordParams1);
        });

        if (!text.endsWith(" ")) {
            messages.remove(messages.size() - 1);
        }
    }


    public static void loadPatterns() {
        patternMap.clear();

        config.getStringList("chat.patterns")
                .forEach(patternString -> {
                    String[] patternComponents = patternString.split(" , ");
                    if (patternComponents.length < 2) return;

                    patternMap.put(patternComponents[0], patternComponents[1]);
                });
    }

    @NotNull
    public String getMessage(String color) {
        StringBuilder stringBuilder = new StringBuilder();
        for (WordParams wordParams : messages) {
            if (wordParams == null) {
                stringBuilder.append(" ");
                continue;
            }

            String word = wordParams.getText();
            if (wordParams.isHide()) {
                word = wordParams.getHideMessage();
                assert word != null;
            }

            if (wordParams.isEdited()) {
                word = ObjectUtil.formatString(word, null);
                word = ChatColor.stripColor(word);
            }

            if (sender.hasPermission("flectonechat.placeholders")) {
                word = ObjectUtil.formatPAPI(word, sender, sender, true);
            }

            if (sender.hasPermission("flectonechat.formatting")) {
                word = ObjectUtil.formatString(word, sender, sender, true);
            }

            color = ChatColor.getLastColors(color) + ChatColor.getLastColors(word);

            String newFormatting = wordParams.getFormatting();
            if (!newFormatting.isEmpty()) {
                word = color + newFormatting + word + ChatColor.RESET + color;
            }

            stringBuilder.append(word);
        }

        return stringBuilder.toString();
    }

    @NotNull
    public BaseComponent[] buildFormat(@NotNull String format, @NotNull CommandSender recipient, @NotNull CommandSender sender) {
        ComponentBuilder componentBuilder = new ComponentBuilder();

        String[] formats = ObjectUtil.formatString(format, recipient, sender).split("<message>");

        FComponent fComponent = this.clickable && sender instanceof Player
                ? new FPlayerComponent(recipient, sender, formats[0])
                : new FComponent(formats[0]);

        componentBuilder.append(fComponent.get());

        String color = ChatColor.getLastColors(formats[0]);

        componentBuilder.append(buildMessage(color, recipient, sender), ComponentBuilder.FormatRetention.NONE);

        if (formats.length > 1)
            componentBuilder.append(FComponent.fromLegacyText(color + formats[1]), ComponentBuilder.FormatRetention.NONE);

        return componentBuilder.create();
    }

    @NotNull
    public BaseComponent[] buildMessage(@NotNull String lastColor, @NotNull CommandSender recipient, @NotNull CommandSender sender) {
        ComponentBuilder componentBuilder = new ComponentBuilder();

        for(WordParams wordParams : messages) {

            if (wordParams == null) {
                componentBuilder.append(" ");
                continue;
            }

            String word = lastColor + wordParams.getFormatting() + wordParams.getText();
            if (wordParams.isEdited() && !wordParams.isHide()) {
                word = ObjectUtil.formatString(word, recipient, sender);
            }

            if (sender.hasPermission("flectonechat.placeholders")) {
                word = ObjectUtil.formatPAPI(word, sender, sender, true);
            }

            if (sender.hasPermission("flectonechat.formatting") && !wordParams.isEdited()) {
                wordParams.setFormatted(true);

                String newWord = ObjectUtil.formatString(lastColor + wordParams.getText(), recipient, sender, true);
                lastColor = ChatColor.getLastColors(newWord);

                word = lastColor + wordParams.getFormatting() + ChatColor.stripColor(newWord);
            }

            FComponent wordComponent = new FComponent(word);

            if (wordParams.isItem()) {
                componentBuilder.append(createItemComponent(itemStack, lastColor, recipient, sender));
                continue;
            }

            if (wordParams.isClickable()) {
                wordComponent = new FPlayerComponent(recipient, FPlayerManager.getPlayerFromName(wordParams.getPlayerPingName()).getPlayer(), word);
            } else if (wordParams.isUrl()) {
                wordComponent = new FURLComponent(recipient, sender, word, wordParams.getUrl());
            } else if (wordParams.isHide()) {
                wordComponent = new FComponent(ObjectUtil.formatString(wordParams.getHideMessage(), recipient, sender));
                wordComponent.addHoverText(word);
            }

            componentBuilder.append(wordComponent.get(), ComponentBuilder.FormatRetention.NONE);
        }

        return componentBuilder.create();
    }

    @NotNull
    private BaseComponent[] createItemComponent(@NotNull ItemStack itemStack, @NotNull String lastColor, @NotNull CommandSender recipient, @NotNull CommandSender sender) {
        ComponentBuilder itemBuilder = new ComponentBuilder();

        String[] componentsStrings = locale.getFormatString("chat.tooltip.message", recipient, sender).split("<tooltip>");
        BaseComponent[] color = FComponent.fromLegacyText(lastColor);

        return itemBuilder
                .append(color)
                .append(FComponent.fromLegacyText(componentsStrings[0]))
                .append(new FLocaleComponent(itemStack).get())
                .append(FComponent.fromLegacyText(componentsStrings[1]))
                .append(color)
                .create();
    }

    @NotNull
    public BaseComponent[] create() {
        return componentBuilder.create();
    }

    @NotNull
    private String replacePattern(@NotNull String word) {
        String wordLowerCased = word.toLowerCase();

        Map.Entry<String, String> pattern = patternMap.entrySet()
                .parallelStream()
                .filter(entry -> wordLowerCased.contains(entry.getKey().toLowerCase()))
                .findFirst()
                .orElse(null);

        if (pattern == null) return word;

        return wordLowerCased.replace(pattern.getKey().toLowerCase(), pattern.getValue());
    }
}
