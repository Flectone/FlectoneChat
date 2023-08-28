package net.flectone.integrations.interactivechat;

import com.loohp.interactivechat.api.InteractiveChatAPI;
import com.loohp.interactivechat.listeners.ChatEvents;
import com.loohp.interactivechat.registry.Registry;
import net.flectone.Main;
import net.flectone.integrations.HookInterface;
import net.flectone.managers.HookManager;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class FInteractiveChat implements HookInterface {

    public static String checkMention(AsyncPlayerChatEvent event) {
        return ChatEvents.checkMention(event);
    }

    @NotNull
    public static String mark(@NotNull String message, @NotNull UUID sender) {
        StringBuilder stringBuilder = new StringBuilder();
        for(String string : message.split(" ")) {
            if (!Registry.MENTION_TAG_CONVERTER.containsTags(string) && !string.contains("<cmd="))
                string = InteractiveChatAPI.markSender(string, sender);

            stringBuilder.append(string).append(" ");
        }

        return stringBuilder.toString().trim();
    }

    @Override
    public void hook() {
        HookManager.enabledInteractiveChat = true;
        Main.info("\uD83D\uDD12 InteractiveChat detected and hooked");
    }
}
