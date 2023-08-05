package net.flectone.commands;

import net.flectone.Main;
import net.flectone.misc.commands.FCommands;
import net.flectone.misc.commands.FTabCompleter;
import net.flectone.integrations.discordsrv.FDiscordSRV;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CommandMaintenance extends FTabCompleter {

    public CommandMaintenance() {
        super.commandName = "maintenance";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommands fCommand = new FCommands(commandSender, command.getName(), s, strings);

        if (fCommand.isInsufficientArgs(1)) return true;

        if (!strings[0].equalsIgnoreCase("on") && !strings[0].equalsIgnoreCase("off")) {
            fCommand.sendUsageMessage();
            return true;
        }

        boolean haveMaintenance = Main.config.getBoolean("command.maintenance.enable");

        if (haveMaintenance && strings[0].equalsIgnoreCase("on")) {
            fCommand.sendMeMessage("command.maintenance.turned-on.already");
            return true;
        }

        if (!haveMaintenance && strings[0].equalsIgnoreCase("off")) {
            fCommand.sendMeMessage("command.maintenance.turned-off.not");
            return true;
        }

        if (fCommand.isHaveCD()) return true;

        if (fCommand.isMuted()) return true;

        haveMaintenance = strings[0].equalsIgnoreCase("on");

        if (haveMaintenance) {

            Set<Player> playerSet = new HashSet<>(Bukkit.getOnlinePlayers());

            playerSet.stream().parallel()
                    .filter(player -> !player.isOp() && !player.hasPermission(Main.config.getString("command.maintenance.permission")) && player.isOnline())
                    .forEach(player -> player.kickPlayer(Main.locale.getFormatString("command.maintenance.kicked-message", null)));

        }

        String maintenanceMessage = "command.maintenance.turned-" + strings[0].toLowerCase() + ".message";

        FDiscordSRV.sendModerationMessage(Main.locale.getString(maintenanceMessage));

        fCommand.sendMeMessage(maintenanceMessage);
        Main.config.setObject("command.maintenance.enable", haveMaintenance);
        Main.config.saveFile();

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        wordsList.clear();

        if (strings.length == 1) {
            isStartsWith(strings[0], "on");
            isStartsWith(strings[0], "off");
        }

        return wordsList;
    }
}
