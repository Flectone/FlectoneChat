package net.flectone.listeners;

import net.flectone.messages.MessageBuilder;
import net.flectone.utils.ObjectUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

public class SignChangeListener implements Listener {

    @EventHandler
    public void onSignChange(SignChangeEvent event){
        for(int x = 0; x < event.getLines().length; x++){
            String string = event.getLine(x);

            if(string == null || string.isEmpty()) continue;

            MessageBuilder messageBuilder = new MessageBuilder("sign", string, event.getPlayer().getItemInHand(), false);
            String message = messageBuilder.getMessage();

            if(event.getPlayer().isOp() || event.getPlayer().hasPermission("flectonechat.formatting")){
                message = ObjectUtil.formatString(message, event.getPlayer());
            }

            event.setLine(x, message);
        }
    }
}
