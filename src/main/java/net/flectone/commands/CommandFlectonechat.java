package net.flectone.commands;

import net.flectone.listeners.PlayerAdvancementDoneListener;
import net.flectone.listeners.PlayerDeathEventListener;
import net.flectone.managers.FPlayerManager;
import net.flectone.managers.FileManager;
import net.flectone.managers.TickerManager;
import net.flectone.messages.MessageBuilder;
import net.flectone.misc.brand.ServerBrand;
import net.flectone.misc.commands.FCommand;
import net.flectone.misc.commands.FTabCompleter;
import net.flectone.misc.files.FYamlConfiguration;
import net.flectone.utils.BlackListUtil;
import net.flectone.utils.ObjectUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static net.flectone.managers.FileManager.config;
import static net.flectone.managers.FileManager.locale;

public class CommandFlectonechat implements FTabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommand fCommand = new FCommand(commandSender, command.getName(), s, strings);

        if (strings.length < 1 || !strings[0].equals("reload") && strings.length < 4) {
            fCommand.sendUsageMessage();
            return true;
        }

        if (fCommand.isHaveCD()) return true;

        if (!strings[0].equals("reload")) {

            FYamlConfiguration file = getFile(strings[0]);
            if (file == null || !strings[2].equals("set")) {
                fCommand.sendUsageMessage();
                return true;
            }

            Object object = file.get(strings[1]);

            if (object == null) {
                fCommand.sendMeMessage("command.flectonechat.wrong-line");
                return true;
            }

            Object newObject;

            if (strings.length > 4) {
                String string = ObjectUtil.toString(strings, 3)
                        .replace("\\n", System.lineSeparator());

                if (string.startsWith("[") && string.endsWith("]")) {
                    string = string.substring(1, string.length() - 1);
                    newObject = new ArrayList<>(List.of(string.split(", ")));
                } else newObject = string;

            } else newObject = getObject(object, strings[3]);

            if (!newObject.getClass().equals(object.getClass())) {
                fCommand.sendMeMessage("command.flectonechat.wrong-object");
                return true;
            }

            file.set(strings[1], newObject);
            file.save();
        }

        FileManager.initialize();

        TickerManager.clear();
        FPlayerManager.clearPlayers();

        Bukkit.getOnlinePlayers().parallelStream().forEach(FPlayerManager::removePlayer);

        TickerManager.start();

        FPlayerManager.loadPlayers();
        MessageBuilder.loadPatterns();

        PlayerDeathEventListener.reload();
        PlayerAdvancementDoneListener.reload();

        if (config.getBoolean("chat.swear-protection.enable")) {
            BlackListUtil.loadSwears();
        }

        if (config.getBoolean("server.brand.enable")) {
            ServerBrand.getInstance().updateEveryBrand();
        }

        fCommand.sendMeMessage("command.flectonechat.message");

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        wordsList.clear();

        switch (strings.length) {
            case 1 -> {
                isStartsWith(strings[0], "reload");
                isStartsWith(strings[0], "config");
                isStartsWith(strings[0], "locale");
            }
            case 2 -> {
                FYamlConfiguration file = getFile(strings[0]);
                if (file == null) break;

                isFileKey(file, strings[1]);
            }
            case 3 -> isStartsWith(strings[2], "set");
            case 4 -> {
                FYamlConfiguration file = getFile(strings[0]);
                if (file == null) break;

                Object object = file.get(strings[1]);
                if (object == null) break;

                if(object instanceof Boolean) {
                    isStartsWith(strings[3], "true");
                    isStartsWith(strings[3], "false");
                    break;
                }

                isStartsWith(strings[3], String.valueOf(object)
                        .replace(System.lineSeparator(), "\\n"));

            }
        }

        Collections.sort(wordsList);

        return wordsList;
    }

    @Nullable
    private FYamlConfiguration getFile(String name){
        return switch (name.toLowerCase()) {
            case "config" -> config;
            case "locale" -> locale;
            default -> null;
        };
    }

    @NotNull
    private Object getObject(@NotNull Object object, @NotNull String value) {
        if (object instanceof Integer) return Integer.parseInt(value);
        if (object instanceof Boolean) return Boolean.parseBoolean(value);

        return value.replace("\\n", System.lineSeparator());
    }

    @NotNull
    @Override
    public String getCommandName() {
        return "flectonechat";
    }
}
