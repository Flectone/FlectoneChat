package net.flectone.chat.module.playerMessage.formatting;

import net.flectone.chat.FlectoneChat;
import net.flectone.chat.component.FImageComponent;
import net.flectone.chat.model.message.TextParameters;
import net.flectone.chat.model.message.WordParams;
import net.flectone.chat.model.player.FPlayer;
import net.flectone.chat.model.sound.FSound;
import net.flectone.chat.module.FModule;
import net.flectone.chat.module.sounds.SoundsModule;
import net.flectone.chat.util.Pair;
import net.flectone.chat.util.PlayerUtil;
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

public class FormattingModule extends FModule {

    private final static Pattern MARKDOWN_URL_PATTERN = Pattern.compile("\\[(?<text>[^\\]]*)\\]\\((?<link>[^\\)]*)\\)");
    private final static Pattern IMAGE_URL_PATTERN = Pattern.compile("^(?:https?://)?([^/]+)");
    private final static List<String> patterns = List.of("||", "**", "__", "##", "??", "~~");
    private final HashMap<String, HashMap<String, String>> FORMATTING_MAP = new HashMap<>();

    public FormattingModule(FModule module, String name) {
        super(module, name);
        init();
    }

    @Override
    public void init() {
        if (!isEnabled()) return;
        register();
    }

    @NotNull
    public HashMap<String, String> load(@Nullable Player player) {
        HashMap<String, String> groupFormattingMap = new HashMap<>();

        String vaultGroup = PlayerUtil.getPrimaryGroup(player);
        if (FORMATTING_MAP.containsKey(vaultGroup)) return FORMATTING_MAP.get(vaultGroup);

        List<String> formattingList = config.getCustomList(player, this + ".list");

        for (String formatting : formattingList) {
            if (!config.getVaultBoolean(player, this + ".list." + formatting + ".enable")) continue;
            if (hasNoPermission(player, formatting)) continue;

            groupFormattingMap.put(formatting, config.getVaultString(player, this + ".list." + formatting + ".trigger"));
        }

        FORMATTING_MAP.put(vaultGroup, groupFormattingMap);

        return groupFormattingMap;
    }

    public void replace(@Nullable Player player,
                        @NotNull String message,
                        @NotNull String command,
                        @NotNull List<WordParams> messages,
                        @Nullable ItemStack itemStack,
                        boolean mentionEnabled,
                        boolean isEnabled,
                        boolean isAsync) {

        HashMap<String, String> formattingMap = load(player);

        List<Pair<String, Integer>> sortedPairs = getPairs(message, formattingMap);

        sortedPairs.sort(Comparator.comparingInt(Pair::right));

        TextParameters lastTextParamaters = new TextParameters("");

        if (sortedPairs.isEmpty()) {
            lastTextParamaters = new TextParameters(message);
            splitStringToWordParams(player, command, lastTextParamaters, messages, itemStack, formattingMap, mentionEnabled, isEnabled, isAsync);

        } else if (sortedPairs.get(0).getValue() != 0) {
            String startText = message.substring(0, sortedPairs.get(0).getValue());
            lastTextParamaters = new TextParameters(startText);
            splitStringToWordParams(player, command, lastTextParamaters, messages, itemStack, formattingMap, mentionEnabled, isEnabled, isAsync);
        }

        for (int x = 0; x < sortedPairs.size() - 1; x++) {

            var pairA = sortedPairs.get(x);
            var pairB = sortedPairs.get(x+1);

            String string = message.substring(pairA.getValue() + pairA.getKey().length(), pairB.getValue());
            TextParameters textParameters = new TextParameters(string);

            if (lastTextParamaters.contains(pairA.getKey())) {
                lastTextParamaters.remove(pairA.getKey());
            } else {
                lastTextParamaters.add(pairA.getKey());
            }

            textParameters.add(lastTextParamaters.getParameters());

            lastTextParamaters = textParameters;

            splitStringToWordParams(player, command, textParameters, messages, itemStack, formattingMap, mentionEnabled, isEnabled, isAsync);
        }

        if (!sortedPairs.isEmpty()) {
            var pairA = sortedPairs.get(sortedPairs.size() - 1);

            if (pairA.getValue() < message.length() - pairA.getKey().length()) {
                String endText = message.substring(pairA.getValue() + pairA.getKey().length());
                TextParameters textParameters = new TextParameters(endText);

                splitStringToWordParams(player, command, textParameters, messages, itemStack, formattingMap, mentionEnabled, isEnabled, isAsync);
            }
        }

    }

    @NotNull
    private List<Pair<String, Integer>> getPairs(@NotNull String message, HashMap<String, String> formattingMap) {
        List<Pair<String, Integer>> sortedPairs = new ArrayList<>();
        Map<String, Integer> patternIndexes = new HashMap<>();

        int charIndex = 0;
        while (charIndex < message.length()) {
            if (message.startsWith("\\", charIndex)) {
                charIndex += 2;
                continue;
            }

            String matchedPattern = null;
            for (String pattern : patterns) {
                if (!formattingMap.containsValue(pattern)) continue;

                if (message.startsWith(pattern, charIndex)) {
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
        return sortedPairs;
    }

    private void splitStringToWordParams(@Nullable Player sender,
                                         @NotNull String command,
                                         @NotNull TextParameters textParameters,
                                         @NotNull List<WordParams> messages,
                                         @Nullable ItemStack itemStack,
                                         @NotNull HashMap<String, String> formattingMap,
                                         boolean mentionEnabled,
                                         boolean isEnabled,
                                         boolean isAsync) {
        String text = textParameters.getText();

        if (text.equals(" ")) {
            messages.add(null);
            return;
        }

        String mentionPrefix = config.getVaultString(sender, this + ".list.mention.trigger");
        Arrays.stream(text.split(" ")).parallel().map(word -> {

            WordParams wordParams = new WordParams();

            if (!isEnabled) {
                wordParams.setText(word);
                return wordParams;
            }

            if (itemStack != null
                    && formattingMap.containsKey("item")
                    && word.equalsIgnoreCase("%item%")) {

                return processItem(wordParams, itemStack);
            }

            if (word.startsWith(mentionPrefix)
                    && formattingMap.containsKey("mention")
                    && mentionEnabled) {
                String playerName = word.replaceFirst(mentionPrefix, "");

                FPlayer fPlayer = playerManager.get(playerName);
                if (fPlayer != null && fPlayer.getPlayer() != null) {
                    return processMention(wordParams, sender, fPlayer.getPlayer());
                }
            }

            if (formattingMap.containsKey("url")) {

                if (config.getVaultBoolean(sender, this + ".list.url.markdown-support")
                        && !hasNoPermission(sender, "markdown-url")) {

                    Matcher urlMatcher = MARKDOWN_URL_PATTERN.matcher(word);
                    if (urlMatcher.find()) {
                        return processMarkdownUrl(wordParams, sender, urlMatcher);
                    }
                }

                if (isAsync
                        && isUrlAllowed(sender, word)
                        && config.getVaultBoolean(sender, this + ".list.url.image.enable")
                        && !hasNoPermission(sender, "url.image")) {

                    FImageComponent fImageComponent = new FImageComponent(word);
                    if (fImageComponent.isCorrect()) {
                        return processImage(wordParams, sender, fImageComponent);
                    }
                }

                Matcher urlMatcher = Pattern.compile(formattingMap.get("url")).matcher(word);
                if (urlMatcher.find()) {
                    return processUrl(wordParams, sender, word, urlMatcher);
                }
            }

            if (sender != null) {

                String valuePing = formattingMap.get("ping");
                if (valuePing != null && valuePing.equals(word)) {
                    return processPing(wordParams, sender);
                }

                String valueCords = formattingMap.get("cords");
                if (valueCords != null && valueCords.equals(word)) {
                    return processCords(wordParams, sender);
                }

                String valueStats = formattingMap.get("stats");
                if (valueStats != null && valueStats.equals(word)) {
                    return processStats(wordParams, sender);
                }
            }

            wordParams.addParameters(textParameters.getParameters());

            if (textParameters.contains("||")) {
                processMarkdownHide(wordParams, sender, word);
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

    public WordParams processItem(@NotNull WordParams wordParams, @NotNull ItemStack itemStack) {
        wordParams.setItem(true);
        wordParams.setText(itemStack.getItemMeta() != null && !itemStack.getItemMeta().getDisplayName().isEmpty()
                ? net.md_5.bungee.api.ChatColor.ITALIC + itemStack.getItemMeta().getDisplayName()
                : itemStack.getType().name());
        return wordParams;
    }

    public WordParams processMention(@NotNull WordParams wordParams, @Nullable CommandSender sender, @NotNull Player player) {
        String word = config.getVaultString(sender, this + ".list.mention.format")
                .replace("<message>", player.getName());

        wordParams.setClickable(true);
        wordParams.setPlayerPingName(player.getName());
        wordParams.setPlayerPing(true);
        wordParams.setText(word);

        FModule fModule1 = moduleManager.get(SoundsModule.class);
        if (fModule1 instanceof SoundsModule soundsModule) {
            soundsModule.play(new FSound(player, "mention"));
        }

        return wordParams;
    }

    public WordParams processMarkdownUrl(@NotNull WordParams wordParams, @Nullable CommandSender sender, @NotNull Matcher urlMatcher) {
        wordParams.setUrlText(urlMatcher.group(2));
        wordParams.setUrl(true);

        String word = config.getVaultString(sender, this + ".list.url.format")
                .replace("<message>", urlMatcher.group(1));
        wordParams.setText(word);
        return wordParams;
    }

    public WordParams processImage(@NotNull WordParams wordParams, @Nullable CommandSender sender, @NotNull FImageComponent fImageComponent) {
        String word = config.getVaultString(sender, this + ".list.url.image.format")
                .replace("<image>", fImageComponent.getText());
        wordParams.setText(word);
        wordParams.setImageComponent(fImageComponent);
        wordParams.setImage(true);
        return wordParams;
    }

    public WordParams processUrl(@NotNull WordParams wordParams, @Nullable CommandSender sender, @NotNull String word, @NotNull Matcher urlMatcher) {
        wordParams.setUrlText(word.substring(urlMatcher.start(0), urlMatcher.end(0)));
        wordParams.setUrl(true);

        word = config.getVaultString(sender, this + ".list.url.format")
                .replace("<message>", word);
        wordParams.setText(word);
        return wordParams;
    }

    public WordParams processPing(@NotNull WordParams wordParams, @NotNull Player sender) {
        wordParams.setPing(true);

        int ping = sender.getPing();
        int badPing = config.getVaultInt(sender, this + ".list.ping.bad.count");
        int mediumPing = config.getVaultInt(sender, this + ".list.ping.medium.count");

        String pingColor;
        if (ping > badPing) {
            pingColor = config.getVaultString(sender, this + ".list.ping.bad.color");
        } else if (ping > mediumPing) {
            pingColor = config.getVaultString(sender, this + ".list.ping.medium.color");
        } else {
            pingColor = config.getVaultString(sender, this + ".list.ping.good.color");
        }

        String word = locale.getVaultString(sender, this + ".list.ping.message")
                .replace("<player>", sender.getName())
                .replace("<ping>", pingColor + ping);
        wordParams.setText(word);

        return wordParams;
    }

    public WordParams processCords(@NotNull WordParams wordParams, @NotNull Player sender) {
        wordParams.setCords(true);

        Location location = sender.getLocation();

        String word = locale.getVaultString(sender, this + ".list.cords.message")
                .replace("<world>", sender.getWorld().getEnvironment().name())
                .replace("<biome>", location.getBlock().getBiome().name())
                .replace("<block_x>", String.valueOf(location.getBlockX()))
                .replace("<block_y>", String.valueOf(location.getBlockY()))
                .replace("<block_z>", String.valueOf(location.getBlockZ()));
        wordParams.setText(word);

        return wordParams;
    }

    public WordParams processStats(@NotNull WordParams wordParams, @NotNull Player sender) {
        wordParams.setStats(true);

        AttributeInstance armor = sender.getAttribute(Attribute.GENERIC_ARMOR);
        AttributeInstance damage = sender.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);

        String word = locale.getVaultString(sender, this + ".list.stats.message")
                .replace("<hp>", String.valueOf(Math.round(sender.getHealth() * 10.0)/10.0))
                .replace("<armor>", String.valueOf(armor != null ? Math.round(armor.getValue() * 10.0)/10.0 : 0.0))
                .replace("<exp>", sender.getLevel() + ".0")
                .replace("<food>", sender.getFoodLevel() + ".0")
                .replace("<attack>", String.valueOf(damage != null ? Math.round(damage.getValue() * 10.0)/10.0 : 0.0));

        wordParams.setText(word);
        return wordParams;
    }

    public WordParams processMarkdownHide(@NotNull WordParams wordParams, @Nullable CommandSender sender, @NotNull String word) {
        wordParams.setHide(true);

        wordParams.setHideMessage(config.getVaultString(sender, this + ".list.markdown-||.symbol")
                .repeat(ChatColor.stripColor(word).length()));
        return wordParams;
    }

    public boolean isUrlAllowed(@Nullable CommandSender player, @NotNull String urlString) {
        Matcher matcher = IMAGE_URL_PATTERN.matcher(urlString);
        if (matcher.find()) {
            String host = matcher.group(1).toLowerCase();
            return FlectoneChat.getPlugin().getFileManager()
                    .getConfig()
                    .getVaultStringList(player, this + ".list.url.image.whitelist-site")
                    .contains(host);
        }

        return false;
    }
}
