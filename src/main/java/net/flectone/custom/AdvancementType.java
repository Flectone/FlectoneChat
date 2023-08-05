package net.flectone.custom;

import javax.annotation.Nullable;
import java.util.Arrays;

public enum AdvancementType {
    UNKNOWN,
    TASK,
    GOAL,
    CHALLENGE;

    public static AdvancementType getType(@Nullable String name) {
        if (name == null) return UNKNOWN;

        return Arrays.stream(values()).parallel()
                .filter(type -> type.toString().equalsIgnoreCase(name))
                .findFirst()
                .orElse(UNKNOWN);
    }

    @Override
    public String toString(){
        return name().toLowerCase();
    }
}
