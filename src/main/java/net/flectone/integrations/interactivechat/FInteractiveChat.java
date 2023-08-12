package net.flectone.integrations.interactivechat;

import com.loohp.interactivechat.api.InteractiveChatAPI;
import net.flectone.Main;
import net.flectone.integrations.HookInterface;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class FInteractiveChat implements HookInterface {

    private static boolean isEnable;

    public static boolean isEnable() {
        return isEnable;
    }

    @NotNull
    public static String mark(@NotNull String message, @NotNull UUID sender) {
        return InteractiveChatAPI.markSender(message, sender);
    }

    @Override
    public void hook() {
        isEnable = true;
        Main.info("\uD83D\uDD12 InteractiveChat detected and hooked");
    }
}
