package net.flectone.commands;

import net.flectone.Main;
import net.flectone.misc.commands.FCommand;
import net.flectone.misc.commands.FTabCompleter;
import net.flectone.misc.components.FComponent;
import net.flectone.misc.entity.FPlayer;
import net.flectone.utils.ObjectUtil;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static net.flectone.managers.FileManager.locale;
import static net.flectone.managers.FileManager.config;

public class CommandChatSettings implements FTabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommand fCommand = new FCommand(commandSender, command.getName(), s, strings);

        if (fCommand.isConsoleMessage()) return true;

        FPlayer fPlayer = fCommand.getFPlayer();
        assert fPlayer != null;

        if (strings.length == 1) {

            if (strings[0].equals("save")) {

                Main.getDataThreadPool().execute(() ->
                        Main.getDatabase().updatePlayerInfo("chats", fPlayer.getChatInfo()));

                Bukkit.dispatchCommand(commandSender, "chat-settings");
                return true;
            }

            if(!fPlayer.getChatInfo().getOptionsList().contains(strings[0])) {
                fCommand.sendUsageMessage();
                return true;
            }

            if (!optionEnable(strings[0].toLowerCase()) || !commandSender.hasPermission("flectonechat.chat-settings." + strings[0].toLowerCase())) {
                fCommand.sendMeMessage("command.chat-settings.not-available");
                return true;
            }

            fPlayer.getChatInfo().setOption(strings[0], !fPlayer.getChatInfo().getOption(strings[0]));
            FComponent fComponent = new FComponent(locale.getFormatString("command.chat-settings.message-changed", commandSender));
            fComponent.addRunCommand("/chat-settings save");
            fPlayer.spigotMessage(fComponent.get());
            return true;
        }

        String configString = locale.getStringList("command.chat-settings.message").stream()
                .map(str -> ObjectUtil.formatString(str + "\n", commandSender)).
                collect(Collectors.joining());

        Set<String> optionsList = fPlayer.getChatInfo().getOptionsList().stream()
                .map(string -> "<" + string + ">")
                .collect(Collectors.toSet());

        ArrayList<String> placeholders = new ArrayList<>(optionsList);

        List<String> chatTypes = List.of("<local>", "<global>", "<onlylocal>", "<onlyglobal>");
        placeholders.addAll(chatTypes);

        String mainColor = "";
        ComponentBuilder mainBuilder = new ComponentBuilder();

        for(String mainPlaceholder : ObjectUtil.splitLine(configString, placeholders)) {

            if (optionsList.contains(mainPlaceholder)) {

                String name = mainPlaceholder.substring(1, mainPlaceholder.length() - 1);

                String text = locale.getFormatString("command.chat-settings.format." + name, commandSender);
                String hoverText = locale.getFormatString("command.chat-settings.hover-text", commandSender)
                        .replace("<component>", text);

                String color = locale.getFormatString("command.chat-settings.color." + fPlayer.getChatInfo().getOption(name), commandSender);

                FComponent fComponent = new FComponent(mainColor + color + text);

                fComponent
                        .addHoverText(hoverText)
                        .addRunCommand("/chat-settings " + name);

                mainBuilder.append(fComponent.get(), ComponentBuilder.FormatRetention.NONE);
            } else if (chatTypes.contains(mainPlaceholder)) {

                String name = mainPlaceholder.substring(1, mainPlaceholder.length() - 1);

                String text = locale.getFormatString("command.chat-settings.format." + name, commandSender);
                String hoverText = locale.getFormatString("command.chat-settings.hover-text", commandSender)
                        .replace("<component>", text);

                String color = locale.getFormatString("command.chat-settings.color." + fPlayer.getChatInfo().getChatType().equals(name), commandSender);

                String runCommandType = name.startsWith("only")
                        ? "hide " + (name.substring(4).equals("local") ? "global" : "local")
                        : "switch " + name;

                FComponent fComponent = new FComponent(mainColor + color + text);
                fComponent
                        .addHoverText(hoverText)
                        .addRunCommand("/switch-chat " + runCommandType);

                mainBuilder.append(fComponent.get(), ComponentBuilder.FormatRetention.NONE);

            } else {
                mainBuilder.append(new FComponent(mainColor + mainPlaceholder).get(), ComponentBuilder.FormatRetention.NONE);
            }

            mainColor = ChatColor.getLastColors(mainColor + mainBuilder.getCurrentComponent().toString());
        }

        fCommand.getFPlayer().spigotMessage(mainBuilder.create());

        return true;
    }

    private boolean optionEnable(String option) {

        if(!config.getString("command." + option + ".enable").isEmpty()) {
            return config.getBoolean("command." + option + ".enable");
        }

        if(!config.getString(option + ".message.enable").isEmpty()) {
            return config.getBoolean(option + ".message.enable");
        }

        if(!config.getString("player." + option + ".message.enable").isEmpty()) {
            return config.getBoolean("player." + option + ".message.enable");
        }

        if(!config.getString("chat." + option + ".enable").isEmpty()) {
            return config.getBoolean("chat." + option + ".enable");
        }

        return true;

    }


    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        wordsList.clear();

        if (strings.length == 1) {
            isStartsWith(strings[0], "save");
        }

        return wordsList;
    }

    @NotNull
    @Override
    public String getCommandName() {
        return "chat-settings";
    }
}
