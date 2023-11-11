package net.flectone.chat.module.commands;

import net.flectone.chat.module.FModule;

public class CommandsModule extends FModule {

    public CommandsModule(String name) {
        super(name);
        init();
    }

    @Override
    public void init() {
        if (!isEnabled()) return;
        register();

        actionManager.add(new CommandAfk(this, "afk"));
        actionManager.add(new CommandBall(this, "ball"));
        actionManager.add(new CommandBan(this, "ban"));
        actionManager.add(new CommandBanlist(this, "banlist"));
        actionManager.add(new CommandMe(this, "me"));
        actionManager.add(new CommandBroadcast(this, "broadcast"));
        actionManager.add(new CommandChatcolor(this, "chatcolor"));
        actionManager.add(new CommandChatsettings(this, "chatsettings"));
        actionManager.add(new CommandClearchat(this, "clearchat"));
        actionManager.add(new CommandFirstonline(this, "firstonline"));
        actionManager.add(new CommandFlectonechat(this, "flectonechat"));
        actionManager.add(new CommandHelper(this, "helper"));
        actionManager.add(new CommandIgnore(this, "ignore"));
        actionManager.add(new CommandIgnorelist(this, "ignorelist"));
        actionManager.add(new CommandKick(this, "kick"));
        actionManager.add(new CommandLastonline(this, "lastonline"));
        actionManager.add(new CommandMail(this, "mail"));
        actionManager.add(new CommandClearmail(this, "clearmail"));
        actionManager.add(new CommandMaintenance(this, "maintenance"));
        actionManager.add(new CommandMark(this, "mark"));
        actionManager.add(new CommandTell(this, "tell"));
        actionManager.add(new CommandMute(this, "mute"));
        actionManager.add(new CommandMutelist(this, "mutelist"));
        actionManager.add(new CommandPing(this, "ping"));
        actionManager.add(new CommandPoll(this, "poll"));
        actionManager.add(new CommandReply(this, "reply"));
        actionManager.add(new CommandSpit(this, "spit"));
        actionManager.add(new CommandSpy(this, "spy"));
        actionManager.add(new CommandStream(this, "stream"));
        actionManager.add(new CommandTictactoe(this, "tictactoe"));
        actionManager.add(new CommandTry(this, "try"));
        actionManager.add(new CommandDice(this, "dice"));
        actionManager.add(new CommandUnban(this, "unban"));
        actionManager.add(new CommandUnmute(this, "unmute"));
        actionManager.add(new CommandWarn(this, "warn"));
        actionManager.add(new CommandUnwarn(this, "unwarn"));
        actionManager.add(new CommandWarnlist(this, "warnlist"));
        actionManager.add(new CommandGeolocate(this, "geolocate"));
    }
}
