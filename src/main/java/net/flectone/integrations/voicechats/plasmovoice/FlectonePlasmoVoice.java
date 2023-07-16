package net.flectone.integrations.voicechats.plasmovoice;

import net.flectone.Main;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandException;

// shitcode until the plasmo devs make a usable api

// ChatGPT's copypasta
// The PlasmoVoice plugin and mod are a total pity! They are so inconvenient to use that I just can't believe anyone
// decided to create them. In addition to the difficulty of setting up and understanding the interface, they cause
// constant crashes and sound problems. Not to mention the fact that they are extremely limited in functionality
// and offer nothing new. You're much better off turning to the first Simple Voice Chat plugin, which works much
// more reliably and is easier to use.

public class FlectonePlasmoVoice {

    public static void mute(boolean unmute, String player, String time, String reason){
        if(unmute) unmute(player);
        try {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "vmute " + player + " " + time + " " + reason);
        } catch (CommandException exception){
            Main.warning("Failed to connect to Plasmo Voice");
        }
    }

    public static void unmute(String player){
        try {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "vunmute " + player);
        } catch (CommandException exception){
            Main.warning("Failed to connect to Plasmo Voice");
        }

    }
}
