package net.flectone.listeners;

import org.bukkit.Location;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.MagmaCube;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.util.Vector;

public class EntitySpawnListener implements Listener {

    @EventHandler
    public void checkCustomEntitySpawn(EntitySpawnEvent event) {
        if (!(event.getEntity() instanceof MagmaCube) && !(event.getEntity() instanceof AreaEffectCloud)) return;

        Location location = event.getEntity().getLocation();

        if (location.getDirection().equals(new Vector(0, 1, 0))) {

            MagmaCube entity = (MagmaCube) event.getEntity();

            entity.setGravity(false);
            entity.setSilent(true);
            entity.setInvulnerable(true);
            entity.setGlowing(true);
            entity.setAI(false);
            entity.setSize(1);
            entity.setInvisible(true);
            entity.setGlowing(true);
            return;
        }

        if (location.getDirection().equals(new Vector(0, -1, 0))) {
            AreaEffectCloud entity = (AreaEffectCloud) event.getEntity();
            entity.setRadius(0);
        }

    }
}
