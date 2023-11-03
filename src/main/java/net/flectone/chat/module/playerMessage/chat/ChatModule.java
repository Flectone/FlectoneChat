package net.flectone.chat.module.playerMessage.chat;

import net.flectone.chat.FlectoneChat;
import net.flectone.chat.builder.MessageBuilder;
import net.flectone.chat.manager.FActionManager;
import net.flectone.chat.module.FModule;
import net.flectone.chat.module.chatBubble.ChatBubbleModule;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ChatModule extends FModule {

    public ChatModule(FModule module, String name) {
        super(module, name);
        init();
    }

    @Override
    public void init() {
        if (!isEnabled()) return;
        register();

        FActionManager.add(new ChatListener(this));
    }

    public void send(@NotNull Player sender, @NotNull List<Player> recipientsList,
                     @NotNull String message, @NotNull String chatFormat,
                     @NotNull List<String> featuresList) {

        MessageBuilder messageBuilder = new MessageBuilder(sender, sender.getInventory().getItemInMainHand(), message, featuresList);

        recipientsList.forEach(player ->
                player.spigot().sendMessage(messageBuilder.buildFormat(sender, player, chatFormat, true)));

        FModule fModule = FlectoneChat.getModuleManager().get(ChatBubbleModule.class);
        if (fModule instanceof ChatBubbleModule chatBubbleModule) {
            chatBubbleModule.add(sender, messageBuilder.getMessage(""));
        }
    }
}
