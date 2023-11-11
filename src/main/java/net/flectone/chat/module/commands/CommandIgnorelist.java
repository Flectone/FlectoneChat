package net.flectone.chat.module.commands;

import net.flectone.chat.builder.FComponentBuilder;
import net.flectone.chat.component.FComponent;
import net.flectone.chat.model.player.FPlayer;
import net.flectone.chat.module.FCommand;
import net.flectone.chat.module.FModule;
import net.flectone.chat.util.MessageUtil;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CommandIgnorelist extends FCommand {

    public CommandIgnorelist(FModule module, String name) {
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

        CmdSettings cmdSettings = new CmdSettings(commandSender, command);

        if (cmdSettings.isConsole()) {
            sendMessage(commandSender, getModule() + ".console");
            return true;
        }

        FPlayer fSender = cmdSettings.getFPlayer();

        int ignoreListSize = fSender.getIgnoreList().size();

        if (ignoreListSize == 0) {
            sendMessage(commandSender, this + ".empty");
            return true;
        }

        int perPage = commands.getInt(getName() + ".per-page");
        if (perPage == 0) {
            throw new RuntimeException("Per-page setting for /" + command + " cannot be zero");
        }

        int lastPage = (int) Math.ceil((double) ignoreListSize / perPage);

        if (args.length != 0 &&
                (!StringUtils.isNumeric(args[0]) || Integer.parseInt(args[0]) < 1 || Integer.parseInt(args[0]) > lastPage)) {
            sendMessage(commandSender, this + ".page-not-exist");
            return true;
        }

        if (cmdSettings.isHaveCooldown()) {
            cmdSettings.getFPlayer().sendCDMessage(alias);
            return true;
        }

        ComponentBuilder componentBuilder = new ComponentBuilder();

        String title = locale.getVaultString(commandSender, this + ".title")
                .replace("<count>", String.valueOf(ignoreListSize));
        title = MessageUtil.formatAll(cmdSettings.getSender(), title);

        componentBuilder
                .append(FComponent.fromLegacyText(title))
                .append("\n\n");

        String unignoreButton = locale.getVaultString(commandSender, this + ".unignore-button");
        String playerIgnoreFormat = locale.getVaultString(commandSender, this + ".player-ignore");
        String unignoreHover = locale.getVaultString(commandSender, this + ".unignore-hover");

        int page = args.length > 0 ? Math.max(1, Integer.parseInt(args[0])) : 1;
        page = Math.min(lastPage, page);

        fSender.getIgnoreList().stream().skip((long) (page - 1) * perPage).limit(perPage).forEach(uuid -> {

            String playerName = Bukkit.getOfflinePlayer(uuid).getName();
            if (playerName == null) return;

            String format = playerIgnoreFormat
                    .replace("<unignore>", unignoreButton)
                    .replace("<player>", playerName);

            format = MessageUtil.formatAll(cmdSettings.getSender(), format);

            String hover = unignoreHover.replace("<player>", playerName);
            hover = MessageUtil.formatAll(cmdSettings.getSender(), hover);

            FComponent textComponent = new FComponent(format)
                    .addHoverText(hover)
                    .addRunCommand("/ignore " + playerName);

            componentBuilder
                    .append(textComponent.get())
                    .append("\n\n");
        });

        componentBuilder.append("", ComponentBuilder.FormatRetention.NONE);

        String pageLine = locale.getVaultString(commandSender, this + ".page-line")
                .replace("<page>", String.valueOf(page))
                .replace("<last-page>", String.valueOf(lastPage));

        pageLine = MessageUtil.formatAll(cmdSettings.getSender(), pageLine);

        FComponentBuilder fComponentBuilder = getfComponentBuilder(pageLine, page, cmdSettings);

        componentBuilder.append(fComponentBuilder.build(cmdSettings.getSender(), cmdSettings.getSender()));

        commandSender.spigot().sendMessage(componentBuilder.create());

        cmdSettings.getFPlayer().playSound(cmdSettings.getSender(), cmdSettings.getSender(), this.toString());

        return true;
    }

    @NotNull
    private FComponentBuilder getfComponentBuilder(String pageLine, int page, CmdSettings cmdSettings) {
        FComponentBuilder fComponentBuilder = new FComponentBuilder(pageLine);

        AtomicInteger prevNumber = new AtomicInteger(page);

        fComponentBuilder.replace("<prev-page>", (prevBuilder, color) -> {
            String button = locale.getVaultString(cmdSettings.getSender(), this + ".prev-page");
            button = MessageUtil.formatAll(cmdSettings.getSender(), button);

            FComponent fComponent = new FComponent(color + button)
                    .addRunCommand("/ignorelist " + prevNumber.decrementAndGet());

            prevBuilder.append(fComponent.get(), ComponentBuilder.FormatRetention.NONE);
        });

        AtomicInteger nextNumber = new AtomicInteger(page);

        fComponentBuilder.replace("<next-page>", (nextBuilder, color) -> {
            String button = locale.getVaultString(cmdSettings.getSender(), this + ".next-page");
            button = MessageUtil.formatAll(cmdSettings.getSender(), button);

            FComponent fComponent = new FComponent(color + button)
                    .addRunCommand("/ignorelist " + nextNumber.incrementAndGet());

            nextBuilder.append(fComponent.get(), ComponentBuilder.FormatRetention.NONE);
        });
        return fComponentBuilder;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {
        tabCompleteClear();

        if (args.length == 1) {
            FPlayer fPlayer = playerManager.get(commandSender.getName());
            if (fPlayer == null) return getTAB_COMPLETE();

            int perPage = commands.getInt(getName() + ".per-page");

            int lastPage = (int) Math.ceil((double) fPlayer.getIgnoreList().size() / perPage);

            isDigitInArray(args[0], "", 1, lastPage + 1);
        }

        return getSortedTabComplete();
    }
}
