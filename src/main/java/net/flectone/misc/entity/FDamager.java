package net.flectone.misc.entity;

import net.flectone.utils.NMSUtil;
import net.flectone.utils.ObjectUtil;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

public class FDamager {

    private int time;
    private Entity killer;
    private String killerItemName;
    private String killerItemAsJson;
    private Entity finalEntityDamager;
    private Material finalBlockDamager;
    private String damagerTranslateName;

    public FDamager() {
    }

    public void replaceDamager(Entity damager) {
        killer = damager;
        time = ObjectUtil.getCurrentTime();
    }

    public Entity getKiller() {
        return killer;
    }

    public void setKiller(Entity killer) {
        this.killer = killer;
    }

    private ItemStack killerItem;

    public void setKillerItem(ItemStack itemStack) {
        this.killerItem = itemStack;
        this.killerItemName = NMSUtil.getMinecraftName(itemStack);
        this.killerItemAsJson = NMSUtil.getItemAsJson(itemStack);
    }

    public ItemStack getKillerItem() {
        return killerItem;
    }

    public String getKillerItemName() {
        return killerItemName;
    }

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

    public String getDamagerTranslateName() {
        return damagerTranslateName;
    }

    public Material getFinalBlockDamager() {
        return finalBlockDamager;
    }

    public Entity getFinalEntity() {
        return finalEntityDamager;
    }

    public boolean isFinalEntity() {
        return finalEntityDamager != null;
    }

    public boolean isFinalBlock() {
        return finalBlockDamager != null;
    }
}
