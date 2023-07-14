package net.flectone.commands;

import net.flectone.custom.FCommands;
import net.flectone.custom.FTabCompleter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import net.flectone.Main;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CommandTryCube extends FTabCompleter {

    public CommandTryCube(){
        super.commandName = "try-cube";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommands fCommand = new FCommands(commandSender, command.getName(), s, strings);

        if(fCommand.isInsufficientArgs(1)) return true;

        if(!StringUtils.isNumeric(strings[0])){
            fCommand.sendMeMessage("command.try-cube.only-number");
            return true;
        }

        int amount = Integer.parseInt(strings[0]);

        if(amount > Main.config.getInt("command.try-cube.max-amount") || amount == 0){
            fCommand.sendMeMessage("command.try-cube.too-much");
            return true;
        }

        if(fCommand.isHaveCD()) return true;

        if(fCommand.isMuted()) return true;

        StringBuilder stringBuilder = new StringBuilder();
        int values = 0;

        Random random = new Random();

        while(amount-- != 0){
            int cubeType = random.nextInt(6) + 1;
            values += cubeType;
            stringBuilder.append(Main.locale.getString("command.try-cube.format." + cubeType)).append(" ");
        }

        if(amount == 6 && values == 21 && stringBuilder.toString().equals("⚀ ⚁ ⚂ ⚃ ⚄ ⚅ ")){

            String formatString = Main.locale.getString("command.try-cube.lucky-message")
                    .replace("<player>", fCommand.getSenderName());

            fCommand.sendGlobalMessage(formatString);
            return true;
        }

        String formatString = Main.locale.getString("command.try-cube." + (values >= Integer.parseInt(strings[0])*3.5) + "-message")
                .replace("<cube>", stringBuilder.toString())
                .replace("<player>", fCommand.getSenderName());

        fCommand.sendGlobalMessage(formatString);

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        wordsList.clear();

        if(strings.length == 1){
            for(int x = 1; x <= Main.config.getInt("command.try-cube.max-amount"); x++){
                isStartsWith(strings[0], String.valueOf(x));
            }
        }

        Collections.sort(wordsList);

        return wordsList;
    }
}
