package net.flectone.commands;

import net.flectone.Main;
import net.flectone.misc.commands.FCommand;
import net.flectone.misc.commands.FTabCompleter;
import net.flectone.listeners.PlayerAdvancementDoneListener;
import net.flectone.listeners.PlayerDeathEventListener;
import net.flectone.managers.FPlayerManager;
import net.flectone.managers.FileManager;
import net.flectone.managers.TickerManager;
import net.flectone.messages.MessageBuilder;
import net.flectone.utils.ObjectUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class CommandFlectonechat extends FTabCompleter {

    public CommandFlectonechat() {
        super.commandName = "flectonechat";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommand fCommand = new FCommand(commandSender, command.getName(), s, strings);

        if (strings.length < 1 || !strings[0].equals("reload") && strings.length < 5) {
            fCommand.sendUsageMessage();
            return true;
        }

        if (fCommand.isHaveCD()) return true;

        if (!strings[0].equals("reload")) {

            if (!strings[2].equals("set") || !strings[3].equals("boolean") && !strings[3].equals("integer") && !strings[3].equals("string")) {
                fCommand.sendUsageMessage();
                return true;
            }

            if (!Main.config.getKeys().contains(strings[1]) && !Main.locale.getKeys().contains(strings[1])) {
                fCommand.sendMeMessage("command.flectonechat.wrong-line");
                return true;
            }

            Object object;
            if (strings.length > 5) {
                object = ObjectUtil.toString(strings, 4);
            } else {
                object = getObject(strings[3], strings[4]);
            }

            switch (strings[0]) {
                case "config" -> {
                    Main.config.setObject(strings[1], object);
                    Main.config.saveFile();
                    Main.locale.setFileConfiguration(new FileManager("language/" + Main.config.getString("language") + ".yml"));
                }
                case "locale" -> {
                    Main.locale.setObject(strings[1], object);
                    Main.locale.saveFile();
                }
            }
        }

        Main.config = new FileManager("config.yml");
        Main.locale = new FileManager("language/" + Main.config.getString("language") + ".yml");

        TickerManager.clear();
        FPlayerManager.uploadPlayers();

        Bukkit.getOnlinePlayers().parallelStream().forEach(FPlayerManager::removePlayer);

        TickerManager.start();

        FPlayerManager.loadPlayers();
        MessageBuilder.loadPatterns();

        PlayerDeathEventListener.reload();
        PlayerAdvancementDoneListener.reload();

        fCommand.sendMeMessage("command.flectonechat.message");

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        wordsList.clear();

        if (strings.length == 1) {
            isStartsWith(strings[0], "reload");
            isStartsWith(strings[0], "config");
            isStartsWith(strings[0], "locale");
        } else if (strings.length == 2) {

            if (strings[0].equalsIgnoreCase("config")) {
                addKeysFile(Main.config, strings[1]);
            }
            if (strings[0].equalsIgnoreCase("locale")) {
                addKeysFile(Main.locale, strings[1]);
            }

        } else if (strings.length == 3) {
            isStartsWith(strings[2], "set");
        } else if (strings.length == 4) {
            isStartsWith(strings[3], "string");
            isStartsWith(strings[3], "integer");
            isStartsWith(strings[3], "boolean");
        }

        Collections.sort(wordsList);

        return wordsList;
    }

    private Object getObject(String objectName, String arg) {
        return switch (objectName.toLowerCase()) {
            case "integer" -> Integer.valueOf(arg);
            case "boolean" -> Boolean.parseBoolean(arg);
            default -> arg;
        };
    }
}
