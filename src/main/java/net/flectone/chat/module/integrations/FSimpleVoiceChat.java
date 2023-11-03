package net.flectone.chat.module.integrations;

import de.maxhenkel.voicechat.api.BukkitVoicechatService;
import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import net.flectone.chat.FlectoneChat;
import org.jetbrains.annotations.NotNull;


public class FSimpleVoiceChat implements FIntegration, VoicechatPlugin {

    public FSimpleVoiceChat() {
        init();
    }

    @Override
    public void init() {
        BukkitVoicechatService service = FlectoneChat.getInstance().getServer().getServicesManager().load(BukkitVoicechatService.class);

        if (service == null) return;

        service.registerPlugin(this);

        FlectoneChat.info("SimpleVoiceChat detected and hooked");
    }

    public static VoicechatApi voicechatApi;

    /**
     * @return the unique ID for this voice chat plugin
     */
    @NotNull
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
    public void initialize(@NotNull VoicechatApi api) {
        voicechatApi = api;
    }

    /**
     * Called once by the voice chat to register all events.
     *
     * @param registration the event registration
     */
    @Override
    public void registerEvents(@NotNull EventRegistration registration) {
        registration.registerEvent(MicrophonePacketEvent.class, this::onMicrophonePacketEvent);
    }

    private void onMicrophonePacketEvent(@NotNull MicrophonePacketEvent event) {
        if (event.getSenderConnection() == null) return;

//        Player player = (Player) event.getSenderConnection().getPlayer().getPlayer();
//        FPlayer fPlayer = FPlayerManager.getPlayer(player);
//
//        if(fPlayer == null || !fPlayer.isMuted()) return;
//
//        event.cancel();
//        String formatMessage = locale.getFormatString("command.mute.local-message", player)
//                .replace("<time>", ObjectUtil.convertTimeToString(fPlayer.getMute().getDifferenceTime()))
//                .replace("<reason>", fPlayer.getMute().getReason())
//                .replace("<moderator>", fPlayer.getMute().getModeratorName());
//
//        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, FComponent.fromLegacyText(formatMessage));
    }
}
