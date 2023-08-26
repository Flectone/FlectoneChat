package net.flectone.misc.entity;

import net.flectone.managers.FPlayerManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class FEntity {

    private static final HashMap<String, Team> noCollisionTeamMap = new HashMap<>();

    public static void addToTeam(@NotNull Entity entity, @NotNull String color) {
        if (entity instanceof Player player) {
            FPlayer fPlayer = FPlayerManager.getPlayer(player);
            if (fPlayer == null) return;
            fPlayer.setTeamColor(color);
            return;
        }

        if (FPlayerManager.getScoreBoard().getTeam(color) != null)
            noCollisionTeamMap.put(color, FPlayerManager.getScoreBoard().getTeam(color));

        Team team = noCollisionTeamMap.get(color);

        if (team == null) {

            team = FPlayerManager.getScoreBoard().registerNewTeam(color);

            team.setCanSeeFriendlyInvisibles(false);
            team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
            team.setColor(ChatColor.valueOf(color));

            noCollisionTeamMap.put(color, team);
        }

        team.addEntry(entity.getUniqueId().toString());
    }

    public static void removeFromTeam(@NotNull Entity entity, @NotNull String color) {
        if (entity instanceof Player player) {
            FPlayer fPlayer = FPlayerManager.getPlayer(player);
            if (fPlayer == null) return;

            fPlayer.setTeamColor("WHITE");
            return;
        }

        Team team = noCollisionTeamMap.get(color);
        team.removeEntry(entity.getUniqueId().toString());
    }

    public static void removeBugEntities(@NotNull Player player) {
        player.getWorld().getNearbyEntities(player.getLocation(), 20, 20, 20, Entity::isGlowing).forEach(entity -> {
            if (entity instanceof MagmaCube
                    && entity.getLocation().getDirection().equals(new Vector(0, 1, 0))) entity.remove();

            entity.setGlowing(false);
        });
    }

}
