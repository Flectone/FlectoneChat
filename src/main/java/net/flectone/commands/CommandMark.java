package net.flectone.commands;

import net.flectone.Main;
import net.flectone.custom.FCommands;
import net.flectone.custom.FEntity;
import net.flectone.custom.FTabCompleter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandMark extends FTabCompleter {

    public static final String[] chatColorValues = {"BLACK", "DARK_BLUE", "DARK_GREEN", "DARK_AQUA", "DARK_RED", "DARK_PURPLE", "GOLD", "GRAY", "DARK_GRAY", "BLUE", "GREEN", "AQUA", "RED", "LIGHT_PURPLE", "YELLOW", "WHITE"};

    public CommandMark() {
        super.commandName = "mark";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommands fCommand = new FCommands(commandSender, command.getName(), s, strings);

        if (fCommand.isConsoleMessage()) return true;

        if (!Main.config.getBoolean("command.mark.enable")) {
            fCommand.sendMeMessage("command.disabled");
            return true;
        }

        String color = (strings.length > 0) ? strings[0].toUpperCase() : "WHITE";

        if (!Arrays.asList(chatColorValues).contains(color)) {
            fCommand.sendMeMessage("command.mark.wrong-color");
            return true;
        }

        if (fCommand.isHaveCD()) return true;

        if (fCommand.isMuted()) return true;

        int range = Main.config.getInt("command.mark.range");

        Entity entity = getEntityInLineOfSightVectorMath((Player) commandSender, range);

        if (entity != null && !entity.isGlowing()) {
            entity.setGlowing(true);

            Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
                entity.setGlowing(false);
                FEntity.removeFromTeam(entity, color);
            }, 40);

            FEntity.addToTeam(entity, color);

            return true;
        }

        Location targetBlock = ((Player) commandSender).getTargetBlock(null, range).getLocation();
        if (!targetBlock.getBlock().getType().equals(Material.AIR)) {
            spawnMarkEntity(targetBlock, color);
        }

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        wordsList.clear();

        if (strings.length == 1) {
            for (String color : chatColorValues) {
                isStartsWith(strings[0], color);
            }
        }

        Collections.sort(wordsList);

        return wordsList;
    }

    private Entity getEntityInLineOfSightVectorMath(Player player, int range) {
        RayTraceResult rayTraceResult = player.getWorld().rayTraceEntities(player.getEyeLocation(), player.getLocation().getDirection(), range, entity -> {
            if (entity instanceof Player) {
                return !player.equals(entity);
            }
            return player.hasLineOfSight(entity);
        });

        return (rayTraceResult != null) ? rayTraceResult.getHitEntity() : null;
    }

    private void spawnMarkEntity(Location location, String color) {
        location.setX(Math.floor(location.getX()) + 0.5);
        location.setY(Math.floor(location.getY()) + 0.25);
        location.setZ(Math.floor(location.getZ()) + 0.5);
        location.setDirection(new Vector(0, 1, 0));

        MagmaCube markBlockEntity = (MagmaCube) location.getWorld().spawnEntity(location, EntityType.MAGMA_CUBE);

        FEntity.addToTeam(markBlockEntity, color);

        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
            markBlockEntity.remove();
            FEntity.removeFromTeam(markBlockEntity, color);
        }, 40);
    }
}
