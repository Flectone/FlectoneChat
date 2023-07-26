package net.flectone.tickers;

import net.flectone.Main;
import net.flectone.custom.FBukkitRunnable;
import net.flectone.custom.FPlayer;
import net.flectone.managers.FPlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class ChatBubbleTicker extends FBukkitRunnable {

    public ChatBubbleTicker() {
        super.period = 5L;
    }

    @Override
    public void run() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            FPlayer fPlayer = FPlayerManager.getPlayer(player);
            List<Entity> entities = fPlayer.getChatBubbleEntities();

            Material currentBlock = player.getLocation().getBlock().getType();
            if (currentBlock.equals(Material.NETHER_PORTAL) || currentBlock.equals(Material.END_PORTAL)) {
                fPlayer.clearChatBubbles();
            }

            if (fPlayer.getListChatBubbles().isEmpty() || !entities.isEmpty()) return;

            String message = fPlayer.getListChatBubbles().get(0);
            spawnMessageBubble(fPlayer.getPlayer(), message);

            fPlayer.removeChatBubble();
        });
    }

    private void spawnMessageBubble(Player player, String message) {
        List<String> messageStrings = divideText(message, Main.config.getInt("chat.bubble.max-per-line"));
        String color = Main.locale.getFormatString("chat.bubble.color", null);
        int readSpeed = Main.config.getInt("chat.bubble.read-speed");

        int duration = (message.length() + 8 * messageStrings.size()) * 1200 / readSpeed;

        Entity lastVehicle = spawnStringBubble(player, "", player.getLocation(), duration);

        for (int x = messageStrings.size() - 1; x > -1; x--) {
            lastVehicle = spawnStringBubble(lastVehicle, color + messageStrings.get(x), player.getLocation(), duration);
        }
    }

    private List<String> divideText(String text, int maxCharactersPerLine) {
        List<String> lines = new ArrayList<>();
        String line = "";

        for (int x = 0; x < text.length(); x++) {
            char symbol = text.charAt(x);
            line += symbol;

            if ((symbol == ' ' && line.length() > maxCharactersPerLine - 5)
                    || line.length() > maxCharactersPerLine) {

                lines.add(symbol == ' ' ? line.trim() : line + "-");
                line = "";
            }
        }

        if (!line.isEmpty()) lines.add(line);

        return lines;
    }

    // Thanks @atesin for chat bubbles implementation
    // https://github.com/atesin/LightChatBubbles
    private AreaEffectCloud spawnStringBubble(Entity vehicle, String message, Location location, int duration) {
        location.setDirection(new Vector(0, -1, 0));
        AreaEffectCloud nameTag = (AreaEffectCloud) location.getWorld().spawnEntity(location, EntityType.AREA_EFFECT_CLOUD);
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
