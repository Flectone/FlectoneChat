package net.flectone.chat.manager;

import lombok.Getter;
import net.flectone.chat.FlectoneChat;
import net.flectone.chat.listener.*;
import net.flectone.chat.module.FModule;
import net.flectone.chat.module.autoMessage.AutoMessageModule;
import net.flectone.chat.module.chatBubble.ChatBubbleModule;
import net.flectone.chat.module.color.ColorModule;
import net.flectone.chat.module.commands.CommandsModule;
import net.flectone.chat.module.extra.ExtraModule;
import net.flectone.chat.module.integrations.IntegrationsModule;
import net.flectone.chat.module.player.PlayerModule;
import net.flectone.chat.module.playerMessage.PlayerMessageModule;
import net.flectone.chat.module.server.ServerModule;
import net.flectone.chat.module.serverMessage.ServerMessageModule;
import net.flectone.chat.module.sounds.SoundsModule;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;

@Getter
public class FModuleManager {

    private final HashMap<Class<?>, FModule> F_MODULE_MAP = new HashMap<>();

    public Collection<FModule> getModules() {
        return F_MODULE_MAP.values();
    }

    public void put(FModule fModule) {
        F_MODULE_MAP.put(fModule.getClass(), fModule);
    }

    public void init() {
        new CommandsModule("commands");
        new ExtraModule("extra");
        new PlayerModule("player");
        new SoundsModule("sounds");
        new ServerModule("server");
        new IntegrationsModule("integrations");
        new ChatBubbleModule("chat-bubble");
        new AutoMessageModule("auto-message");
        new ServerMessageModule("server-message");
        new PlayerMessageModule("player-message");
        new ColorModule("color");

        FActionManager actionManager = FlectoneChat.getPlugin().getActionManager();
        actionManager.add(new FPlayerActionListener(null));
        actionManager.add(new MarkSpawnListener(null));
        actionManager.add(new ChatBubbleSpawnListener(null));
        actionManager.add(new FPlayerTicker(null));
        actionManager.add(new SpitHitListener(null));
    }

    @Nullable
    public FModule get(Class<?> clazz) {
        return F_MODULE_MAP.get(clazz);
    }
}
