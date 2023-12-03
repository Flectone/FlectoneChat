package net.flectone.chat.module.commands;

import net.flectone.chat.FlectoneChat;
import net.flectone.chat.module.FCommand;
import net.flectone.chat.module.FModule;
import net.flectone.chat.module.integrations.IntegrationsModule;
import net.flectone.chat.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class CommandTranslateto extends FCommand {

    public CommandTranslateto(FModule module, String name) {
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

        Bukkit.getScheduler().runTaskAsynchronously(FlectoneChat.getPlugin(), () ->
                asyncOnCommand(commandSender, command, alias, args));

        return true;
    }

    public void asyncOnCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String alias,
                               @NotNull String[] args) {

        if (args.length < 2) {
            sendUsageMessage(commandSender, alias);
            return;
        }

        CmdSettings cmdSettings = processCommand(commandSender, command);
        if (cmdSettings.isHaveCooldown()) {
            cmdSettings.getFPlayer().sendCDMessage(alias, command.getName());
            return;
        }

        if (cmdSettings.isMuted()) {
            cmdSettings.getFPlayer().sendMutedMessage(command.getName());
            return;
        }

        if (cmdSettings.isDisabled()) {
            sendErrorMessage(commandSender, getModule() + ".you-disabled");
            return;
        }

        String messageToTranslate = MessageUtil.joinArray(args, 2, " ");
        String message = translate(args[0], args[1], messageToTranslate);
        if (message.isEmpty() || message.equalsIgnoreCase(messageToTranslate)) {
            sendErrorMessage(commandSender, this + ".error");
            return;
        }

        String targetLanguage = args[1].toUpperCase();

        String formatString = locale.getVaultString(commandSender, this + ".message")
                .replace("<language>", targetLanguage);

        Bukkit.getScheduler().runTask(FlectoneChat.getPlugin(), () -> {
            sendGlobalMessage(cmdSettings.getSender(), cmdSettings.getItemStack(), formatString, message, true);

            IntegrationsModule.sendDiscordTranslateto(cmdSettings.getSender(), targetLanguage, message);
        });
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {
        tabCompleteClear();

        switch (args.length) {
            case 1 -> isTabCompleteMessage(commandSender, args[0], "source-language");
            case 2 -> isTabCompleteMessage(commandSender, args[1], "target-language");
            case 3 -> isTabCompleteMessage(commandSender, args[2], "message");
        }

        return getSortedTabComplete();
    }

    public String translate(@NotNull String sourceLang, @NotNull String targetLang, String msg) {

        try {
            msg = URLEncoder.encode(msg, StandardCharsets.UTF_8);
            URL url = new URL("http://translate.googleapis.com/translate_a/single?client=gtx&sl=" + sourceLang + "&tl="
                    + targetLang + "&dt=t&q=" + msg + "&ie=UTF-8&oe=UTF-8");

            URLConnection uc = url.openConnection();
            uc.setRequestProperty("User-Agent", "Mozilla/5.0");

            BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream(), StandardCharsets.UTF_8));
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                msg = inputLine;
            }

            in.close();

            String jsonResponse = msg;
            int startIndex = jsonResponse.indexOf("\"") + 1;
            int endIndex = jsonResponse.indexOf("\"", startIndex);
            return jsonResponse.substring(startIndex, endIndex);

        } catch (IOException e) {
            return "";
        }
    }
}
