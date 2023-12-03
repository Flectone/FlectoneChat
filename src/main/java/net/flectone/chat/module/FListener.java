package net.flectone.chat.module;

import lombok.Getter;
import net.flectone.chat.FlectoneChat;
import net.flectone.chat.manager.FPlayerManager;
import net.flectone.chat.model.file.FConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Getter
public abstract class FListener implements Listener, FAction {

    private final FModule module;
    protected final FlectoneChat plugin;
    protected final FConfiguration config;
    protected final FConfiguration locale;
    protected final FConfiguration commands;
    protected final FPlayerManager playerManager;

    public FListener(FModule module) {
        this.module = module;

        plugin = FlectoneChat.getPlugin();
        config = plugin.getFileManager().getConfig();
        locale = plugin.getFileManager().getLocale();
        commands = plugin.getFileManager().getCommands();
        playerManager = plugin.getPlayerManager();
    }

    public boolean hasNoPermission(@NotNull Player player) {
        return !player.hasPermission(getPermission());
    }

    public boolean hasNoPermission(@NotNull Player player, @NotNull String string) {
        return !player.hasPermission(getPermission() + "." + string);
    }

    public String getPermission() {
        return "flectonechat." + getModule();
    }

    // https://hub.spigotmc.org/stash/projects/SPIGOT/repos/bukkit/browse/src/main/java/org/bukkit/plugin/java/JavaPluginLoader.java#228
    public void registerEvents() {
        PluginManager pluginManager = Bukkit.getServer().getPluginManager();

        for (final Method method : this.getClass().getMethods()) {
            final EventHandler eh = method.getAnnotation(EventHandler.class);
            if (eh == null) continue;

            if (method.isBridge() || method.isSynthetic()) {
                continue;
            }

            final Class<? extends Event> eventClass = method.getParameterTypes()[0].asSubclass(Event.class);
            method.setAccessible(true);

            EventExecutor executor = (listener, event) -> {
                try {
                    if (!eventClass.isAssignableFrom(event.getClass())) {
                        return;
                    }
                    method.invoke(listener, event);
                } catch (InvocationTargetException ex) {
                    throw new EventException(ex.getCause());
                } catch (Throwable t) {
                    throw new EventException(t);
                }
            };

            EventPriority eventPriority = getModule() != null
                    ? getEventPriority(eventClass.getSimpleName())
                    : eh.priority();

            pluginManager.registerEvent(eventClass, this, eventPriority, executor,
                    FlectoneChat.getPlugin(), false);
        }
    }

    public EventPriority getEventPriority(@NotNull String eventName) {

        if (getModule() == null || eventName.isEmpty()) return EventPriority.NORMAL;

        String priorityName = plugin.getFileManager().getListeners().getString(getModule() + "." + eventName);

        return !priorityName.isEmpty()
                ? EventPriority.valueOf(priorityName.toUpperCase())
                : EventPriority.NORMAL;
    }

}
