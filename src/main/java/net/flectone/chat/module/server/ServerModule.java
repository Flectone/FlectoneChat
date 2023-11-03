package net.flectone.chat.module.server;

import lombok.Getter;
import net.flectone.chat.module.FModule;
import net.flectone.chat.module.server.brand.BrandModule;
import net.flectone.chat.module.server.status.StatusModule;
import net.flectone.chat.module.server.tab.TabModule;

@Getter
public class ServerModule extends FModule {

    private BrandModule brandModule;
    private StatusModule statusModule;
    private TabModule tabModule;

    public ServerModule(String name) {
        super(name);
        init();
    }

    @Override
    public void init() {
        if (!isEnabled()) return;
        register();

        brandModule = new BrandModule(this, "brand");
        statusModule = new StatusModule(this, "status");
        tabModule = new TabModule(this, "tab");
    }
}
