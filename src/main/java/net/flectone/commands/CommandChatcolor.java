package net.flectone.commands;

import net.flectone.Main;
import net.flectone.custom.FCommands;
import net.flectone.custom.FPlayer;
import net.flectone.custom.FTabCompleter;
import net.flectone.managers.FPlayerManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class CommandChatcolor extends FTabCompleter {

    public CommandChatcolor() {
        super.commandName = "chatcolor";
    }

    public static String[] getDefaultColors() {
        return new String[]{Main.config.getString("color.first"), Main.config.getString("color.second")};
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        FCommands fCommand = new FCommands(commandSender, command.getName(), s, strings);

        if (strings.length == 0 || (strings.length == 1 && !strings[0].equalsIgnoreCase("default"))) {
            fCommand.sendUsageMessage();
            return true;
        }

        FPlayer fPlayer;

        if (strings.length == 3 && commandSender.hasPermission("flectonechat.chatcolor.other")) {
            fPlayer = FPlayerManager.getPlayerFromName(strings[2]);
            if (fPlayer == null) {
                fCommand.sendMeMessage("command.null-player");
                return true;
            }
        } else {
            if (fCommand.isConsoleMessage()) return true;
            fPlayer = fCommand.getFPlayer();
        }

        if (fCommand.isHaveCD()) return true;

        if (strings[0].equalsIgnoreCase("default")) {
            strings = getDefaultColors();
        }

        setColors(fPlayer, strings);
        return true;
    }

    private void setColors(FPlayer fPlayer, String[] strings) {
        fPlayer.setColors(strings[0], strings[1]);
        fPlayer.setUpdated(true);

        if (fPlayer.isOnline()) {
            fPlayer.setDisplayName();
            fPlayer.getPlayer().sendMessage(Main.locale.getFormatString("command.chatcolor.message", fPlayer.getPlayer()));
        }
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        wordsList.clear();

        if (strings.length == 1) {
            isStartsWith(strings[0], "default");
            isStartsWith(strings[0], "#1abaf0");
            isStartsWith(strings[0], "&b");
        } else if (strings.length == 2) {
            isStartsWith(strings[1], "#77d7f7");
            isStartsWith(strings[1], "&f");
        } else if (strings.length == 3 && commandSender.hasPermission("flectonechat.chatcolor.other")){
            isOfflinePlayer(strings[2]);
        }

        Collections.sort(wordsList);

        return wordsList;
    }
}
