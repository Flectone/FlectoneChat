package net.flectone.chat.module.commands;

import net.flectone.chat.builder.FComponentBuilder;
import net.flectone.chat.component.FComponent;
import net.flectone.chat.model.player.FPlayer;
import net.flectone.chat.model.player.Settings;
import net.flectone.chat.module.FCommand;
import net.flectone.chat.module.FModule;
import net.flectone.chat.util.MessageUtil;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CommandChatsettings extends FCommand {

    public CommandChatsettings(FModule module, String name) {
        super(module, name);
        init();
    }

    @Override
    public void init() {
        if (!isEnabled()) return;
        register();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {

        CmdSettings cmdSettings = new CmdSettings(commandSender, command);
        if (cmdSettings.isConsole()) {
            sendMessage(commandSender, getModule() + ".console");
            return true;
        }

        FPlayer fPlayer = cmdSettings.getFPlayer();

        if (args.length == 1 && args[0].equals("save")) {

            if (cmdSettings.isHaveCooldown()) {
                cmdSettings.getFPlayer().sendCDMessage(alias);
                return true;
            }

            database.execute(() -> database.updateFPlayer("settings", fPlayer));

            Bukkit.dispatchCommand(commandSender, "chatsettings");
            return true;
        }

        if (args.length == 3) {
            String name = args[0].toLowerCase();
            String enabled = args[1].toLowerCase();
            String typeName = args[2].toLowerCase();

            Settings.Type type = Settings.Type.fromString(typeName);
            if (type == null) {
                sendUsageMessage(commandSender, alias);
                return true;
            }

            List<String> possibleChangeList = commands.getStringList(getName() + ".change-list");
            if (!possibleChangeList.contains(name)
                    || !commandSender.hasPermission("flectonechat.commands.chatsettings." + name)) {
                sendMessage(commandSender, this + ".not-available");
                return true;
            }

            if (!typeName.equalsIgnoreCase(Settings.Type.CHAT.toString())) {
                fPlayer.getSettings().set(type, Boolean.parseBoolean(enabled) ? -1 : 1);
            } else {
                fPlayer.getSettings().set(Settings.Type.CHAT, name);
            }

            String message = locale.getVaultString(commandSender, this + ".message-changed");

            FComponent fComponent = new FComponent(MessageUtil.formatAll(cmdSettings.getSender(), message));
            fComponent.addRunCommand("/chatsettings save");
            cmdSettings.getSender().spigot().sendMessage(fComponent.get());
            return true;
        }

        String message = locale.getVaultStringList(commandSender, this + ".message")
                .stream()
                .map(string -> string + "\n")
                .collect(Collectors.joining());

        List<Settings.Type> typesList = new ArrayList<>(List.of(Settings.Type.values()));
        typesList.remove(Settings.Type.UUID);
        typesList.remove(Settings.Type.COLORS);
        typesList.remove(Settings.Type.STREAM);
        typesList.remove(Settings.Type.SPY);

        FComponentBuilder fComponentBuilder = new FComponentBuilder(message);

        fComponentBuilder.replace("<chat>", (chatBuilder, color) -> {
            Player player = cmdSettings.getSender();
            String currentChat = fPlayer.getSettings().getChat();
            if (currentChat == null || currentChat.isEmpty()) currentChat = "local";

            List<String> chatTypeList = config.getCustomList(player, "player-message.chat.list");

            for (String chatType : chatTypeList) {
                String formatName = locale.getVaultString(player, this + ".format.chat");
                formatName = MessageUtil.formatAll(player, formatName.replace("<chat>", chatType));

                String hoverText = locale.getVaultString(player, this + ".hover-text")
                        .replace("<component>", formatName);
                hoverText = MessageUtil.formatAll(player, hoverText);

                boolean isCurrentChat = currentChat.equalsIgnoreCase(chatType);

                String enableColor = locale.getVaultString(player, this + ".color." + isCurrentChat);
                enableColor = MessageUtil.formatAll(player, enableColor);

                FComponent fComponent = new FComponent(color + enableColor + formatName + " ");

                fComponent
                        .addHoverText(hoverText)
                        .addRunCommand("/chatsettings " + chatType + " " + isCurrentChat + " " + Settings.Type.CHAT);

                chatBuilder.append(fComponent.get(), ComponentBuilder.FormatRetention.NONE);
            }
        });

        typesList.remove(Settings.Type.CHAT);

        for (Settings.Type type : typesList) {
            String name = type.toString()
                    .replaceFirst("enable_", "")
                    .replaceFirst("command_", "")
                    .replace("_", "-");
            Object object = fPlayer.getSettings().getValue(type);
            Boolean enabled = object == null || Integer.parseInt(String.valueOf(object)) != -1;

            fComponentBuilder.replace("<" + name + ">",
                    createReplace(fPlayer.getPlayer(), name, enabled, type.toString()));
        }


        Player player = fPlayer.getPlayer();
        player.spigot().sendMessage(fComponentBuilder.build(player, player));

        fPlayer.playSound(player, player, this.toString());

        return true;
    }

    public FComponentBuilder.Replace createReplace(@NotNull Player player, @NotNull String name,
                                                   @NotNull Boolean enabled, @NotNull String typeName) {
        return ((componentBuilder, color) -> {

            String formatName = locale.getVaultString(player, this + ".format." + name);
            formatName = MessageUtil.formatAll(player, formatName);

            String hoverText = locale.getVaultString(player, this + ".hover-text")
                    .replace("<component>", formatName);
            hoverText = MessageUtil.formatAll(player, hoverText);

            String enableColor = locale.getVaultString(player, this + ".color." + enabled);
            enableColor = MessageUtil.formatAll(player, enableColor);

            FComponent fComponent = new FComponent(color + enableColor + formatName);

            fComponent
                    .addHoverText(hoverText)
                    .addRunCommand("/chatsettings " + name + " " + enabled + " " + typeName);

            componentBuilder.append(fComponent.get(), ComponentBuilder.FormatRetention.NONE);
        });
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {
        tabCompleteClear();
        if (args.length == 1) {
            isStartsWith(args[0], "save");
        }

        return getTAB_COMPLETE();
    }
}
