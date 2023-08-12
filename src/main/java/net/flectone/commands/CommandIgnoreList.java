package net.flectone.commands;

import net.flectone.misc.commands.FCommand;
import net.flectone.misc.commands.FTabCompleter;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static net.flectone.managers.FileManager.locale;

public class CommandIgnoreList implements FTabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommand fCommand = new FCommand(commandSender, command.getName(), s, strings);

        if (fCommand.isConsoleMessage() || fCommand.getFPlayer() == null || fCommand.getPlayer() == null) return true;

        List<String> ignoreList = fCommand.getFPlayer().getIgnoreList();

        if (ignoreList.isEmpty()) {
            fCommand.sendMeMessage("command.ignore-list.empty");
            return true;
        }

        if (fCommand.isHaveCD()) return true;

        List<Inventory> inventoryList = new ArrayList<>();

        String inventoryTitle = locale.getFormatString("command.ignore-list.name", commandSender);

        int inventorySize = 9 * 3;
        int numInventories = (ignoreList.size() - 1) / inventorySize + 1;

        for (int x = 0; x < numInventories; x++) {
            Inventory inventory = Bukkit.createInventory(null, 9 * 3, inventoryTitle.replace("<number>", String.valueOf(x + 1)));
            inventoryList.add(inventory);
        }

        fCommand.getFPlayer().setInventoryList(inventoryList);
        fCommand.getPlayer().openInventory(inventoryList.get(fCommand.getFPlayer().getNumberLastInventory()));

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return wordsList;
    }

    @NotNull
    @Override
    public String getCommandName() {
        return "ignore-list";
    }
}
