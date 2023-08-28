package net.flectone.misc.components;

import net.flectone.integrations.vault.FVault;
import net.flectone.managers.HookManager;
import net.flectone.utils.ObjectUtil;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


import static net.flectone.managers.FileManager.locale;

public class FPlayerComponent extends FComponent{

    public FPlayerComponent(@Nullable CommandSender recipient, @NotNull CommandSender sender, @NotNull String text) {
        super(text);

        String playerName = sender.getName();
        String hoverGroup = "default";

        if(HookManager.enabledVault && sender instanceof Player playerSender) {
            Chat provider = FVault.getProvider();

            String group = provider.getPrimaryGroup(playerSender);

            if(group != null) {
                hoverGroup = group;
            }
        }

        String hoverText = ObjectUtil.formatString(getHoverString(playerName, hoverGroup, "text"),
                recipient, sender);

        addHoverText(hoverText);

        String command = getHoverString(playerName, hoverGroup, "command");

        switch (getHoverString(playerName, hoverGroup, "command-type").toLowerCase()) {
            case "suggest" -> addSuggestCommand(command);
            case "run" -> addRunCommand(command);
        }
    }

    @NotNull
    private String getHoverString(String playerName, String hoverGroup, String type) {
        String hoverText = locale.getString("player.hover." + hoverGroup + "." + type);
        hoverText = hoverText.isEmpty()
                ? locale.getString("player.hover.default." + type)
                : hoverText;

        return hoverText.replace("<player>", playerName);
    }
}
