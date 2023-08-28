package net.flectone.misc.components;

import net.flectone.misc.entity.player.PlayerDamager;
import net.flectone.utils.NMSUtil;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FLocaleComponent extends FComponent{

    public FLocaleComponent(@Nullable String string) {
        super(new TranslatableComponent(string));
    }

    public FLocaleComponent(@NotNull PlayerDamager playerDamager) {
        this(playerDamager.getDamagerTranslateName());
    }

    public FLocaleComponent(@Nullable Entity entity) {
        this(NMSUtil.getMinecraftName(entity));
    }

    public FLocaleComponent(@Nullable ItemStack itemStack) {
        this(NMSUtil.getCorrectlyName(itemStack));

        addHoverItem(NMSUtil.getItemAsJson(itemStack));
    }
}
