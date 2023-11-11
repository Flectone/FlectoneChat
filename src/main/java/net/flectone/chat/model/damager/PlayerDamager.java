package net.flectone.chat.model.damager;

import lombok.Getter;
import net.flectone.chat.util.NMSUtil;
import net.flectone.chat.util.TimeUtil;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerDamager {

    @Getter
    private int time;
    @Getter
    private Entity killer;
    private String killerItemName;
    private Entity finalEntityDamager;
    private Material finalBlockDamager;
    private String damagerTranslateName;
    private ItemStack killerItem;

    public PlayerDamager() {}

    public void replaceDamager(@Nullable Entity damager) {
        killer = damager;
        time = TimeUtil.getCurrentTime();
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
    }

    @Nullable
    public String getKillerItemName() {
        return killerItemName;
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

    public boolean isExpired() {
        return TimeUtil.getCurrentTime() - getTime() < 5;
    }
}
