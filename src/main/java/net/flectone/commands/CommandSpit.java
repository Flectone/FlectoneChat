package net.flectone.commands;

import net.flectone.misc.commands.FCommand;
import net.flectone.misc.commands.FTabCompleter;
import net.flectone.utils.ObjectUtil;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LlamaSpit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CommandSpit implements FTabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommand fCommand = new FCommand(commandSender, command.getName(), s, strings);
        if (fCommand.isConsoleMessage()) return true;

        Player player = (Player) commandSender;
        Location location = player.getEyeLocation();
        World world = player.getWorld();

        location.setY(location.getY() - 0.3);

        ObjectUtil.playSound(player, location, command.getName());
        LlamaSpit spit = (LlamaSpit) world.spawnEntity(location, EntityType.LLAMA_SPIT);
        spit.setVelocity(location.getDirection());

        return true;
    }

    @Override
    public String getCommandName() {
        return "spit";
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return null;
    }
}
