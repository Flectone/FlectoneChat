package net.flectone.commands;

import net.flectone.custom.FCommands;
import net.flectone.custom.FTabCompleter;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import net.flectone.Main;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CommandIgnoreList extends FTabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommands fCommand = new FCommands(commandSender, command.getName(), s, strings);

        if(fCommand.isConsoleMessage()) return true;

        List<String> ignoreList = fCommand.getFPlayer().getIgnoreList();

        if(ignoreList.isEmpty()){
            fCommand.sendMeMessage("ignore-list.empty");
            return true;
        }

        if(fCommand.isHaveCD()) return true;

        List<Inventory> inventoryList = new ArrayList<>();

        String inventoryTitle = Main.locale.getFormatString("ignore-list.name", commandSender);

        int inventorySize = 9 * 3;
        int numInventories = (ignoreList.size() - 1) / inventorySize + 1;

        for(int x = 0; x < numInventories; x++){
            Inventory inventory = Bukkit.createInventory(null, 9*3, inventoryTitle + (x+1));
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
}
