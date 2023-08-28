package net.flectone.misc.entity.player;

import net.flectone.utils.NMSUtil;
import net.flectone.utils.ObjectUtil;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerDamager {

    private int time;
    private Entity killer;
    private String killerItemName;
    private String killerItemAsJson;
    private Entity finalEntityDamager;
    private Material finalBlockDamager;
    private String damagerTranslateName;
    private ItemStack killerItem;

    public PlayerDamager() {}

    public void replaceDamager(@Nullable Entity damager) {
        killer = damager;
        time = ObjectUtil.getCurrentTime();
    }

    public Entity getKiller() {
        return killer;
    }

    public void setKiller(@Nullable Entity killer) {
        this.killer = killer;
    }

    @Nullable
    public ItemStack getKillerItem() {
        return killerItem;
    }

    public void setKillerItem(@NotNull ItemStack itemStack) {
        this.killerItem = itemStack;
        this.killerItemName = NMSUtil.getMinecraftName(itemStack);
        this.killerItemAsJson = NMSUtil.getItemAsJson(itemStack);
    }

    @Nullable
    public String getKillerItemName() {
        return killerItemName;
    }

    @Nullable
    public String getKillerItemAsJson() {
        return killerItemAsJson;
    }

    public int getTime() {
        return time;
    }

    public void setFinalDamager(Entity finalDamager) {
        this.finalEntityDamager = finalDamager;
        this.damagerTranslateName = NMSUtil.getMinecraftName(finalDamager);
    }

    public void setFinalDamager(Material block) {
        this.finalBlockDamager = block;
        this.damagerTranslateName = NMSUtil.getMinecraftName(new ItemStack(finalBlockDamager));
    }

    @Nullable
    public String getDamagerTranslateName() {
        return damagerTranslateName;
    }

    @Nullable
    public Material getFinalBlockDamager() {
        return finalBlockDamager;
    }

    @Nullable
    public Entity getFinalEntity() {
        return finalEntityDamager;
    }

    public boolean isFinalBlock() {
        return finalBlockDamager != null;
    }
}
