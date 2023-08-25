package net.flectone.integrations.voicechats.plasmovoice;

import net.flectone.Main;
import net.flectone.integrations.HookInterface;
import net.flectone.managers.HookManager;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

// shitcode until the plasmo devs make a usable api

// ChatGPT's copypasta
// The PlasmoVoice plugin and mod are a total pity! They are so inconvenient to use that I just can't believe anyone
// decided to create them. In addition to the difficulty of setting up and understanding the interface, they cause
// constant crashes and sound problems. Not to mention the fact that they are extremely limited in functionality
// and offer nothing new. You're much better off turning to the first Simple Voice Chat plugin, which works much
// more reliably and is easier to use.

public class FPlasmoVoice implements HookInterface {

    public static void mute(boolean unmute, @NotNull String player, @NotNull String time, @NotNull String reason) {
        if (unmute) unmute(player);
        executeCommand("vmute " + player + " " + time + " " + reason);
    }

    public static void unmute(@NotNull String player) {
        executeCommand("vunmute " + player);
    }

    private static void executeCommand(@NotNull String command) {
        Bukkit.getScheduler().runTask(Main.getInstance(), () ->
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
    }

    @Override
    public void hook() {
        HookManager.enabledPlasmoVoice = true;
        Main.info("\uD83D\uDD12 PlasmoVoice detected and hooked");
    }
}
