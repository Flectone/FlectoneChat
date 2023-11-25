package net.flectone.chat.listener;

import net.flectone.chat.module.FListener;
import net.flectone.chat.module.FModule;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.MagmaCube;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class MarkSpawnListener extends FListener {

    public MarkSpawnListener(FModule module) {
        super(module);
        init();
    }

    @Override
    public void init() {
        registerEvents();
    }

    @EventHandler
    public void checkCustomEntitySpawn(@NotNull EntitySpawnEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof MagmaCube)) return;

        Location location = event.getEntity().getLocation();

        if (!location.getDirection().equals(new Vector(0, 1, 0))) return;

        MagmaCube magmaCube = (MagmaCube) event.getEntity();

        magmaCube.setGravity(false);
        magmaCube.setSilent(true);
        magmaCube.setInvulnerable(true);
        magmaCube.setGlowing(true);
        magmaCube.setAI(false);
        magmaCube.setSize(1);
        magmaCube.setInvisible(true);
        magmaCube.setGlowing(true);
    }
}