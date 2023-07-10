package net.flectone.integrations.voicechats.simplevoicechat;

import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.*;
import net.flectone.Main;
import net.flectone.custom.FPlayer;
import net.flectone.managers.FPlayerManager;
import net.flectone.utils.ObjectUtil;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

public class FlectoneVoiceChatPlugin implements VoicechatPlugin {

    public static VoicechatApi voicechatApi;

    /**
     * @return the unique ID for this voice chat plugin
     */
    @Override
    public String getPluginId() {
        return "FlectoneChat";
    }

    /**
     * Called when the voice chat initializes the plugin.
     *
     * @param api the voice chat API
     */
    @Override
    public void initialize(VoicechatApi api) {
        voicechatApi = api;
    }

    /**
     * Called once by the voice chat to register all events.
     *
     * @param registration the event registration
     */
    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(MicrophonePacketEvent.class, this::onMicrophonePacketEvent);
    }

    private void onMicrophonePacketEvent(MicrophonePacketEvent event){
        Player player = (Player) event.getSenderConnection().getPlayer().getPlayer();
        FPlayer fPlayer = FPlayerManager.getPlayer(player);
        if(fPlayer != null && fPlayer.isMuted()) {
            event.cancel();
            String formatMessage = Main.locale.getFormatString("command.mute.local-message", player)
                    .replace("<time>", ObjectUtil.convertTimeToString(fPlayer.getMuteTime()))
                    .replace("<reason>", fPlayer.getMuteReason());

            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(formatMessage));
        }
    }
}
