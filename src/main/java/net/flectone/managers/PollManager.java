package net.flectone.managers;

import net.flectone.custom.Poll;

import java.util.ArrayList;
import java.util.List;

public class PollManager {

    private static final List<Poll> pollList = new ArrayList<>();

    public static Poll get(int id) {
        return pollList.get(id);
    }

    public static void add(Poll poll) {
        pollList.add(poll);
    }

    public static List<Poll> getPollList() {
        return pollList;
    }
}
