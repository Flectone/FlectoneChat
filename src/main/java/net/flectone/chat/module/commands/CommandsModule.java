package net.flectone.chat.module.commands;

import net.flectone.chat.manager.FActionManager;
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

        FActionManager.add(new CommandAfk(this, "afk"));
        FActionManager.add(new CommandBall(this, "ball"));
        FActionManager.add(new CommandBan(this, "ban"));
        FActionManager.add(new CommandBanlist(this, "banlist"));
        FActionManager.add(new CommandMe(this, "me"));
        FActionManager.add(new CommandBroadcast(this, "broadcast"));
        FActionManager.add(new CommandChatcolor(this, "chatcolor"));
        FActionManager.add(new CommandChatsettings(this, "chatsettings"));
        FActionManager.add(new CommandClearchat(this, "clearchat"));
        FActionManager.add(new CommandFirstonline(this, "firstonline"));
        FActionManager.add(new CommandFlectonechat(this, "flectonechat"));
        FActionManager.add(new CommandHelper(this, "helper"));
        FActionManager.add(new CommandIgnore(this, "ignore"));
        FActionManager.add(new CommandIgnorelist(this, "ignorelist"));
        FActionManager.add(new CommandKick(this, "kick"));
        FActionManager.add(new CommandLastonline(this, "lastonline"));
        FActionManager.add(new CommandMail(this, "mail"));
        FActionManager.add(new CommandClearmail(this, "clearmail"));
        FActionManager.add(new CommandMaintenance(this, "maintenance"));
        FActionManager.add(new CommandMark(this, "mark"));
        FActionManager.add(new CommandTell(this, "tell"));
        FActionManager.add(new CommandMute(this, "mute"));
        FActionManager.add(new CommandMutelist(this, "mutelist"));
        FActionManager.add(new CommandPing(this, "ping"));
        FActionManager.add(new CommandPoll(this, "poll"));
        FActionManager.add(new CommandReply(this, "reply"));
        FActionManager.add(new CommandSpit(this, "spit"));
        FActionManager.add(new CommandSpy(this, "spy"));
        FActionManager.add(new CommandStream(this, "stream"));
        FActionManager.add(new CommandTictactoe(this, "tictactoe"));
        FActionManager.add(new CommandTry(this, "try"));
        FActionManager.add(new CommandDice(this, "dice"));
        FActionManager.add(new CommandUnban(this, "unban"));
        FActionManager.add(new CommandUnmute(this, "unmute"));
        FActionManager.add(new CommandWarn(this, "warn"));
        FActionManager.add(new CommandUnwarn(this, "unwarn"));
        FActionManager.add(new CommandWarnlist(this, "warnlist"));
        FActionManager.add(new CommandGeolocate(this, "geolocate"));
    }
}
