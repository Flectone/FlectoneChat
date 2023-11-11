package net.flectone.chat.listener;

import net.flectone.chat.component.FPlayerComponent;
import net.flectone.chat.module.FListener;
import net.flectone.chat.module.FModule;
import net.flectone.chat.util.MessageUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.NotNull;

public class SpitHitListener extends FListener {

    public SpitHitListener(FModule module) {
        super(module);
        init();
    }

    @Override
    public void init() {
        register();
    }

    @EventHandler
    public void spitHitEvent(@NotNull ProjectileHitEvent event) {
        Projectile entity = event.getEntity();
        if (entity.getType() != EntityType.LLAMA_SPIT) return;

        ProjectileSource projectileSource = entity.getShooter();
        if (!(projectileSource instanceof Player player)) return;

        event.setCancelled(true);

        Entity hitEntity = event.getHitEntity();
        if (!(hitEntity instanceof Player hitPlayer)) return;

        String message = locale.getVaultString(hitPlayer, "commands.spit.message");
        message = MessageUtil.formatPlayerString(player, message);

        message = MessageUtil.formatAll(player, hitPlayer, message);

        hitPlayer.spigot().sendMessage(new FPlayerComponent(player, hitEntity, message).get());
    }
}
