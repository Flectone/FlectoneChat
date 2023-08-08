package net.flectone.listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.spigotmc.event.entity.EntityDismountEvent;

public class EntityDismountListener implements Listener {

    @EventHandler
    public void checkAreaEffectCloudDismount(@NotNull EntityDismountEvent event) {
        Entity entity = event.getEntity();
        if (entity.getType().equals(EntityType.AREA_EFFECT_CLOUD)) entity.remove();
    }
}
