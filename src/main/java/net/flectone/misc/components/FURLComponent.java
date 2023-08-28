package net.flectone.misc.components;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.flectone.managers.FileManager.locale;

public class FURLComponent extends FComponent{

    public FURLComponent(@Nullable CommandSender recipient, @Nullable CommandSender sender, @NotNull String text, @NotNull String url) {
        super(text);

        String showText = locale.getFormatString("chat.url.hover-message", recipient, sender);
        addOpenURL(url);
        addHoverText(showText);
    }
}
