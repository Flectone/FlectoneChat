package net.flectone.integrations.interactivechat;

import com.loohp.interactivechat.api.InteractiveChatAPI;

import java.util.UUID;

public class FlectoneInteractiveChat {

    public static String mark(String message, UUID sender) {
        return InteractiveChatAPI.markSender(message, sender);
    }
}
