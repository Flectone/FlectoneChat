package net.flectone.chat.module.integrations;

import com.loohp.interactivechat.api.InteractiveChatAPI;
import com.loohp.interactivechat.listeners.ChatEvents;
import com.loohp.interactivechat.registry.Registry;
import net.flectone.chat.FlectoneChat;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class FInteractiveChat implements FIntegration {

    public FInteractiveChat() {
        init();
    }

    public String checkMention(AsyncPlayerChatEvent event) {
        return ChatEvents.checkMention(event);
    }

    @NotNull
    public String mark(@NotNull String message, @NotNull UUID sender) {
        StringBuilder stringBuilder = new StringBuilder();
        for(String string : message.split(" ")) {
            if (!Registry.MENTION_TAG_CONVERTER.containsTags(string) && !string.contains("<cmd="))
                string = InteractiveChatAPI.markSender(string, sender);

            stringBuilder.append(string).append(" ");
        }

        return stringBuilder.toString().trim();
    }

    @Override
    public void init() {
        FlectoneChat.info("InteractiveChat detected and hooked");
    }
}
