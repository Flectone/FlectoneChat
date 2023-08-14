package net.flectone.misc.components;

import static net.flectone.managers.FileManager.locale;

public class FVoteComponent extends FComponent{

    public FVoteComponent(String voteType, String voteId) {
        super(locale.getFormatString("command.poll.format." + voteType, null));

        addRunCommand("/poll vote " + voteId + " " + voteType);
    }

}
