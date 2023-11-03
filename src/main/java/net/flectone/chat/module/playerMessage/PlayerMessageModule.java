package net.flectone.chat.module.playerMessage;

import lombok.Getter;
import net.flectone.chat.module.FModule;
import net.flectone.chat.module.playerMessage.anvil.AnvilModule;
import net.flectone.chat.module.playerMessage.book.BookModule;
import net.flectone.chat.module.playerMessage.chat.ChatModule;
import net.flectone.chat.module.playerMessage.formatting.FormattingModule;
import net.flectone.chat.module.playerMessage.patterns.PatternsModule;
import net.flectone.chat.module.playerMessage.sign.SignModule;
import net.flectone.chat.module.playerMessage.swearProtection.SwearProtectionModule;

@Getter
public class PlayerMessageModule extends FModule {
    private PatternsModule patternsModule;
    private SwearProtectionModule swearProtectionModule;
    private FormattingModule formattingModule;
    private ChatModule chatModule;
    private SignModule signModule;
    private BookModule bookModule;
    private AnvilModule anvilModule;

    public PlayerMessageModule(String name) {
        super(name);
        init();
    }

    @Override
    public void init() {
        if (!isEnabled()) return;
        register();

        patternsModule = new PatternsModule(this, "patterns");
        swearProtectionModule = new SwearProtectionModule(this, "swear-protection");
        formattingModule = new FormattingModule(this, "formatting");
        chatModule = new ChatModule(this, "chat");
        signModule = new SignModule(this, "sign");
        bookModule = new BookModule(this, "book");
        anvilModule = new AnvilModule(this, "anvil");
    }
}
