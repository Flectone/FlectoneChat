package net.flectone.chat.module;

import lombok.Getter;
import net.flectone.chat.FlectoneChat;
import net.flectone.chat.manager.FActionManager;
import net.flectone.chat.manager.FModuleManager;
import net.flectone.chat.manager.FPlayerManager;
import net.flectone.chat.model.file.FConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public abstract class FModule {

    private FModule module;
    private final String name;
    protected FPlayerManager playerManager;
    protected FModuleManager moduleManager;
    protected FActionManager actionManager;
    protected FConfiguration config;
    protected FConfiguration locale;
    protected FConfiguration modules;

    public FModule(String name) {
        this.name = name;

        FlectoneChat plugin = FlectoneChat.getPlugin();
        playerManager = plugin.getPlayerManager();
        moduleManager = plugin.getModuleManager();
        actionManager = plugin.getActionManager();
        config = plugin.getFileManager().getConfig();
        locale = plugin.getFileManager().getLocale();
        modules = plugin.getFileManager().getModules();
    }

    public FModule(FModule module, String name) {
        this(name);
        this.module = module;
    }

    public abstract void init();

    public void register() {
        moduleManager.put(this);
    }

    public boolean hasNoPermission(@Nullable Player player) {
        if (player == null) return false;
        return !player.hasPermission(getPermission());
    }

    public boolean hasNoPermission(@Nullable Player player, @NotNull String string) {
        if (player == null) return false;
        return !player.hasPermission(getPermission() + "." + string);
    }

    public String getPermission() {
        return "flectonechat." + this;
    }

    public boolean isEnabled() {
        String string = name + ".enable";
        return modules.getBoolean(module != null ? module + "." + string : string);
    }

    public boolean isEnabledFor(@Nullable Player player) {
        if (player == null) return true;
        String string = name + ".enable";

        return module != null
                ? module.isEnabledFor(player) && config.getVaultBoolean(player, module + "." + string)
                : config.getVaultBoolean(player, string);
    }

    @Override
    public String toString() {
        return module != null
                ? module + "." + name
                : name;
    }
}
