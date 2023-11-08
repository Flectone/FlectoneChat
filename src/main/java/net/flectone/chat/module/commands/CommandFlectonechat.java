package net.flectone.chat.module.commands;

import net.flectone.chat.FlectoneChat;
import net.flectone.chat.database.sqlite.Database;
import net.flectone.chat.manager.FActionManager;
import net.flectone.chat.manager.FModuleManager;
import net.flectone.chat.manager.FPlayerManager;
import net.flectone.chat.manager.FileManager;
import net.flectone.chat.model.file.FConfiguration;
import net.flectone.chat.module.FCommand;
import net.flectone.chat.module.FModule;
import net.flectone.chat.module.integrations.IntegrationsModule;
import net.flectone.chat.util.MessageUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class CommandFlectonechat extends FCommand {
    public CommandFlectonechat(FModule module, String name) {
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

        if (args.length < 1 || !args[0].equalsIgnoreCase("reload") &&
                (args[0].equalsIgnoreCase("set") && args.length < 4)) {

            sendUsageMessage(commandSender, alias);
            return true;
        }

        CmdSettings cmdSettings = new CmdSettings(commandSender, command);

        if (cmdSettings.isHaveCooldown()) {
            cmdSettings.getFPlayer().sendCDMessage(alias);
            return true;
        }

        if (args[0].equalsIgnoreCase("set")) {
            String fileName = args[1];

            Optional<FileManager.Type> fileType = Arrays.stream(FileManager.Type.values())
                    .filter(type -> type.getFileName().equalsIgnoreCase(fileName))
                    .findAny();

            if (fileType.isEmpty() || fileType.get().getFile() == null) {
                sendMessage(commandSender, this + ".wrong-file");
                return true;
            }


            Object object = args[3];

            if (args.length > 4) {
                String string = MessageUtil.joinArray(args, 3, " ")
                        .replace("\\n", System.lineSeparator());
                if (string.startsWith("[") && string.endsWith("]")) {
                    string = string.substring(1, string.length() - 1);
                    object = new ArrayList<>(List.of(string.split(", ")));
                } else object = string;
            }

            if (StringUtils.isNumeric(args[3])) {
                object = Integer.parseInt(args[3]);
            } else if (NumberUtils.isNumber(args[3])) {
                object = Double.parseDouble(args[3]);
            } else if (args[3].equalsIgnoreCase("true") || args[3].equalsIgnoreCase("false")) {
                object = Boolean.parseBoolean(args[3]);
            }

            FConfiguration file = fileType.get().getFile();
            file.set(args[2], object);
            file.save();
        }

        // disable all
        IntegrationsModule.unregister();
        FActionManager.clearAll();
        FModuleManager.clear();

        FPlayerManager.terminateAll();

        FlectoneChat.getDatabase().getExecutor().close();
        FlectoneChat.getDatabase().disconnect();

        // enable all
        FileManager.init();
        FlectoneChat.setScoreBoard();

        FlectoneChat.setDatabase(new Database(FlectoneChat.getInstance()));

        FlectoneChat.setModuleManager(new FModuleManager());
        FlectoneChat.getModuleManager().init();

        FPlayerManager.loadOfflinePlayers();
        FPlayerManager.loadOnlinePlayers();
        FPlayerManager.loadTabCompleteData();

        sendMessage(commandSender, this + ".message");

        if (!cmdSettings.isConsole()) {
            cmdSettings.getFPlayer().playSound(cmdSettings.getSender(), cmdSettings.getSender(), this.toString());
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {

        tabCompleteClear();

        switch (args.length) {
            case 1 -> {
                isStartsWith(args[0], "reload");
                isStartsWith(args[0], "set");
            }
            case 2 -> {
                if (!args[0].equalsIgnoreCase("set")) break;

                for (var type : FileManager.Type.values()) {
                    if (type.getFile() == null) continue;
                    isStartsWith(args[1], type.getFileName());
                }
            }
            case 3 -> {
                Optional<FileManager.Type> fileType = Arrays.stream(FileManager.Type.values())
                        .filter(type -> type.getFileName().equalsIgnoreCase(args[1]))
                        .findAny();

                if (fileType.isEmpty() || fileType.get().getFile() == null) break;

                isFileKey(fileType.get().getFile(), args[2]);
            }

            case 4 -> {
                Optional<FileManager.Type> fileType = Arrays.stream(FileManager.Type.values())
                        .filter(type -> type.getFileName().equalsIgnoreCase(args[1]))
                        .findAny();


                if (fileType.isEmpty() || fileType.get().getFile() == null) break;

                if (args[0].equalsIgnoreCase("set")) {
                    Object object = fileType.get().getFile().get(args[2]);
                    if (object != null) {
                        if(object instanceof Boolean) {
                            isStartsWith(args[3], "true");
                            isStartsWith(args[3], "false");
                            break;
                        }

                        isStartsWith(args[3], String.valueOf(object)
                                .replace(System.lineSeparator(), "\\n"));
                    }
                }
            }
        }

        return getSortedTabComplete();
    }
}
