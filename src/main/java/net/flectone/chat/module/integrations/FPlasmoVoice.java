package net.flectone.chat.module.integrations;

import com.google.inject.Inject;
import net.flectone.chat.FlectoneChat;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.api.addon.AddonInitializer;
import su.plo.voice.api.addon.AddonLoaderScope;
import su.plo.voice.api.addon.ServerAddonsLoader;
import su.plo.voice.api.addon.annotation.Addon;
import su.plo.voice.api.server.PlasmoVoiceServer;
import su.plo.voice.api.server.mute.MuteDurationUnit;

import java.util.UUID;

@Addon(id = "FlectoneChat", scope = AddonLoaderScope.SERVER, version = "1.0.0", authors = {"TheFaser, fxd"})
public class FPlasmoVoice implements FIntegration, AddonInitializer {

    @Inject
    private PlasmoVoiceServer voiceServer;

    public FPlasmoVoice() {
        init();
    }

    public void mute(@NotNull Player player, @Nullable UUID moderator, int time, @NotNull String reason) {
        voiceServer.getMuteManager().mute(player.getUniqueId(), moderator, time, MuteDurationUnit.SECOND, reason, true);
    }

    public void unmute(@NotNull Player player) {
        voiceServer.getMuteManager().unmute(player.getUniqueId(), true);
    }

    @Override
    public void init() {
        ServerAddonsLoader.INSTANCE.load(this);
    }

    @Override
    public void onAddonInitialize() {
        FlectoneChat.info("PlasmoVoice detected and hooked");
    }
}
