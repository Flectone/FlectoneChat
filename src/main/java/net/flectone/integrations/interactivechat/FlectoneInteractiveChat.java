package net.flectone.integrations.interactivechat;

import com.loohp.interactivechat.api.InteractiveChatAPI;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class FlectoneInteractiveChat {

    @NotNull
    public static String mark(@NotNull String message, @NotNull UUID sender) {
        return InteractiveChatAPI.markSender(message, sender);
    }
}
