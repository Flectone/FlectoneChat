package net.flectone.messages;

import net.flectone.managers.FPlayerManager;
import net.flectone.misc.components.FComponent;
import net.flectone.misc.components.FLocaleComponent;
import net.flectone.misc.components.FPlayerComponent;
import net.flectone.misc.components.FURLComponent;
import net.flectone.misc.entity.FPlayer;
import net.flectone.utils.ObjectUtil;
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
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

    private final LinkedHashMap<Integer, WordParams> messageHashMap = new LinkedHashMap<>();
    private final ComponentBuilder componentBuilder = new ComponentBuilder();
    private final ItemStack itemStack;
    private final String command;

    private final boolean clickable;

    public MessageBuilder(@NotNull String command, @NotNull String text, @Nullable CommandSender sender, @Nullable ItemStack itemStack, boolean clickable) {
        this.itemStack = itemStack;
        this.command = command;
        this.clickable = clickable;

        String pingPrefix = locale.getString("chat.ping.prefix");

        AtomicInteger index = new AtomicInteger();

        Arrays.stream(text.split(" ")).parallel().map(word -> {
            WordParams wordParams = new WordParams();

            word = replacePattern(word);

            if (itemStack != null && word.equalsIgnoreCase("%item%") && config.getBoolean("chat.tooltip.enable")) {
                wordParams.setItem(true);
                wordParams.setText(itemStack.getItemMeta() != null && !itemStack.getItemMeta().getDisplayName().isEmpty()
                        ? net.md_5.bungee.api.ChatColor.ITALIC + itemStack.getItemMeta().getDisplayName()
                        : itemStack.getType().name());
                return wordParams;
            }

            if (word.startsWith(pingPrefix) && config.getBoolean("chat.ping.enable")) {
                String playerName = word.replaceFirst(pingPrefix, "");

                FPlayer fPlayer = FPlayerManager.getPlayerFromName(playerName);
                if (fPlayer != null && fPlayer.isOnline() && fPlayer.getPlayer() != null) {
                    Player player = fPlayer.getPlayer();

                    word = locale.getString("chat.ping.message")
                            .replace("<player>", player.getName())
                            .replace("<prefix>", pingPrefix);

                    wordParams.setClickable(clickable, player.getName());
                    wordParams.setPlayerPing(true);

                    if(!config.getBoolean("chat.global.enable") || command.equals("globalchat")) {
                        ObjectUtil.playSound(player, "chatping");
                    }
                }
            }

            if (word.startsWith("||") && word.endsWith("||") && !word.replace("||", "").isEmpty() && config.getBoolean("chat.hide.enable")) {
                word = word.replace("||", "");

                wordParams.setHideMessage(word);
                wordParams.setHide(true);

                word = locale.getString("chat.hide.message")
                        .repeat(word.length());
            }

            Matcher urlMatcher = urlPattern.matcher(word);
            if (urlMatcher.find() && config.getBoolean("chat.url.enable")) {
                wordParams.setUrl(word.substring(urlMatcher.start(0), urlMatcher.end(0)));

                word = locale.getString("chat.url.message")
                        .replace("<url>", word);
            }

            if (sender instanceof Player player) {

                switch (word) {
                    case "%cords%" -> {
                        if (!config.getBoolean("chat.cords.enable")) break;
                        wordParams.setCords(true);

                        Location location = player.getLocation();

                        word = locale.getString("chat.cords.message")
                                .replace("<world>", location.getWorld().getEnvironment().name())
                                .replace("<biome>", location.getBlock().getBiome().name())
                                .replace("<block_x>", String.valueOf(location.getBlockX()))
                                .replace("<block_y>", String.valueOf(location.getBlockY()))
                                .replace("<block_z>", String.valueOf(location.getBlockZ()));
                    }
                    case "%stats%" -> {
                        if (!config.getBoolean("chat.stats.enable")) break;

                        wordParams.setStats(true);

                        AttributeInstance armor = player.getAttribute(Attribute.GENERIC_ARMOR);
                        AttributeInstance damage = player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);

                        word = locale.getString("chat.stats.message")
                                .replace("<hp>", String.valueOf(player.getHealth()))
                                .replace("<armor>", String.valueOf(armor != null ? armor.getValue() : 0))
                                .replace("<exp>", player.getLevel() + ".0")
                                .replace("<food>", player.getFoodLevel() + ".0")
                                .replace("<attack>", String.valueOf(damage != null ? damage.getValue() : 0));
                    }
                }
            }

            wordParams.setText(word);
            return wordParams;

        })
        .forEachOrdered(wordParams -> messageHashMap.put(index.getAndIncrement(), wordParams));
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

    @NotNull
    public BaseComponent[] build(@NotNull String format, @NotNull CommandSender recipient, @NotNull CommandSender sender) {
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
    private BaseComponent[] buildMessage(@NotNull String lastColor, @NotNull CommandSender recipient, @NotNull CommandSender sender) {
        ComponentBuilder componentBuilder = new ComponentBuilder();

        for (Map.Entry<Integer, WordParams> entry : messageHashMap.entrySet()) {
            String word = entry.getValue().getText();
            WordParams wordParams = entry.getValue();

            if (sender.hasPermission("flectonechat.formatting") && !wordParams.isEdited()) {
                String color1 = ChatColor.getLastColors(word);
                word = ObjectUtil.formatString(true, word, recipient, sender);
                String color2 = ChatColor.getLastColors(word);

                wordParams.setFormatted(!color1.equals(color2));
                wordParams.setText(word);
            }

            FComponent wordComponent = new FComponent(lastColor + word);

            if (!wordParams.isEdited() || wordParams.isFormatted())
                lastColor = ChatColor.getLastColors(lastColor + word);

            if (wordParams.isItem()) {
                componentBuilder.append(createItemComponent(itemStack, lastColor, recipient, sender));
                continue;
            }

            if (wordParams.isClickable()) {
                wordComponent = new FPlayerComponent(recipient, FPlayerManager.getPlayerFromName(wordParams.getPlayerPingName()).getPlayer(), ObjectUtil.formatString(lastColor + word, recipient, sender));
            }

            if (wordParams.isUrl()) {
                wordComponent = new FURLComponent(recipient, sender, ObjectUtil.formatString(lastColor + word, recipient, sender), wordParams.getUrl());
            }

            if (wordParams.isHide()) {
                wordComponent = new FComponent(ObjectUtil.formatString(lastColor + word, recipient, sender));
                wordComponent.addHoverText(lastColor + wordParams.getHideMessage());
            }

            if (wordParams.isCords() || wordParams.isStats()) {
                wordComponent = new FComponent(ObjectUtil.formatString(lastColor + word, recipient, sender));
            }

            componentBuilder
                    .append(wordComponent.get(), ComponentBuilder.FormatRetention.NONE)
                    .append(" ");
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
                .append(" ")
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
