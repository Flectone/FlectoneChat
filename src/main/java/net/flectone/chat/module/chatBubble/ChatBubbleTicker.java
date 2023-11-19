package net.flectone.chat.module.chatBubble;

import net.flectone.chat.module.FModule;
import net.flectone.chat.module.FTicker;
import net.flectone.chat.module.integrations.IntegrationsModule;
import net.flectone.chat.model.player.FPlayer;
import net.flectone.chat.util.MessageUtil;
import net.flectone.chat.util.PlayerUtil;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Entity;

import java.util.List;

public class ChatBubbleTicker extends FTicker {

    public ChatBubbleTicker(FModule module) {
        super(module);
        init();
    }

    @Override
    public void init() {
        super.period = 20L;
        runTaskTimer();
    }

    @Override
    public void run() {
        PlayerUtil.getPlayersWithFeature(getModule() + ".enable")
                .stream()
                .filter(player -> !player.isInsideVehicle() || !(player.getVehicle() instanceof AreaEffectCloud))
                .forEach(player -> {

                    FPlayer fPlayer = playerManager.get(player);
                    if(fPlayer == null) return;
                    if (fPlayer.getChatBubbles().isEmpty()) return;


                    Material currentBlock = player.getLocation().getBlock().getType();
                    if (currentBlock.equals(Material.NETHER_PORTAL)
                            || currentBlock.equals(Material.END_PORTAL)
                            || player.getGameMode() == GameMode.SPECTATOR
                            || IntegrationsModule.isVanished(player)) {
                        fPlayer.getChatBubbles().poll();
                        return;
                    }

                    List<Entity> entities = player.getPassengers().parallelStream()
                            .filter(entity -> entity instanceof AreaEffectCloud)
                            .toList();

                    if (!entities.isEmpty()) return;

                    String message = fPlayer.getChatBubbles().poll();
                    if (message == null) return;

                    int maxPerLine = config.getVaultInt(player, getModule() + ".max-per-line");
                    String color = config.getVaultString(player, getModule() + ".color");
                    int readSpeed = config.getVaultInt(player, getModule() + ".read-speed");
                    int height = config.getVaultInt(player, getModule() + ".height");

                    ((ChatBubbleModule) getModule()).spawn(player, message, MessageUtil.formatAll(player, color),
                            height, maxPerLine, readSpeed);
                });
    }
}
