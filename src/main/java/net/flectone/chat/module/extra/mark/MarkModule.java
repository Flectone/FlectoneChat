package net.flectone.chat.module.extra.mark;


import net.flectone.chat.manager.FActionManager;
import net.flectone.chat.model.mark.Mark;
import net.flectone.chat.module.FModule;
import org.bukkit.entity.Entity;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class MarkModule extends FModule {

    public MarkModule(FModule module, String name) {
        super(module, name);
        init();
    }

    @Override
    public void init() {
        if (!isEnabled()) return;
        register();

        FActionManager.add(new MarkListener(this));
    }

    public void removeBugEntities(@NotNull Player player) {
        player.getWorld().getNearbyEntities(player.getLocation(), 20, 20, 20, Entity::isGlowing).forEach(entity -> {
            if (entity instanceof MagmaCube
                    && entity.getLocation().getDirection().equals(new Vector(0, 1, 0))) entity.remove();
            entity.setGlowing(false);
        });
    }

    public void mark(Player player, int range, String color) {
        Mark mark = Mark.getMark(player, range, color);
        mark.spawn();
    }
}
