package net.flectone.chat.manager;


import lombok.Getter;
import net.flectone.chat.model.poll.Poll;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class PollManager {

    @Getter
    private static final Map<Integer, Poll> POLL_MAP = new HashMap<>();

    @Nullable
    public static Poll get(int id) {
        return POLL_MAP.get(id);
    }

    public static void add(@NotNull Poll poll) {
        POLL_MAP.put(poll.getId(), poll);
    }
}
