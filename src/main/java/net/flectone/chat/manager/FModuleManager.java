package net.flectone.chat.manager;

import lombok.Getter;
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

    private static final HashMap<Class<?>, FModule> F_MODULE_MAP = new HashMap<>();

    private CommandsModule commandsModule;
    private ExtraModule extraModule;
    private PlayerModule playerModule;
    private SoundsModule soundsModule;
    private ServerModule serverModule;
    private IntegrationsModule integrationsModule;
    private ChatBubbleModule chatBubbleModule;
    private AutoMessageModule autoMessageModule;
    private ServerMessageModule serverMessageModule;
    private PlayerMessageModule playerMessageModule;
    private ColorModule colorModule;

    public Collection<FModule> getModules() {
        return F_MODULE_MAP.values();
    }

    public void put(FModule fModule) {
        F_MODULE_MAP.put(fModule.getClass(), fModule);
    }

    public void init() {
        commandsModule = new CommandsModule("commands");
        extraModule = new ExtraModule("extra");
        playerModule = new PlayerModule("player");
        soundsModule = new SoundsModule("sounds");
        serverModule = new ServerModule("server");
        integrationsModule = new IntegrationsModule("integrations");
        chatBubbleModule = new ChatBubbleModule("chat-bubble");
        autoMessageModule = new AutoMessageModule("auto-message");
        serverMessageModule = new ServerMessageModule("server-message");
        playerMessageModule = new PlayerMessageModule("player-message");
        colorModule = new ColorModule("color");

        FActionManager.add(new FPlayerActionListener(null));
        FActionManager.add(new MarkSpawnListener(null));
        FActionManager.add(new ChatBubbleSpawnListener(null));
        FActionManager.add(new FPlayerTicker(null));
        FActionManager.add(new SpitHitListener(null));
    }

    @Nullable
    public FModule get(Class<?> clazz) {
        return F_MODULE_MAP.get(clazz);
    }

    public static void clear() {
        F_MODULE_MAP.clear();
    }
}
