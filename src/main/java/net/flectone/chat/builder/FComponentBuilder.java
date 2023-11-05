package net.flectone.chat.builder;

import net.flectone.chat.component.FComponent;
import net.flectone.chat.util.MessageUtil;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class FComponentBuilder {

    private final String message;
    private final Map<String, Replace> replacements = new HashMap<>();

    private final ComponentBuilder componentBuilder = new ComponentBuilder();

    public FComponentBuilder(String message) {
        this.message = message;
    }

    public void replace(@NotNull String string, Replace replace) {
        replacements.put(string, replace);
    }

    public BaseComponent[] build(@Nullable Player sender, @Nullable Player recipient) {
        List<String> strings = splitLine(message, replacements.keySet());

        String mainColor = "";
        for (String placeholder : strings) {

            Replace replace = replacements.get(placeholder);

            if (replace == null) {

                placeholder = MessageUtil.formatAll(sender, recipient, placeholder);

                componentBuilder.append(FComponent.fromLegacyText(mainColor + placeholder), ComponentBuilder.FormatRetention.NONE);

                mainColor = ChatColor.getLastColors(mainColor + placeholder);

                continue;
            }

            replace.action(componentBuilder, mainColor);
        }

        return componentBuilder.create();
    }


    public List<String> splitLine(@NotNull String line, @NotNull Set<String> placeholders) {
        ArrayList<String> split = new ArrayList<>(List.of(line));

        for (String placeholder : placeholders) {
            split = (ArrayList<String>) split.stream().flatMap(part -> {
                String[] sp = part.split("((?=" + placeholder + ")|(?<=" + placeholder + "))");
                return Arrays.stream(sp);
            }).collect(Collectors.toList());
        }

        return split;
    }

    @FunctionalInterface
    public interface Replace {
        void action(ComponentBuilder componentBuilder, String color);
    }
}
