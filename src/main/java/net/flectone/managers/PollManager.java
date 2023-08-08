package net.flectone.managers;

import net.flectone.misc.actions.Poll;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PollManager {

    private static final List<Poll> pollList = new ArrayList<>();

    @Nullable
    public static Poll get(int id) {
        return pollList.get(id);
    }

    public static void add(@NotNull Poll poll) {
        pollList.add(poll);
    }

    @NotNull
    public static List<Poll> getPollList() {
        return pollList;
    }
}
