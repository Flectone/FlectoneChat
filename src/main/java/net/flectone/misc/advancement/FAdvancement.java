package net.flectone.misc.advancement;

import net.flectone.utils.NMSUtil;
import org.bukkit.advancement.Advancement;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

// Thanks, @CroaBeast, for these methods
// Source https://github.com/CroaBeast/AdvancementInfo

public class FAdvancement {
    private final Advancement adv;

    private String toChat = null, hidden = null, parent = null;

    private FAdvancementType type = FAdvancementType.UNKNOWN;

    private Object requirements = null;

    private ItemStack item = null;
    private Object rewards = null, criteria = null;

    private static final String COMP_CLASS = "IChatBaseComponent";

    private String translateKey;
    private String translateDesc;

    private String title;

    public FAdvancement(@NotNull Advancement adv) {
        this.adv = adv;

        Class<?> craftClass = net.flectone.utils.NMSUtil.getBukkitClass("advancement.CraftAdvancement");
        if (craftClass == null) return;

        Object nmsAdv = net.flectone.utils.NMSUtil.getObject(craftClass, craftClass.cast(adv), "getHandle");
        Object display = net.flectone.utils.NMSUtil.getObject(nmsAdv, is_19_4() ? "d" : "c");
        if (display == null) return;

        Object rawTitle = net.flectone.utils.NMSUtil.getObject(display, "a");
        Object rawDesc = net.flectone.utils.NMSUtil.getObject(display, "b");

        translateKey = String.valueOf(net.flectone.utils.NMSUtil.getObject(net.flectone.utils.NMSUtil.getObject(rawTitle, "b"), "a"));
        translateDesc = String.valueOf(net.flectone.utils.NMSUtil.getObject(net.flectone.utils.NMSUtil.getObject(rawDesc, "b"), "a"));

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

        String typeName = net.flectone.utils.NMSUtil.checkValue(net.flectone.utils.NMSUtil.getObject(display, "e"), "PROGRESS");
        this.type = FAdvancementType.getType(typeName);

        parent = net.flectone.utils.NMSUtil.checkValue(net.flectone.utils.NMSUtil.getObject(nmsAdv, "b", "getName"), "null");
        toChat = net.flectone.utils.NMSUtil.checkValue(net.flectone.utils.NMSUtil.getObject(display, "i"));
        hidden = NMSUtil.checkValue(net.flectone.utils.NMSUtil.getObject(display, "j"));

        item = net.flectone.utils.NMSUtil.getBukkitItem(nmsItemStack);
        requirements = net.flectone.utils.NMSUtil.getObject(nmsAdv, is_19_4() ? "j" : "i");
        rewards = net.flectone.utils.NMSUtil.getObject(nmsAdv, is_19_4() ? "e" : "d");
        criteria = net.flectone.utils.NMSUtil.getObject(nmsAdv, is_19_4() ? "g" :
                (net.flectone.utils.NMSUtil.getVersion() < 18 ? "getCriteria" : "f"));

        Class<?> chatClass = net.flectone.utils.NMSUtil.getVersion() >= 17 ?
                net.flectone.utils.NMSUtil.getNMSClass("net.minecraft.network.chat", COMP_CLASS, false) :
                net.flectone.utils.NMSUtil.getNMSClass(null, COMP_CLASS, true);

        if (chatClass != null) {
            String method = net.flectone.utils.NMSUtil.getVersion() < 13 ? "toPlainText" : "getString";
            title = String.valueOf(net.flectone.utils.NMSUtil.getObject(chatClass, rawTitle, method));
        }
    }

    public String getTitle() {
        return title;
    }

    public String getTranslateKey() {
        return translateKey;
    }

    public String getTranslateDesc() {
        return translateDesc;
    }

    private static boolean is_19_4() {
        return net.flectone.utils.NMSUtil.getVersion() >= 19.4;
    }

    @NotNull
    public Advancement getBukkit() {
        return adv;
    }

    @NotNull
    public FAdvancementType getType() {
        return type;
    }

    private static boolean getBool(String string) {
        return string.matches("(?i)true|false") && string.matches("(?i)true");
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

    @Nullable
    public Object getRewards() {
        return rewards;
    }

    @SuppressWarnings("unchecked")
    @NotNull
    public Map<String, Object> getCriteria() {
        try {
            return criteria == null ? new HashMap<>() : (Map<String, Object>) criteria;
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    @Nullable
    public String[][] getRequirements() {
        try {
            return (String[][]) requirements;
        } catch (Exception e) {
            return null;
        }
    }
}

