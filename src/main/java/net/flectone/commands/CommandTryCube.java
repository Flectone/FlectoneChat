package net.flectone.commands;

import net.flectone.custom.FCommands;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import net.flectone.Main;

import java.util.Random;

public class CommandTryCube implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommands fCommand = new FCommands(commandSender, command.getName(), s, strings);

        if(fCommand.isInsufficientArgs(1)) return true;

        if(!StringUtils.isNumeric(strings[0])){
            fCommand.sendMeMessage("try-cube.only_int");
            return true;
        }

        int amount = Integer.parseInt(strings[0]);

        if(amount > Main.config.getInt("try-cube.max_amount")){
            fCommand.sendMeMessage("try-cube.too_much");
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
            stringBuilder.append(Main.config.getString("try-cube." + cubeType)).append(" ");
        }

        if(amount == 6 && values == 21 && stringBuilder.toString().equals("⚀ ⚁ ⚂ ⚃ ⚄ ⚅ ")){

            String formatString = Main.locale.getString("try-cube.very_lucky")
                    .replace("<player>", fCommand.getSenderName());

            fCommand.sendGlobalMessage(formatString);
            return true;
        }

        String formatString = Main.locale.getString("try-cube.success_" + (values >= Integer.parseInt(strings[0])*3.5))
                .replace("<cube>", stringBuilder.toString())
                .replace("<player>", fCommand.getSenderName());

        fCommand.sendGlobalMessage(formatString);

        return true;
    }
}
