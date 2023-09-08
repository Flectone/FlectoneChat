package net.flectone.tickers;

import net.flectone.managers.FPlayerManager;
import net.flectone.misc.entity.FPlayer;
import net.flectone.misc.runnables.FBukkitRunnable;
import org.bukkit.*;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static net.flectone.managers.FileManager.config;
import static net.flectone.managers.FileManager.locale;

public class ChatBubbleTicker extends FBukkitRunnable {

    public ChatBubbleTicker() {
        super.period = 20L;
    }

    @Override
    public void run() {
        Bukkit.getOnlinePlayers().stream()
                .filter(player -> !player.isInsideVehicle() || !(player.getVehicle() instanceof AreaEffectCloud))
                .forEach(player -> {

                    FPlayer fPlayer = FPlayerManager.getPlayer(player);
                    if(fPlayer == null) return;

                    List<Entity> entities = fPlayer.getChatBubbleEntities();

                    Material currentBlock = player.getLocation().getBlock().getType();
                    if (currentBlock.equals(Material.NETHER_PORTAL) || currentBlock.equals(Material.END_PORTAL)) {
                        fPlayer.clearChatBubbles();
                    }

                    if (fPlayer.getListChatBubbles().isEmpty() || !entities.isEmpty()) return;

                    String message = fPlayer.getListChatBubbles().get(0);
                    spawnMessageBubble(player, message);

                    fPlayer.removeChatBubble();
        });
    }

    private void spawnMessageBubble(@NotNull Player player, @NotNull String message) {
        List<String> messageStrings = divideText(message, config.getInt("chat.bubble.max-per-line"));
        String color = locale.getFormatString("chat.bubble.color", null);
        int readSpeed = config.getInt("chat.bubble.read-speed");

        int duration = (message.length() + 8 * messageStrings.size()) * 1200 / readSpeed;

        Entity lastVehicle = spawnStringBubble(player, "", player.getLocation(), duration);

        for (int x = messageStrings.size() - 1; x > -1; x--) {
            if(lastVehicle == null) return;
            lastVehicle = spawnStringBubble(lastVehicle, color + messageStrings.get(x), player.getLocation(), duration);
        }
    }

    private List<String> divideText(@NotNull String text, int maxCharactersPerLine) {
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
    private AreaEffectCloud spawnStringBubble(@NotNull Entity vehicle, @NotNull String message, @NotNull Location location, int duration) {
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
