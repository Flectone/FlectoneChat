package net.flectone.misc.components;

import net.flectone.misc.entity.FPlayer;
import net.flectone.utils.ObjectUtil;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FPlayerComponent extends FComponent{

    public FPlayerComponent(@Nullable CommandSender recipient, @NotNull CommandSender sender, @NotNull String text) {
        super(text);

        String hoverText = FPlayer.getVaultLocaleString(sender, "player.hover.<group>.text")
                .replace("<player>", sender.getName());

        addHoverText(ObjectUtil.formatString(hoverText, recipient, sender));

        String command = FPlayer.getVaultLocaleString(sender, "player.hover.<group>.command")
                .replace("<player>", sender.getName());

        switch (FPlayer.getVaultLocaleString(sender, "player.hover.<group>.command-type").toLowerCase()) {
            case "suggest" -> addSuggestCommand(command);
            case "run" -> addRunCommand(command);
        }
    }
}
