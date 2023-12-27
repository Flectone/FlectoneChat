package net.flectone.chat.module.integrations;

import net.flectone.chat.FlectoneChat;
import net.skinsrestorer.api.PropertyUtils;
import net.skinsrestorer.api.SkinsRestorer;
import net.skinsrestorer.api.SkinsRestorerProvider;
import net.skinsrestorer.api.exception.DataRequestException;
import net.skinsrestorer.api.property.SkinProperty;
import net.skinsrestorer.api.storage.PlayerStorage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class FSkinsRestorer implements FIntegration {

    private SkinsRestorer skinsRestorer;

    public FSkinsRestorer() {
        init();
    }

    @Override
    public void init() {
        firstHook();
    }

    private void firstHook() {
        try {
            skinsRestorer = SkinsRestorerProvider.get();
            FlectoneChat.info("SkinsRestorer detected and hooked");
        } catch (Exception e) {
            FlectoneChat.warning("SkinsRestorer is not initialized yet. This is due to proxy. The second hook attempt will be in 10 seconds");
            secondHook();
        }
    }

    private void secondHook() {
        Bukkit.getScheduler().runTaskLaterAsynchronously(FlectoneChat.getPlugin(), () -> {
            try {
                skinsRestorer = SkinsRestorerProvider.get();
                FlectoneChat.info("SkinsRestorer detected and hooked");
            } catch (Exception e) {
                FlectoneChat.warning("SkinsRestorer is not initialized after all");
            }
        }, 200L);
    }

    @Nullable
    public String getTextureId(@NotNull Player player) {
        if (skinsRestorer == null) return null;

        PlayerStorage storage = skinsRestorer.getPlayerStorage();
        try {
            Optional<SkinProperty> skin = storage.getSkinForPlayer(player.getUniqueId(), player.getName());
            return skin.map(PropertyUtils::getSkinTextureUrlStripped).orElse(null);
        } catch (DataRequestException e) {
            return null;
        }
    }
}
