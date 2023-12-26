package net.flectone.chat.model.advancement;

import net.flectone.chat.util.NMSUtil;
import org.bukkit.advancement.Advancement;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Optional;

// Thanks, @CroaBeast, for these methods
// Source https://github.com    /CroaBeast/AdvancementInfo

public class FAdvancement {
    private static final String COMP_CLASS = "IChatBaseComponent";
    private String toChat = null;
    private String hidden = null;
    private Type type = Type.UNKNOWN;
    private String translateKey;
    private String translateDesc;

    private String title;

    private static final boolean IS_19_4;
    private static final boolean IS_20_2;
    private static final boolean IS_20_3;

    static {
        double value = NMSUtil.getVersion();
        IS_19_4 = value >= 19.4;
        IS_20_2 = value >= 20.2;
        IS_20_3 = value >= 20.3;
    }

    public FAdvancement(@NotNull Advancement adv) {

        Class<?> craftClass = NMSUtil.getBukkitClass("advancement.CraftAdvancement");
        if (craftClass == null) return;

        Object nmsAdv = NMSUtil.getObject(craftClass, craftClass.cast(adv), "getHandle");
        if (IS_20_2) nmsAdv = NMSUtil.getObject(nmsAdv, "b");

        Object display = NMSUtil.getObject(nmsAdv, IS_19_4 && !IS_20_3 ? "d" : "c");
        if (display == null) return;

        if (IS_20_2) {
            Optional<?> o = ((Optional<?>) display);
            if (o.isPresent()) display = o.get();
        }

        Object rawTitle = NMSUtil.getObject(display, "a");
        Object rawDesc = NMSUtil.getObject(display, "b");

        translateKey = getTranslateString(rawTitle);
        if (translateKey.isEmpty()) return;

        translateDesc = getTranslateString(rawDesc);
        if (translateDesc.isEmpty()) return;

        String typeName = NMSUtil.checkValue(NMSUtil.getObject(display, "e"), "PROGRESS");
        this.type = Type.getType(typeName);

        toChat = NMSUtil.checkValue(NMSUtil.getObject(display, "i"));
        hidden = NMSUtil.checkValue(NMSUtil.getObject(display, "j"));

        Class<?> chatClass = NMSUtil.getVersion() >= 17 ?
                NMSUtil.getNMSClass("net.minecraft.network.chat", COMP_CLASS, false) :
                NMSUtil.getNMSClass(null, COMP_CLASS, true);

        if (chatClass != null) {
            String method = NMSUtil.getVersion() < 13 ? "toPlainText" : "getString";
            title = String.valueOf(NMSUtil.getObject(chatClass, rawTitle, method));
        }
    }

    @NotNull
    private String getTranslateString(@Nullable Object object) {
        String string = String.valueOf(object);
        int start = string.indexOf("key='");
        int end = string.indexOf("',");

        if (start == -1 || end == -1) return "";

        return string.substring(start + 5, end);
    }

    private boolean getBool(String string) {
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
    public Type getType() {
        return type;
    }

    public boolean announceToChat() {
        return getBool(toChat);
    }

    public boolean isHidden() {
        return getBool(hidden);
    }

    public enum Type {
        UNKNOWN,
        TASK,
        GOAL,
        CHALLENGE;

        @NotNull
        public static Type getType(@Nullable String name) {
            if (name == null) return UNKNOWN;

            return Arrays.stream(values())
                    .filter(type -> type.toString().equalsIgnoreCase(name))
                    .findFirst()
                    .orElse(UNKNOWN);
        }

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }
}

