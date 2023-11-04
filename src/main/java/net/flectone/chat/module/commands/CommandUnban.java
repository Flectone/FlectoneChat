package net.flectone.chat.module.commands;

import net.flectone.chat.FlectoneChat;
import net.flectone.chat.manager.FPlayerManager;
import net.flectone.chat.model.player.FPlayer;
import net.flectone.chat.model.player.Moderation;
import net.flectone.chat.module.FCommand;
import net.flectone.chat.module.FModule;
import net.flectone.chat.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.flectone.chat.manager.FileManager.locale;

public class CommandUnban extends FCommand {
    public CommandUnban(FModule module, String name) {
        super(module, name);
        init();
    }

    @Override
    public void init() {
        if (!isEnabled()) return;
        register();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String alias,
                             @NotNull String[] args) {

        FlectoneChat.getDatabase().execute(() ->
                asyncOnCommand(commandSender, command, alias, args));

        return true;
    }

    public void asyncOnCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String alias,
                               @NotNull String[] args) {

        if (args.length == 0) {
            sendUsageMessage(commandSender, alias);
            return;
        }

        FPlayer fPlayer = FPlayerManager.getOffline(args[0]);

        if (fPlayer == null) {
            sendMessage(commandSender, getModule() + ".null-player");
            return;
        }

        FlectoneChat.getDatabase().getBan(fPlayer);

        Moderation ban = fPlayer.getBan();

        if (ban == null || ban.isExpired()) {
            sendMessage(commandSender, this + ".not-banned");
            return;
        }

        CmdSettings cmdSettings = new CmdSettings(commandSender, command);

        if (cmdSettings.isHaveCooldown()){
            cmdSettings.getFPlayer().sendCDMessage(alias);
            return;
        }

        fPlayer.unban();

        String message = locale.getVaultString(cmdSettings.getSender(), this + ".message")
                .replace("<player>", fPlayer.getMinecraftName());

        commandSender.sendMessage(MessageUtil.formatAll(cmdSettings.getSender(), message));

        if (!cmdSettings.isConsole()) {
            cmdSettings.getFPlayer().playSound(cmdSettings.getSender(), cmdSettings.getSender(), this.toString());
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {
        tabCompleteClear();
        if (args.length == 1) {
            FPlayerManager.getBANNED_PLAYERS()
                    .stream()
                    .map(Bukkit::getOfflinePlayer)
                    .filter(offlinePlayer -> offlinePlayer.getName() != null)
                    .forEach(offlinePlayer -> isStartsWith(args[0], offlinePlayer.getName()));
        }

        return getSortedTabComplete();
    }
}
