package net.flectone.utils;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

// Copyright (c) 2022, Tau <nullvoxel@gmail.com>
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//
// 1. Redistributions of source code must retain the above copyright notice, this
//   list of conditions and the following disclaimer.
//
// 2. Redistributions in binary form must reproduce the above copyright notice,
//   this list of conditions and the following disclaimer in the documentation
//   and/or other materials provided with the distribution.
//
// 3. Redistributions in source and/or binary forms must be free of all charges
//   and fees to the recipient of the redistribution unless It is clearly
//   disclosed to the recipient of the redistribution prior to payment that it
//   includes this software or portions of it and by providing a means to
//   obtain this software free of charge.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
// FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
// DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
// CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
// OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
// https://gitlab.com/TauCu/bukkit-utils/-/blob/master/src/main/java/me/taucu/bukkitutils/commands/CommandUtil.java
public class CommandsUtil {

    public static final TabExecutor emptyExec = new TabExecutor() {
        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
            return false;
        }

        @Override
        public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
            return null;
        }
    };

    public static boolean unregisterCommand(Command cmd) {
        HashSet<Command> set = new HashSet<>(1);
        set.add(cmd);
        return unregisterCommands(set);
    }

    public static boolean unregisterCommands(Collection<? extends Command> commands) {
        boolean changed = false;
        CommandMap commandMap = getCommandMap();
        Map<String, Command> knownCommands = getKnownCommands(commandMap);

        HashMap<String, Command> commandsToCheck = new HashMap<>();

        for (Command c : commands) {
            commandsToCheck.put(c.getLabel().toLowerCase(), c);
            commandsToCheck.put(c.getName().toLowerCase(), c);
            c.getAliases().forEach(a -> commandsToCheck.put(a.toLowerCase(), c));
        }

        for (Map.Entry<String, Command> check : commandsToCheck.entrySet()) {
            Command mappedCommand = knownCommands.get(check.getKey());
            if (check.getValue().equals(mappedCommand)) {
                mappedCommand.unregister(commandMap);
                knownCommands.remove(check.getKey());
                changed = true;
            } else if (check.getValue() instanceof PluginCommand checkPCmd) {
                if (mappedCommand instanceof PluginCommand mappedPCmd) {
                    CommandExecutor mappedExec = mappedPCmd.getExecutor();

                    if (mappedExec.equals(checkPCmd.getExecutor())) {
                        mappedPCmd.setExecutor(null);
                        mappedPCmd.setTabCompleter(null);
                    }
                }
                checkPCmd.setExecutor(emptyExec);
                checkPCmd.setTabCompleter(emptyExec);
            }
        }
        return changed;
    }

    public static CommandMap getCommandMap() {
        Server server = Bukkit.getServer();
        try {
            Method m = server.getClass().getDeclaredMethod("getCommandMap");
            m.setAccessible(true);
            return (CommandMap) m.invoke(Bukkit.getServer());
        } catch (Exception ignored) {
        }
        try {
            Field commandMapField = server.getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            return (CommandMap) commandMapField.get(server);
        } catch (Exception e) {
            throw new RuntimeException("Could not get commandMap", e);
        }
    }

    public static Map<String, Command> getKnownCommands(CommandMap m) {
        try {
            Method me = m.getClass().getDeclaredMethod("getKnownCommands");
            me.setAccessible(true);
            return (Map<String, Command>) me.invoke(m);
        } catch (Exception ignored) {
        }
        try {
            Field knownCommandsField = m.getClass().getDeclaredField("knownCommands");
            knownCommandsField.setAccessible(true);
            return (Map<String, Command>) knownCommandsField.get(m);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Could not get knownCommands", e);
        }
    }

    public static PluginCommand createCommand(JavaPlugin plugin, String cmd, List<String> aliases) {
        PluginCommand pc = createCommand(plugin, cmd);
        pc.setAliases(aliases);
        return pc;
    }

    public static PluginCommand createCommand(JavaPlugin plugin, String cmd) {
        PluginCommand command = null;
        cmd = cmd.toLowerCase();

        ReflectiveOperationException lastException = null;
        for (Constructor<?> constructor : PluginCommand.class.getDeclaredConstructors()) {
            try {
                constructor.setAccessible(true);
                command = (PluginCommand) constructor.newInstance(cmd, plugin);
            } catch (ReflectiveOperationException e) {
                lastException = e;
            }
        }

        if (command == null) {
            throw new RuntimeException("Could not create PluginCommand", lastException);
        }

        return command;
    }
}
