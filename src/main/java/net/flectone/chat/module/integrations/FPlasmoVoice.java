package net.flectone.chat.module.integrations;

import net.flectone.chat.FlectoneChat;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

// shitcode until the plasmo devs make a usable api
public class FPlasmoVoice implements FIntegration {

    public FPlasmoVoice() {
        init();
    }

    public static void mute(boolean unmute, @NotNull String player, @NotNull String time, @NotNull String reason) {
        if (unmute) unmute(player);
        executeCommand("vmute " + player + " " + time + " " + reason);
    }

    public static void unmute(@NotNull String player) {
        executeCommand("vunmute " + player);
    }

    private static void executeCommand(@NotNull String command) {
        Bukkit.getScheduler().runTask(FlectoneChat.getInstance(), () ->
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
    }

    @Override
    public void init() {
        FlectoneChat.info("PlasmoVoice detected and hooked");
    }
}
