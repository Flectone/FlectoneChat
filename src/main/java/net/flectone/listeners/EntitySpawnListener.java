package net.flectone.listeners;

import org.bukkit.Location;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Entity;
import org.bukkit.entity.MagmaCube;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class EntitySpawnListener implements Listener {

    @EventHandler
    public void checkCustomEntitySpawn(@NotNull EntitySpawnEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof MagmaCube)
                && !(entity instanceof AreaEffectCloud)) return;

        Location location = event.getEntity().getLocation();

        if (location.getDirection().equals(new Vector(0, 1, 0))) {

            MagmaCube magmaCube = (MagmaCube) event.getEntity();

            magmaCube.setGravity(false);
            magmaCube.setSilent(true);
            magmaCube.setInvulnerable(true);
            magmaCube.setGlowing(true);
            magmaCube.setAI(false);
            magmaCube.setSize(1);
            magmaCube.setInvisible(true);
            magmaCube.setGlowing(true);
            return;
        }

        if (location.getDirection().equals(new Vector(0, -1, 0))) {
            AreaEffectCloud areaEffectCloud = (AreaEffectCloud) event.getEntity();
            areaEffectCloud.setRadius(0);
        }

    }
}
