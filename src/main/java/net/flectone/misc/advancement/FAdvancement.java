package net.flectone.misc.advancement;

import net.flectone.utils.NMSUtil;
import org.bukkit.advancement.Advancement;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.lang.reflect.Field;

// Thanks, @CroaBeast, for these methods
// Source https://github.com/CroaBeast/AdvancementInfo

public class FAdvancement {
    private static final String COMP_CLASS = "IChatBaseComponent";
    private final Advancement adv;
    private String toChat = null;
    private String hidden = null;
    private FAdvancementType type = FAdvancementType.UNKNOWN;
    private ItemStack item = null;
    private String translateKey;
    private String translateDesc;

    private String title;

    public FAdvancement(@NotNull Advancement adv) {
        this.adv = adv;

        Class<?> craftClass = NMSUtil.getBukkitClass("advancement.CraftAdvancement");
        if (craftClass == null) return;

        Object nmsAdv = NMSUtil.getObject(craftClass, craftClass.cast(adv), "getHandle");
        Object display = NMSUtil.getObject(nmsAdv, is_19_4() ? "d" : "c");
        if (display == null) return;

        Object rawTitle = NMSUtil.getObject(display, "a");
        Object rawDesc = NMSUtil.getObject(display, "b");

        translateKey = String.valueOf(NMSUtil.getObject(NMSUtil.getObject(rawTitle, "b"), "a"));
        translateDesc = String.valueOf(NMSUtil.getObject(NMSUtil.getObject(rawDesc, "b"), "a"));

        Field itemField = null;
        try {
            itemField = display.getClass().getDeclaredField("c");
        } catch (Exception e) {
            e.printStackTrace();
        }

        Object nmsItemStack = null;
        if (itemField != null) {
            try {
                itemField.setAccessible(true);
                nmsItemStack = itemField.get(display);
                itemField.setAccessible(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        String typeName = NMSUtil.checkValue(NMSUtil.getObject(display, "e"), "PROGRESS");
        this.type = FAdvancementType.getType(typeName);

        toChat = NMSUtil.checkValue(NMSUtil.getObject(display, "i"));
        hidden = NMSUtil.checkValue(NMSUtil.getObject(display, "j"));

        item = NMSUtil.getBukkitItem(nmsItemStack);

        Class<?> chatClass = NMSUtil.getVersion() >= 17 ?
                NMSUtil.getNMSClass("net.minecraft.network.chat", COMP_CLASS, false) :
                NMSUtil.getNMSClass(null, COMP_CLASS, true);

        if (chatClass != null) {
            String method = NMSUtil.getVersion() < 13 ? "toPlainText" : "getString";
            title = String.valueOf(NMSUtil.getObject(chatClass, rawTitle, method));
        }
    }

    private static boolean is_19_4() {
        return NMSUtil.getVersion() >= 19.4;
    }

    private static boolean getBool(String string) {
        return string.matches("(?i)true|false") && string.matches("(?i)true");
    }

    @NotNull
    public String getTitle() {
        return title;
    }

    @NotNull
    public String getTranslateKey() {
        return translateKey;
    }

    @NotNull
    public String getTranslateDesc() {
        return translateDesc;
    }

    @NotNull
    public Advancement getBukkit() {
        return adv;
    }

    @NotNull
    public FAdvancementType getType() {
        return type;
    }

    public boolean announceToChat() {
        return getBool(toChat);
    }

    public boolean isHidden() {
        return getBool(hidden);
    }

    @Nullable
    public ItemStack getItem() {
        return item;
    }
}

