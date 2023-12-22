package net.flectone.chat.model.player;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Getter
public class Settings {

    public final Map<Type, Object> SETTINGS_MAP = new HashMap<>();

    public Settings() {

    }

    public void add(Type type, Object object) {
        SETTINGS_MAP.put(type, object);
    }


    @Getter
    public enum Type {
        UUID("uuid"),
        COLORS("colors"),
        CHAT("chat"),
        STREAM("stream"),
        SPY("spy"),
        ADVANCEMENT("enable_advancement"),
        DEATH("enable_death"),
        JOIN("enable_join"),
        GREETING("enable_greeting"),
        QUIT("enable_quit"),
        AUTO_MESSAGE("enable_auto_message"),
        COMMAND_ME("enable_command_me"),
        COMMAND_TRY("enable_command_try"),
        COMMAND_DICE("enable_command_dice"),
        COMMAND_BALL("enable_command_ball"),
        COMMAND_BAN("enable_command_ban"),
        COMMAND_MUTE("enable_command_mute"),
        COMMAND_WARN("enable_command_warn"),
        COMMAND_TELL("enable_command_tell"),
        COMMAND_REPLY("enable_command_reply"),
        COMMAND_MAIL("enable_command_mail"),
        COMMAND_TICTACTOE("enable_command_tictactoe"),
        COMMAND_KICK("enable_command_kick"),
        COMMAND_TRANSLATETO("enable_command_translateto");

        private final String dbPath;
        Type(String dbPath) {
            this.dbPath = dbPath;
        }

        @Nullable
        public static Type fromString(@NotNull String string) {
            return Arrays.stream(Type.values())
                    .filter(type -> type.dbPath.equals(string))
                    .findAny()
                    .orElse(null);
        }

        @Override
        public String toString() {
            return dbPath;
        }
    }

    public String getValue(@NotNull Type type) {
        Object object = SETTINGS_MAP.get(type);
        return object == null ? null : String.valueOf(object);
    }

    @Nullable
    public HashMap<String, String> getColors() {
        Object object = SETTINGS_MAP.get(Type.COLORS);
        if (object instanceof HashMap<?,?> colors) return (HashMap<String, String>) colors;
        return null;
    }

    @Nullable
    public String getChat() {
        return (String) SETTINGS_MAP.get(Type.CHAT);
    }

    public void set(@NotNull Type type, @Nullable Object o) {
        SETTINGS_MAP.put(type, o);
    }
}
