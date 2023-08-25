package net.flectone.integrations.voicechats.simplevoicechat;

import de.maxhenkel.voicechat.api.BukkitVoicechatService;
import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import net.flectone.Main;
import net.flectone.integrations.HookInterface;
import net.flectone.managers.FPlayerManager;
import net.flectone.misc.components.FComponent;
import net.flectone.misc.entity.FPlayer;
import net.flectone.utils.ObjectUtil;
import net.md_5.bungee.api.ChatMessageType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static net.flectone.managers.FileManager.locale;

public class FSimpleVoiceChat implements HookInterface, VoicechatPlugin {

    @Override
    public void hook() {
        BukkitVoicechatService service = Main.getInstance().getServer().getServicesManager().load(BukkitVoicechatService.class);

        if (service == null) return;

        service.registerPlugin(this);

        Main.info("\uD83D\uDD12 SimpleVoiceChat detected and hooked");
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
        FPlayer fPlayer = FPlayerManager.getPlayer(player);

        if(fPlayer == null || !fPlayer.isMuted()) return;

        event.cancel();
        String formatMessage = locale.getFormatString("command.mute.local-message", player)
                .replace("<time>", ObjectUtil.convertTimeToString(fPlayer.getMute().getDifferenceTime()))
                .replace("<reason>", fPlayer.getMute().getReason())
                .replace("<moderator>", fPlayer.getMute().getModeratorName());

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, FComponent.fromLegacyText(formatMessage));
    }
}
