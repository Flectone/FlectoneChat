package net.flectone.chat.module.playerMessage;

import net.flectone.chat.module.FModule;
import net.flectone.chat.module.playerMessage.anvil.AnvilModule;
import net.flectone.chat.module.playerMessage.book.BookModule;
import net.flectone.chat.module.playerMessage.chat.ChatModule;
import net.flectone.chat.module.playerMessage.formatting.FormattingModule;
import net.flectone.chat.module.playerMessage.patterns.PatternsModule;
import net.flectone.chat.module.playerMessage.sign.SignModule;
import net.flectone.chat.module.playerMessage.swearProtection.SwearProtectionModule;

public class PlayerMessageModule extends FModule {

    public PlayerMessageModule(String name) {
        super(name);
        init();
    }

    @Override
    public void init() {
        if (!isEnabled()) return;
        register();

        new PatternsModule(this, "patterns");
        new SwearProtectionModule(this, "swear-protection");
        new FormattingModule(this, "formatting");
        new ChatModule(this, "chat");
        new SignModule(this, "sign");
        new BookModule(this, "book");
        new AnvilModule(this, "anvil");
    }
}
