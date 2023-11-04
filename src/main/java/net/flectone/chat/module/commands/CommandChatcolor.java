package net.flectone.chat.module.commands;

import net.flectone.chat.FlectoneChat;
import net.flectone.chat.manager.FPlayerManager;
import net.flectone.chat.model.player.FPlayer;
import net.flectone.chat.model.player.Settings;
import net.flectone.chat.module.FCommand;
import net.flectone.chat.module.FModule;
import net.flectone.chat.util.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.flectone.chat.manager.FileManager.*;

public class CommandChatcolor extends FCommand {

    private static final List<String> DEFAULT_MINECRAFT_COLORS = List.of("0", "1", "2", "3",
            "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f", "g", "h", "j", "m", "n", "p",
            "q", "s", "t", "u");

    public CommandChatcolor(FModule module, String name) {
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

        String possiblePlayerName = args[0];
        FPlayer possibleFPlayer = FPlayerManager.getOffline(possiblePlayerName);

        CmdSettings cmdSettings = new CmdSettings(commandSender, command);

        if (cmdSettings.isHaveCooldown()) {
            cmdSettings.getFPlayer().sendCDMessage(alias);
            return true;
        }

        HashMap<String, String> colors = new HashMap<>();

        if (possibleFPlayer != null && commandSender.hasPermission("flectonechat.commands.chatcolor.other")) {

            for (int x = 1; x < args.length; x++) {
                colors.put("&&" + x, args[x]);
            }

            setColors(possibleFPlayer, colors);
            return true;
        }

        if (cmdSettings.isConsole()) {
            sendMessage(commandSender, getModule() + ".console");
            return true;
        }

        if (args[0].equalsIgnoreCase("default")) {
            List<String> colorsKey = config.getCustomList(cmdSettings.getSender(), ".color.list");

            for (String colorKey : colorsKey) {
                colors.put(colorKey, config.getVaultString(cmdSettings.getSender(), "color.list." + colorKey));
            }
        } else {

            int minimumColors = commands.getInt(getName() + ".minimum");
            if (args.length < minimumColors) {
                String minimumMessage = locale.getVaultString(commandSender, this + ".minimum-message");
                minimumMessage = minimumMessage.replace("<minimum>", String.valueOf(minimumColors));
                commandSender.sendMessage(MessageUtil.formatAll(cmdSettings.getSender(), minimumMessage));
                return true;
            }

            for (int x = 0; x < args.length; x++) {
                colors.put("&&" + (x + 1), args[x]);
            }
        }

        setColors(cmdSettings.getFPlayer(), colors);
        return true;
    }

    public void setColors(@NotNull FPlayer fPlayer, @NotNull HashMap<String, String> colors) {
        fPlayer.getSettings().set(Settings.Type.COLORS, colors);

        FlectoneChat.getDatabase().execute(() ->
                FlectoneChat.getDatabase().updateSettings(fPlayer, "colors"));

        Player player = fPlayer.getPlayer();

        if (player == null) return;

        String message = locale.getVaultString(player, this + ".message");
        String colorsMessage = locale.getVaultString(player, this + ".color").repeat(colors.size());

        for (Map.Entry<String, String> entry : colors.entrySet()) {
            colorsMessage = colorsMessage.replaceFirst("<color>", entry.getValue());
        }

        message = message.replace("<colors>", colorsMessage);
        player.sendMessage(MessageUtil.formatAll(player, player, message, true));

        fPlayer.playSound(fPlayer.getPlayer(), fPlayer.getPlayer(), this.toString());
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {
        tabCompleteClear();

        String arg = args[args.length - 1];

        isStartsWith(arg, "default");
        isStartsWith(arg, "#1abaf0");
        DEFAULT_MINECRAFT_COLORS.forEach(string -> isStartsWith(arg, "&" + string));

        return getSortedTabComplete();
    }
}
