package ru.flectone.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import ru.flectone.Main;
import ru.flectone.custom.FCommands;
import ru.flectone.custom.FEntity;

import java.util.Arrays;

public class CommandMark implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommands fCommand = new FCommands(commandSender, command.getName(), s, strings);

        if(fCommand.isConsoleMessage()) return true;

        if(!Main.config.getBoolean("mark.enable")) {
            fCommand.sendMeMessage( "mark.enable.false");
            return true;
        }

        String color = (strings.length > 0) ? strings[0].toUpperCase() : "WHITE";

        if(!Arrays.asList(TabComplets.chatColorValues).contains(color)){
            fCommand.sendMeMessage("mark.error_color");
            return true;
        }

        int range = Main.config.getInt("mark.range");

        Entity entity = getEntityInLineOfSightVectorMath((Player) commandSender, range);

        if(entity != null && !entity.isGlowing()){
            entity.setGlowing(true);

            Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
                entity.setGlowing(false);
                FEntity.leaveEntityTeam(entity, color);
            }, 40);

            FEntity.addEntityToENT(entity, color);

            return true;
        }

        Location targetBlock = ((Player) commandSender).getTargetBlock(null, range).getLocation();
        if (!targetBlock.getBlock().getType().equals(Material.AIR)) {
            spawnMarkEntity(targetBlock, color);
        }

        return true;
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

        FEntity.addEntityToENT(markBlockEntity, color);

        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
            markBlockEntity.remove();
            FEntity.leaveEntityTeam(markBlockEntity, color);
        }, 40);
    }
}
