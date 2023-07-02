package net.flectone.listeners;

import org.bukkit.Location;
import org.bukkit.entity.MagmaCube;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.util.Vector;

public class EntitySpawnListener implements Listener {

    @EventHandler
    public void checkCustomEntitySpawn(EntitySpawnEvent event){
        if(!(event.getEntity() instanceof MagmaCube)) return;

        Location location = event.getEntity().getLocation();

        if(location.getDirection().equals(new Vector(0, 1, 0))){

            MagmaCube entity = (MagmaCube) event.getEntity();

            entity.setGravity(false);
            entity.setSilent(true);
            entity.setInvulnerable(true);
            entity.setGlowing(true);
            entity.setVisualFire(false);
            entity.setAI(false);
            entity.setSize(1);
            entity.setInvisible(true);
            entity.setGlowing(true);
        }
    }
}
