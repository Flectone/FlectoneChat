package net.flectone.chat.component;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.flectone.chat.manager.FileManager.config;

public class FURLComponent extends FComponent{

    public FURLComponent(@Nullable Player sender, @NotNull Player recipient, @NotNull String text, @NotNull String url) {
        super(text);

        String showText = config.getVaultString(sender, "formatting.list.url.format");
        addOpenURL(url);
        addHoverText(showText);
    }
}
