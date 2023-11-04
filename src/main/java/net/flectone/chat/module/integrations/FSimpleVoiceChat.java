package net.flectone.chat.module.integrations;

import de.maxhenkel.voicechat.api.BukkitVoicechatService;
import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import net.flectone.chat.FlectoneChat;
import net.flectone.chat.component.FComponent;
import net.flectone.chat.manager.FPlayerManager;
import net.flectone.chat.model.player.FPlayer;
import net.flectone.chat.util.MessageUtil;
import net.flectone.chat.util.TimeUtil;
import net.md_5.bungee.api.ChatMessageType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static net.flectone.chat.manager.FileManager.locale;


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

        Player player = (Player) event.getSenderConnection().getPlayer().getPlayer();
        FPlayer fPlayer = FPlayerManager.get(player);
        if(fPlayer == null || !fPlayer.isMuted()) return;

        event.cancel();
        String formatMessage = locale.getVaultString(player, "commands.muted")
                .replace("<time>", TimeUtil.convertTime(player, fPlayer.getMute().getRemainingTime()))
                .replace("<reason>", fPlayer.getMute().getReason())
                .replace("<moderator>", fPlayer.getMute().getModeratorName());

        formatMessage = formatMessage.replace(System.lineSeparator(), "");
        formatMessage = MessageUtil.formatAll(player, formatMessage);

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, FComponent.fromLegacyText(formatMessage));
    }
}
