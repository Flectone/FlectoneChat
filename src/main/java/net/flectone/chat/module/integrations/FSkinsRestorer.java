package net.flectone.chat.module.integrations;

import net.skinsrestorer.api.PropertyUtils;
import net.skinsrestorer.api.SkinsRestorer;
import net.skinsrestorer.api.SkinsRestorerProvider;
import net.skinsrestorer.api.exception.DataRequestException;
import net.skinsrestorer.api.property.SkinProperty;
import net.skinsrestorer.api.storage.PlayerStorage;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class FSkinsRestorer implements FIntegration{

    private SkinsRestorer skinsRestorer;

    public FSkinsRestorer() {
        init();
    }

    @Override
    public void init() {
        skinsRestorer = SkinsRestorerProvider.get();
    }

    @Nullable
    public String getTextureId(@NotNull Player player) {
        PlayerStorage storage = skinsRestorer.getPlayerStorage();
        try {
            Optional<SkinProperty> skin = storage.getSkinForPlayer(player.getUniqueId(), player.getName());
            return skin.map(PropertyUtils::getSkinTextureUrlStripped).orElse(null);
        } catch (DataRequestException e) {
            return null;
        }
    }
}
