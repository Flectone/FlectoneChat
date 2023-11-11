package net.flectone.chat.module.commands;

import net.flectone.chat.model.player.FPlayer;
import net.flectone.chat.model.player.Moderation;
import net.flectone.chat.module.FCommand;
import net.flectone.chat.module.FModule;
import net.flectone.chat.util.MessageUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CommandUnwarn extends FCommand {

    public CommandUnwarn(FModule module, String name) {
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

        database.execute(() -> asyncOnCommand(commandSender, command, alias, args));

        return true;
    }

    public void asyncOnCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String alias,
                               @NotNull String[] args) {

        if (args.length == 0) {
            sendUsageMessage(commandSender, alias);
            return;
        }

        FPlayer unwarnedFPlayer = playerManager.getOffline(args[0]);
        if (unwarnedFPlayer == null) {
            sendMessage(commandSender, getModule() + ".null-player");
            return;
        }

        database.getWarns(unwarnedFPlayer);

        if (unwarnedFPlayer.getCountWarns() == 0) {
            sendMessage(commandSender, this + ".not-warned");
            return;
        }

        int index = 0;

        if (args.length == 1 && args[0].startsWith("db:")) {
            int idWarn = Integer.parseInt(args[0].substring(2));

            for (int x = 0; x < unwarnedFPlayer.getWarnList().size(); x++) {
                Moderation warn = unwarnedFPlayer.getWarnList().get(x);
                if (warn.getId() != idWarn) continue;
                index = x;
                break;
            }
        } else {
            index = args.length == 1 || !StringUtils.isNumeric(args[1])
                    ? 1
                    : Integer.parseInt(args[1]);
        }

        if (index < 1 || index > unwarnedFPlayer.getCountWarns()) {
            sendMessage(commandSender, getModule() + ".long-number");
            return;
        }

        CmdSettings cmdSettings = new CmdSettings(commandSender, command);

        if (cmdSettings.isHaveCooldown()) {
            cmdSettings.getFPlayer().sendCDMessage(alias);
            return;
        }

        index--;
        unwarnedFPlayer.unwarn(index);

        String message = locale.getVaultString(cmdSettings.getSender(), this + ".message")
                .replace("<player>", unwarnedFPlayer.getMinecraftName());

        commandSender.sendMessage(MessageUtil.formatAll(cmdSettings.getSender(), message));

        if (!cmdSettings.isConsole()) {
            cmdSettings.getFPlayer().playSound(cmdSettings.getSender(), cmdSettings.getSender(), this.toString());
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {

        tabCompleteClear();
        switch (args.length) {
            case 1 -> {
                for (String playerName : playerManager.getWARNS_PLAYERS().keySet()) {
                    isStartsWith(args[0], playerName);
                }
            }
            case 2 -> {
                List<Moderation> warns = playerManager.getWARNS_PLAYERS().get(args[0]);
                if (warns == null) break;

                int k = 1;
                for (Moderation ignored : warns) {
                    isStartsWith(args[1], String.valueOf(k++));
                }
            }
        }

        return getSortedTabComplete();
    }
}
