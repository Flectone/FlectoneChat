package net.flectone.chat.listener;

import net.flectone.chat.module.FListener;
import net.flectone.chat.module.FModule;
import org.bukkit.Location;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.spigotmc.event.entity.EntityDismountEvent;

public class ChatBubbleSpawnListener extends FListener {
    public ChatBubbleSpawnListener(FModule module) {
        super(module);
        init();
    }

    @Override
    public void init() {
        register();
    }

    @EventHandler
    public void chatBubbleDismountEvent(@NotNull EntityDismountEvent event) {
        Entity entity = event.getEntity();
        if (entity.getType().equals(EntityType.AREA_EFFECT_CLOUD)) entity.remove();
    }

    @EventHandler
    public void chatBubbleSpawnEvent(@NotNull EntitySpawnEvent event) {
        if (!(event.getEntity() instanceof AreaEffectCloud areaEffectCloud)) return;

        Location location = areaEffectCloud.getLocation();

        if (location.getDirection().equals(new Vector(0, -1, 0))) {
            areaEffectCloud.setRadius(0);
        }
    }
}
