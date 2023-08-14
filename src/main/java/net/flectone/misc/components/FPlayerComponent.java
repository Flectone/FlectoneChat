package net.flectone.misc.components;

import org.bukkit.command.CommandSender;

import static net.flectone.managers.FileManager.locale;

public class FPlayerComponent extends FComponent{

    public FPlayerComponent(CommandSender recipient, CommandSender sender, String text) {
        super(text);

        String playerName = sender.getName();

        String showText = locale.getFormatString("player.hover-message", recipient, sender)
                .replace("<player>", playerName);

        addSuggestCommand("/msg " + playerName + " ");
        addHoverText(showText);
    }
}
