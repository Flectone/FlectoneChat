package net.flectone.chat.model.sound;

import lombok.Getter;
import net.flectone.chat.FlectoneChat;
import net.flectone.chat.model.file.FConfiguration;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FSound {

    @Getter
    private final Player sender;
    private Player recipient;
    private final String name;
    private Location location;

    private final FConfiguration sounds;

    public FSound(@Nullable Player sender, @Nullable Player recipient, @NotNull String name) {
        this.sender = sender;
        this.recipient = recipient;
        this.name = name;

        sounds = FlectoneChat.getPlugin().getFileManager().getSounds();
    }

    public FSound(@Nullable Player player, @NotNull String name) {
        this(player, player, name);
    }

    public FSound(@Nullable Player sender, @Nullable Location location, @NotNull String name) {
        this.sender = sender;
        this.location = location;
        this.name = name;

        sounds = FlectoneChat.getPlugin().getFileManager().getSounds();
    }

    public void play() {
        if (recipient == null && location == null) return;
        if (!sounds.getVaultBoolean(sender, name + ".enable")) return;

        String[] params = sounds.getVaultString(recipient, name + ".type").split(":");

        try {

            if (recipient != null) {
                recipient.playSound(recipient.getLocation(), Sound.valueOf(params[0]), Float.parseFloat(params[1]), Float.parseFloat(params[2]));
                return;
            }

            if (location.getWorld() == null) return;

            location.getWorld().playSound(location, Sound.valueOf(params[0]), Float.parseFloat(params[1]), Float.parseFloat(params[2]));

        } catch (IllegalArgumentException e) {
            FlectoneChat.warning("Incorrect sound " + params[0] + " for " + name + ".sound.type");
            e.printStackTrace();
        }

    }
}
