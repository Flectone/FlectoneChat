package net.flectone.commands;

import net.flectone.Main;
import net.flectone.misc.commands.FCommand;
import net.flectone.misc.commands.FTabCompleter;
import net.flectone.misc.entity.FEntity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
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

import static net.flectone.managers.FileManager.config;

public class CommandMark implements FTabCompleter {

    public static final String[] chatColorValues = {
            "BLACK",
            "DARK_BLUE",
            "DARK_GREEN",
            "DARK_AQUA",
            "DARK_RED",
            "DARK_PURPLE",
            "GOLD",
            "GRAY",
            "DARK_GRAY",
            "BLUE",
            "GREEN",
            "AQUA",
            "RED",
            "LIGHT_PURPLE",
            "YELLOW",
            "WHITE"};

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommand fCommand = new FCommand(commandSender, command.getName(), s, strings);

        if (fCommand.isConsoleMessage()) return true;

        String color = (strings.length > 0) ? strings[0].toUpperCase() : "WHITE";

        if (!Arrays.asList(chatColorValues).contains(color)) {
            fCommand.sendMeMessage("command.mark.wrong-color");
            return true;
        }

        if (fCommand.isHaveCD() || fCommand.isMuted()) return true;

        int range = config.getInt("command.mark.range");

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
            Arrays.stream(chatColorValues).parallel()
                    .forEach(color -> isStartsWith(strings[0], color));
        }

        Collections.sort(wordsList);

        return wordsList;
    }

    @Nullable
    private Entity getEntityInLineOfSightVectorMath(@NotNull Player player, int range) {
        RayTraceResult rayTraceResult = player.getWorld().rayTraceEntities(player.getEyeLocation(), player.getLocation().getDirection(), range, 0.35, entity -> {
            // ignoring executor
            if (player.equals(entity)) return false;

            return player.hasLineOfSight(entity);
        });

        return (rayTraceResult != null) ? rayTraceResult.getHitEntity() : null;
    }

    private void spawnMarkEntity(@NotNull Location location, @NotNull String color) {
        location.setX(Math.floor(location.getX()) + 0.5);
        location.setY(Math.floor(location.getY()) + 0.25);
        location.setZ(Math.floor(location.getZ()) + 0.5);
        location.setDirection(new Vector(0, 1, 0));

        World world = location.getWorld();
        if(world == null) return;

        MagmaCube markBlockEntity = (MagmaCube) world.spawnEntity(location, EntityType.MAGMA_CUBE);

        FEntity.addToTeam(markBlockEntity, color);

        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
            markBlockEntity.remove();
            FEntity.removeFromTeam(markBlockEntity, color);
        }, 40);
    }

    @NotNull
    @Override
    public String getCommandName() {
        return "mark";
    }
}
