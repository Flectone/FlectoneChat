package net.flectone.chat.module.chatBubble;

import net.flectone.chat.manager.FActionManager;
import net.flectone.chat.manager.FPlayerManager;
import net.flectone.chat.model.player.FPlayer;
import net.flectone.chat.module.FModule;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ChatBubbleModule extends FModule {

    public ChatBubbleModule(String name) {
        super(name);
        init();
    }

    @Override
    public void init() {
        if (!isEnabled()) return;
        register();

        FActionManager.add(new ChatBubbleListener(this));
        FActionManager.add(new ChatBubbleTicker(this));
    }

    public void add(@NotNull Player player, @NotNull String message) {
        FPlayer fPlayer = FPlayerManager.get(player);
        if (fPlayer == null) return;
        fPlayer.addChatBubble(message);
    }

    public void spawn(@NotNull Player player, @NotNull String message, @NotNull String color, int maxPerLine, int readSpeed) {
        List<String> messageStrings = divideText(message, maxPerLine);

        int duration = (message.length() + 8 * messageStrings.size()) * 1200 / readSpeed;

        Entity lastVehicle = spawnStringBubble(player, "", player.getLocation(), duration);

        for (int x = messageStrings.size() - 1; x > -1; x--) {
            if(lastVehicle == null) return;

            lastVehicle = spawnStringBubble(lastVehicle, color + messageStrings.get(x).replace("§r", color),
                    player.getLocation(), duration);
        }
    }

    public List<String> divideText(@NotNull String text, int maxCharactersPerLine) {
        List<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();

        for (int x = 0; x < text.length(); x++) {
            char symbol = text.charAt(x);
            line.append(symbol);

            if ((symbol == ' ' && line.length() > maxCharactersPerLine - 5)
                    || line.length() > maxCharactersPerLine) {

                lines.add(symbol == ' ' ? line.toString().trim() : line + "-");
                line = new StringBuilder();
            }
        }

        if (!line.isEmpty()) lines.add(line.toString());

        return lines;
    }

    // Thanks, @atesin, for chat bubbles implementation
    // https://github.com/atesin/LightChatBubbles
    @Nullable
    public AreaEffectCloud spawnStringBubble(@NotNull Entity vehicle, @NotNull String message, @NotNull Location location, int duration) {
        location.setDirection(new Vector(0, -1, 0));

        World world = location.getWorld();
        if (world == null) return null;

        AreaEffectCloud nameTag = (AreaEffectCloud) world.spawnEntity(location, EntityType.AREA_EFFECT_CLOUD);
        nameTag.setParticle(Particle.TOWN_AURA);
        nameTag.setRadius(0);

        vehicle.addPassenger(nameTag);
        nameTag.setCustomName(message);
        nameTag.setCustomNameVisible(true);

        nameTag.setWaitTime(0);
        nameTag.setDuration(duration);
        return nameTag;
    }
}
