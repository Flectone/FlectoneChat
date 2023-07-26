package net.flectone.integrations.voicechats.simplevoicechat;

import de.maxhenkel.voicechat.api.BukkitVoicechatService;
import net.flectone.Main;

public class RegisterSimpleVoiceChat {

    public RegisterSimpleVoiceChat() {
        BukkitVoicechatService service = Main.getInstance().getServer().getServicesManager().load(BukkitVoicechatService.class);
        if (service != null) {
            FlectoneVoiceChatPlugin flectoneVoiceChatPlugin = new FlectoneVoiceChatPlugin();
            service.registerPlugin(flectoneVoiceChatPlugin);
        }
    }
}
