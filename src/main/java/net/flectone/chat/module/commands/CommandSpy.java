package net.flectone.chat.module.commands;

import net.flectone.chat.FlectoneChat;
import net.flectone.chat.manager.FActionManager;
import net.flectone.chat.model.player.Settings;
import net.flectone.chat.module.FCommand;
import net.flectone.chat.module.FModule;
import net.flectone.chat.util.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.flectone.chat.manager.FileManager.locale;

public class CommandSpy extends FCommand {
    public CommandSpy(FModule module, String name) {
        super(module, name);
        init();
    }

    @Override
    public void init() {
        if (!isEnabled()) return;

        FActionManager.add(new SpyListener(null));

        register();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String alias,
                             @NotNull String[] args) {

        CmdSettings cmdSettings = new CmdSettings(commandSender, command);

        if (cmdSettings.isConsole()) {
            sendMessage(commandSender, getModule() + ".console");
            return true;
        }

        String value = cmdSettings.getFPlayer().getSettings().getValue(Settings.Type.SPY);
        boolean isEnabled = value != null && value.equals("1");

        cmdSettings.getFPlayer().getSettings().set(Settings.Type.SPY, isEnabled ? "-1" : "1");

        FlectoneChat.getDatabase().execute(() ->
                FlectoneChat.getDatabase().updateFPlayer("spy", cmdSettings.getFPlayer()));

        String message = locale.getVaultString(commandSender, this + "." + !isEnabled + "-message");
        commandSender.sendMessage(MessageUtil.formatAll(cmdSettings.getSender(), message));

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {
        return tabCompleteClear();
    }
}
