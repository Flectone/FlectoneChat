package net.flectone.misc.components;

import org.jetbrains.annotations.NotNull;

import static net.flectone.managers.FileManager.locale;

public class FVoteComponent extends FComponent{

    public FVoteComponent(@NotNull String voteType, @NotNull String voteId) {
        super(locale.getFormatString("command.poll.format." + voteType, null));

        addRunCommand("/poll vote " + voteId + " " + voteType);
    }

}
