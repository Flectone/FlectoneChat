package net.flectone.chat.util;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtil {

    private static final Map<Material, String> DYE_HEX_MAP = new HashMap<>();

    static {
        DYE_HEX_MAP.put(Material.WHITE_DYE, "#ffffff");
        DYE_HEX_MAP.put(Material.GRAY_DYE, "#999999");
        DYE_HEX_MAP.put(Material.LIGHT_GRAY_DYE, "#cccccc");
        DYE_HEX_MAP.put(Material.BLACK_DYE, "#333333");
        DYE_HEX_MAP.put(Material.RED_DYE, "#ff3333");
        DYE_HEX_MAP.put(Material.ORANGE_DYE, "#ff9900");
        DYE_HEX_MAP.put(Material.YELLOW_DYE, "#ffff00");
        DYE_HEX_MAP.put(Material.LIME_DYE, "#33ff33");
        DYE_HEX_MAP.put(Material.GREEN_DYE, "#009900");
        DYE_HEX_MAP.put(Material.LIGHT_BLUE_DYE, "#99ccff");
        DYE_HEX_MAP.put(Material.CYAN_DYE, "#33cccc");
        DYE_HEX_MAP.put(Material.BLUE_DYE, "#3366ff");
        DYE_HEX_MAP.put(Material.PURPLE_DYE, "#9900cc");
        DYE_HEX_MAP.put(Material.MAGENTA_DYE, "#ff66ff");
        DYE_HEX_MAP.put(Material.PINK_DYE, "#ff99cc");
        DYE_HEX_MAP.put(Material.BROWN_DYE, "#cc6600");
    }

    @Nullable
    public static String dyeToHex(@NotNull ItemStack itemStack) {
        return DYE_HEX_MAP.get(itemStack.getType());
    }

    @Nullable
    public static ItemStack hexToDye(@NotNull String sign) {
        return DYE_HEX_MAP.entrySet().stream()
                .filter(entry -> sign.toLowerCase().contains(translateHexToColor(entry.getValue()).toLowerCase()))
                .findFirst()
                .map(materialStringEntry -> new ItemStack(materialStringEntry.getKey()))
                .orElse(null);

    }

    private static final Pattern HEX_PATTERN = Pattern.compile("&?#[a-fA-F0-9]{6}");

    @NotNull
    public static String translateHexToColor(@NotNull String message) {
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            String hexCode = matcher.group();
            String replaceSharp = hexCode.replaceFirst("&", "").replace('#', 'x');

            StringBuilder builder = new StringBuilder();
            for (char c : replaceSharp.toCharArray()) {
                builder.append("&").append(c);
            }
            matcher.appendReplacement(sb, builder.toString());
        }

        matcher.appendTail(sb);

        return ChatColor.translateAlternateColorCodes('&', sb.toString());
    }

}
