package net.flectone.misc.components;

import net.flectone.misc.entity.FDamager;
import net.flectone.utils.NMSUtil;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

public class FLocaleComponent extends FComponent{

    public FLocaleComponent(String string) {
        super(new TranslatableComponent(string));
    }

    public FLocaleComponent(FDamager fDamager) {
        this(fDamager.getDamagerTranslateName());
    }

    public FLocaleComponent(Entity entity) {
        this(NMSUtil.getMinecraftName(entity));
    }

    public FLocaleComponent(ItemStack itemStack) {
        this(NMSUtil.getCorrectlyName(itemStack));

        addHoverItem(NMSUtil.getItemAsJson(itemStack));
    }
}
