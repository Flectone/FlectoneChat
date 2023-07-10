package net.flectone.integrations.voicechats.plasmovoice;

import org.bukkit.Bukkit;

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
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "vmute " + player + " " + time + " " + reason);
    }

    public static void unmute(String player){
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "vunmute " + player);
    }
}
