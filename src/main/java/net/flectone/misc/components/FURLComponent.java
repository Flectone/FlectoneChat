package net.flectone.misc.components;

import org.bukkit.command.CommandSender;

import static net.flectone.managers.FileManager.locale;

public class FURLComponent extends FComponent{

    public FURLComponent(CommandSender recipient, CommandSender sender, String text, String url) {
        super(text);

        String showText = locale.getFormatString("chat.url.hover-message", recipient, sender);
        addOpenURL(url);
        addHoverText(showText);
    }
}
