package net.flectone.chat.model.mark;

import net.flectone.chat.FlectoneChat;
import net.flectone.chat.manager.FModuleManager;
import net.flectone.chat.manager.FPlayerManager;
import net.flectone.chat.model.player.FPlayer;
import net.flectone.chat.model.sound.FSound;
import net.flectone.chat.module.FModule;
import net.flectone.chat.module.sounds.SoundsModule;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Mark {

    public static final List<String> COLOR_VALUES = List.of("BLACK", "DARK_BLUE", "DARK_GREEN",
            "DARK_AQUA", "DARK_RED", "DARK_PURPLE", "GOLD", "GRAY", "DARK_GRAY", "BLUE", "GREEN",
            "AQUA", "RED", "LIGHT_PURPLE", "YELLOW", "WHITE");

    static  {
        COLOR_VALUES.forEach(color -> {
            Team team = FlectoneChat.getPlugin().getScoreBoard().getTeam(color);
            if (team != null) return;

            team = FlectoneChat.getPlugin().getScoreBoard().registerNewTeam(color);

            team.setCanSeeFriendlyInvisibles(false);
            team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
            team.setColor(ChatColor.valueOf(color));

        });
    }

    private final Entity entity;
    private final String color;
    private final Player player;

    private boolean needRemove;

    private ChatColor lastColor;

    private final FlectoneChat plugin;
    private final FPlayerManager playerManager;
    private final FModuleManager moduleManager;
    private final Scoreboard scoreboard;

    public Mark(Player player, Entity entity, String color) {
        this.player = player;
        this.entity = entity;
        this.color = color;

        plugin = FlectoneChat.getPlugin();
        playerManager = plugin.getPlayerManager();
        moduleManager = plugin.getModuleManager();
        scoreboard = plugin.getScoreBoard();
    }

    public Mark(Player player, Location location, String color) {
        this.player = player;
        this.color = color;

        World world = location.getWorld();
        if (!location.getBlock().getType().equals(Material.AIR) && world != null) {

            location.setX(Math.floor(location.getX()) + 0.5);
            location.setY(Math.floor(location.getY()) + 0.25);
            location.setZ(Math.floor(location.getZ()) + 0.5);

            location.setDirection(new Vector(0, 1, 0));

            this.entity = world.spawnEntity(location, EntityType.MAGMA_CUBE);
            needRemove = true;

        } else this.entity = null;

        plugin = FlectoneChat.getPlugin();
        playerManager = plugin.getPlayerManager();
        moduleManager = plugin.getModuleManager();
        scoreboard = plugin.getScoreBoard();
    }


    public static Mark getMark(Player player, int range, String color) {
        Entity entity = getEntityInLineOfSightVectorMath(player, range);

        if (entity != null && !entity.isGlowing()) {
            entity.setGlowing(true);

            return new Mark(player, entity, color);
        }

        Location location = player.getTargetBlock(null, range).getLocation();
        return new Mark(player, location, color);
    }

    @Nullable
    public static Entity getEntityInLineOfSightVectorMath(@NotNull Player player, int range) {
        RayTraceResult rayTraceResult = player.getWorld().rayTraceEntities(player.getEyeLocation(),
                player.getLocation().getDirection(), range, 0.35, entity -> {
            // ignoring executor
            if (player.equals(entity)) return false;

            return player.hasLineOfSight(entity);
        });

        return (rayTraceResult != null) ? rayTraceResult.getHitEntity() : null;
    }

    public void spawn() {
        if (entity == null) return;
        markEntity();
        unMarkEntityScheduler();

        FModule fModule = moduleManager.get(SoundsModule.class);
        if (fModule instanceof SoundsModule soundsModule) {
            soundsModule.play(new FSound(player, entity.getLocation(), "mark"));
        }
    }

    public void unMarkEntityScheduler() {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            entity.setGlowing(false);
            if (entity instanceof Player entityPlayer) {
                FPlayer fPlayer = playerManager.get(entityPlayer);
                if (fPlayer == null) return;
                fPlayer.getTeam().setColor(lastColor);
                return;
            }

            if (needRemove) entity.remove();

            Team team = scoreboard.getTeam(color);
            if (team == null) return;

            team.removeEntry(entity.getUniqueId().toString());
        }, 40);
    }

    public void markEntity() {

        if (entity instanceof Player entityPlayer) {
            FPlayer fPlayer = playerManager.get(entityPlayer);
            if (fPlayer == null) return;

            lastColor = fPlayer.getTeam().getColor();
            fPlayer.getTeam().setColor(ChatColor.valueOf(color));
            return;
        }

        Team team = scoreboard.getTeam(color);
        if (team == null) return;

        team.addEntry(entity.getUniqueId().toString());
    }
}
