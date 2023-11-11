package net.flectone.chat.module.commands;

import net.flectone.chat.FlectoneChat;
import net.flectone.chat.model.player.FPlayer;
import net.flectone.chat.module.FCommand;
import net.flectone.chat.module.FModule;
import net.flectone.chat.util.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class CommandIgnore extends FCommand {

    public CommandIgnore(FModule module, String name) {
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

        if (args.length == 0) {
            sendUsageMessage(commandSender, alias);
            return true;
        }

        CmdSettings cmdSettings = new CmdSettings(commandSender, command);

        if (cmdSettings.isConsole()) {
            sendMessage(commandSender, getModule() + ".console");
            return true;
        }

        if (cmdSettings.isHaveCooldown()) {
            cmdSettings.getFPlayer().sendCDMessage(alias);
            return true;
        }

        String ignoredPlayerName = args[0];
        if (ignoredPlayerName.equalsIgnoreCase(commandSender.getName())) {
            sendMessage(commandSender, this + ".myself");
            return true;
        }

        FPlayer ignoredFPlayer = playerManager.getOffline(ignoredPlayerName);
        if (ignoredFPlayer == null) {
            sendMessage(commandSender, getModule() + ".null-player");
            return true;
        }

        if (cmdSettings.isHaveCooldown()) {
            sendMessage(commandSender, getModule() + ".cooldown");
            return true;
        }

        FPlayer fSender = cmdSettings.getFPlayer();

        List<UUID> senderIgnoreList = fSender.getIgnoreList();

        boolean isIgnored = senderIgnoreList.contains(ignoredFPlayer.getUuid());
        if (isIgnored) {
            senderIgnoreList.remove(ignoredFPlayer.getUuid());
            FlectoneChat.getPlugin().getDatabase().removeIgnore(fSender.getUuid(), ignoredFPlayer.getUuid());
        } else {
            senderIgnoreList.add(ignoredFPlayer.getUuid());
            FlectoneChat.getPlugin().getDatabase().addIgnore(fSender.getUuid(), ignoredFPlayer.getUuid());
        }

        fSender.setIgnoreList(senderIgnoreList);

        String message = locale.getVaultString(commandSender, this + "." + !isIgnored + "-message")
                .replace("<player>", ignoredFPlayer.getMinecraftName());
        message = MessageUtil.formatAll(cmdSettings.getSender(), message);

        commandSender.sendMessage(message);

        cmdSettings.getFPlayer().playSound(cmdSettings.getSender(), cmdSettings.getSender(), this.toString());

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {

        tabCompleteClear();

        if (args.length == 1) {
            isConfigModePlayer(args[0]);
        }

        return getSortedTabComplete();
    }
}
