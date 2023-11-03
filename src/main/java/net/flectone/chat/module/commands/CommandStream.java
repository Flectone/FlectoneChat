package net.flectone.chat.module.commands;

import net.flectone.chat.FlectoneChat;
import net.flectone.chat.model.player.FPlayer;
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

public class CommandStream extends FCommand {
    public CommandStream(FModule module, String name) {
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

        String trigger = args[0].toLowerCase();

        if (!trigger.equals("start") && !trigger.equals("end")) {
            sendUsageMessage(commandSender, alias);
            return true;
        }

        if (args.length == 1 && trigger.equals("start")) {
            sendMessage(commandSender, this + ".start.need-url");
            return true;
        }

        CmdSettings cmdSettings = new CmdSettings(commandSender, command);

        if (!cmdSettings.isConsole()) {
            FPlayer fPlayer = cmdSettings.getFPlayer();

            if (!fPlayer.isStreaming() && trigger.equals("end")) {
                sendMessage(commandSender, this + ".end.not");
                return true;
            }

            if (fPlayer.isStreaming() && trigger.equals("start")) {
                sendMessage(commandSender, this + ".start.already");
                return true;
            }

            if (trigger.equals("end")) {

                setStreaming(fPlayer, "-1");

                sendMessage(commandSender, this + ".end.message");
                return true;
            }

            if (cmdSettings.isHaveCooldown()) {
                sendCDMessage(cmdSettings.getSender(), alias, cmdSettings.getCooldownTime());
                return true;
            }

            if (cmdSettings.isMuted()) {
                sendMutedMessage(fPlayer);
                return true;
            }

            setStreaming(fPlayer, "1");

        }

        StringBuilder stringBuilder = new StringBuilder();

        locale.getVaultStringList(commandSender,this + ".start.message")
                .forEach(string -> {

                    string = string
                            .replace("<player>", commandSender.getName())
                            .replace("<links>", MessageUtil.joinArray(args, 1, "\n") + " ");

                    stringBuilder.append(string);
                    stringBuilder.append("\n");

                });

        sendGlobalMessage(cmdSettings.getSender(), cmdSettings.getItemStack(), stringBuilder.toString(), "", false);
        return true;
    }

    private void setStreaming(@NotNull FPlayer fPlayer, @NotNull String value) {
        fPlayer.getSettings().set(Settings.Type.STREAM, value);

        fPlayer.reloadStreamPrefix();

        FlectoneChat.getDatabase().execute(() ->
                FlectoneChat.getDatabase().updateFPlayer("stream", fPlayer));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {

        tabCompleteClear();

        if (args.length == 1) {
            isStartsWith(args[0], "start");
            isStartsWith(args[0], "end");
        } else if (args[0].equalsIgnoreCase("start")) {
            isTabCompleteMessage(commandSender, args[1], "stream-url");
        }

        return getSortedTabComplete();
    }
}
