package net.flectone.chat.module.playerMessage.formatting;

import net.flectone.chat.FlectoneChat;
import net.flectone.chat.manager.FPlayerManager;
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
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.flectone.chat.manager.FileManager.config;
import static net.flectone.chat.manager.FileManager.locale;

public class FormattingModule extends FModule {

    private final static List<String> patterns = List.of("||", "**", "__", "##", "??", "~~");
    private final static HashMap<String, HashMap<String, String>> FORMATTING_MAP = new HashMap<>();

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

        if (!hasNoPermission(player)) {
            List<String> formattingList = config.getCustomList(player, this + ".list");

            for (String formatting : formattingList) {
                if (!config.getVaultBoolean(player, this + ".list." + formatting + ".enable")) continue;
                if (hasNoPermission(player, formatting)) continue;

                groupFormattingMap.put(formatting, config.getVaultString(player, this + ".list." + formatting + ".trigger"));
            }
        }

        FORMATTING_MAP.put(vaultGroup, groupFormattingMap);

        return groupFormattingMap;
    }

    public void replace(@Nullable Player player, @NotNull String message, @NotNull String command,
                               @NotNull List<WordParams> messages, @Nullable ItemStack itemStack,
                               boolean isEnabled) {

        HashMap<String, String> formattingMap = load(player);

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

        sortedPairs.sort(Comparator.comparingInt(Pair::right));

        TextParameters lastTextParamaters = new TextParameters("");

        if (sortedPairs.isEmpty()) {
            lastTextParamaters = new TextParameters(message);
            splitStringToWordParams(player, command, lastTextParamaters, messages, itemStack, formattingMap, isEnabled);

        } else if (sortedPairs.get(0).getValue() != 0) {
            String startText = message.substring(0, sortedPairs.get(0).getValue());
            lastTextParamaters = new TextParameters(startText);
            splitStringToWordParams(player, command, lastTextParamaters, messages, itemStack, formattingMap, isEnabled);
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

            splitStringToWordParams(player, command, textParameters, messages, itemStack, formattingMap, isEnabled);
        }

        if (!sortedPairs.isEmpty()) {
            var pairA = sortedPairs.get(sortedPairs.size() - 1);

            if (pairA.getValue() < message.length() - pairA.getKey().length()) {
                String endText = message.substring(pairA.getValue() + pairA.getKey().length());
                TextParameters textParameters = new TextParameters(endText);

                splitStringToWordParams(player, command, textParameters, messages, itemStack, formattingMap, isEnabled);
            }
        }

    }

    private void splitStringToWordParams(@Nullable Player sender, @NotNull String command,
                                         @NotNull TextParameters textParameters, @NotNull List<WordParams> messages,
                                         @Nullable ItemStack itemStack, @NotNull HashMap<String, String> formattingMap,
                                         boolean isEnabled) {
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

            if (itemStack != null && word.equalsIgnoreCase("%item%")
                    && formattingMap.containsKey("item")) {
                wordParams.setItem(true);
                wordParams.setText(itemStack.getItemMeta() != null && !itemStack.getItemMeta().getDisplayName().isEmpty()
                        ? net.md_5.bungee.api.ChatColor.ITALIC + itemStack.getItemMeta().getDisplayName()
                        : itemStack.getType().name());
                return wordParams;
            }

            if (word.startsWith(mentionPrefix)
                    && formattingMap.containsKey("mention")) {
                String playerName = word.replaceFirst(mentionPrefix, "");

                FPlayer fPlayer = FPlayerManager.get(playerName);
                if (fPlayer != null && fPlayer.getPlayer() != null) {
                    Player player = fPlayer.getPlayer();

                    word = config.getVaultString(sender, this + ".list.mention.format")
                            .replace("<message>", player.getName());

                    wordParams.setClickable(true, player.getName());
                    wordParams.setPlayerPing(true);
                    wordParams.setText(word);

                    //need rewrite
                    if(!config.getBoolean("chat.global.enable") || command.equals("globalchat")) {
                        FModule fModule1 = FlectoneChat.getModuleManager().get(SoundsModule.class);
                        if (fModule1 instanceof SoundsModule soundsModule) {
                            soundsModule.play(new FSound(player, "chatping"));
                        }
                    }

                    return wordParams;
                }
            }

            if (formattingMap.containsKey("url")) {
                Matcher urlMatcher = Pattern.compile(formattingMap.get("url")).matcher(word);
                if (urlMatcher.find()) {
                    wordParams.setUrl(word.substring(urlMatcher.start(0), urlMatcher.end(0)));

                    word = config.getVaultString(sender, this + ".list.url.format")
                            .replace("<message>", word);
                    wordParams.setText(word);
                    return wordParams;
                }
            }

            if (sender != null) {

                switch (word) {
                    case "%cords%" -> {
                        if (!formattingMap.containsKey("cords")) break;
                        wordParams.setCords(true);

                        Location location = sender.getLocation();

                        word = locale.getVaultString(sender, this + ".list.cords.message")
                                .replace("<world>", location.getWorld().getEnvironment().name())
                                .replace("<biome>", location.getBlock().getBiome().name())
                                .replace("<block_x>", String.valueOf(location.getBlockX()))
                                .replace("<block_y>", String.valueOf(location.getBlockY()))
                                .replace("<block_z>", String.valueOf(location.getBlockZ()));
                        wordParams.setText(word);

                        return wordParams;
                    }
                    case "%stats%" -> {
                        if (!formattingMap.containsKey("stats")) break;

                        wordParams.setStats(true);

                        AttributeInstance armor = sender.getAttribute(Attribute.GENERIC_ARMOR);
                        AttributeInstance damage = sender.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);

                        word = locale.getVaultString(sender, this + ".list.stats.message")
                                .replace("<hp>", String.valueOf(Math.round(sender.getHealth() * 10.0)/10.0))
                                .replace("<armor>", String.valueOf(armor != null ? Math.round(armor.getValue() * 10.0)/10.0 : 0.0))
                                .replace("<exp>", sender.getLevel() + ".0")
                                .replace("<food>", sender.getFoodLevel() + ".0")
                                .replace("<attack>", String.valueOf(damage != null ? Math.round(damage.getValue() * 10.0)/10.0 : 0.0));

                        wordParams.setText(word);
                        return wordParams;
                    }
                }
            }

            wordParams.addParameters(textParameters.getParameters());

            if (textParameters.contains("||")) {
                wordParams.setHide(true);

                wordParams.setHideMessage(config.getVaultString(sender, this + ".list.markdown-||.symbol")
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

}
