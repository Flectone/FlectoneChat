package net.flectone.chat.model.spit;

import net.flectone.chat.FlectoneChat;
import net.flectone.chat.model.sound.FSound;
import net.flectone.chat.module.FModule;
import net.flectone.chat.module.sounds.SoundsModule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LlamaSpit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Spit {


    private final World world;
    private final Location location;
    private final Player player;

    public Spit(@NotNull Player player) {
        this.location = player.getEyeLocation();
        this.world = player.getWorld();
        this.player = player;

        location.setY(location.getY() - 0.3);
    }

    public void spawn() {
        LlamaSpit spit = (LlamaSpit) world.spawnEntity(location, EntityType.LLAMA_SPIT);
        spit.setVelocity(location.getDirection());

        FModule fModule = FlectoneChat.getModuleManager().get(SoundsModule.class);
        if (fModule instanceof SoundsModule soundsModule) {
            soundsModule.play(new FSound(player, location, "spit"));
        }
    }

}
