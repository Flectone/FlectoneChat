package net.flectone.chat.component;

import net.flectone.chat.util.MessageUtil;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.flectone.chat.manager.FileManager.config;
import static net.flectone.chat.manager.FileManager.locale;

public class FURLComponent extends FComponent{

    public FURLComponent(@Nullable Player sender, @NotNull Player recipient, @NotNull String text, @NotNull String url) {
        super(text);

        if (config.getVaultBoolean(sender, "player-message.formatting.list.url.clickable")) {
            addOpenURL(url);
        }

        if (config.getVaultBoolean(sender, "player-message.formatting.list.url.hover.enable")) {
            String showText = locale.getVaultString(sender, "player-message.formatting.list.url.hover.message");
            showText = MessageUtil.formatAll(sender, recipient, showText);
            addHoverText(showText);
        }
    }
}
